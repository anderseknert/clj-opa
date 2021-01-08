(ns opa.middleware-test
  (:require [midje.sweet :refer :all]
            [opa.middleware :refer [wrap-opa-authorize]]
            [compojure.core :refer [routes ANY]]
            [ring.mock.request :as mock]
            [clj-http.fake :refer [with-fake-routes-in-isolation]]))

; NOTE: would prefer to just use clojure.test, but that apparently does not play well with clj-http.fake, see
; https://github.com/myfreeweb/clj-http-fake/issues/1
; An alternative would be to run a test server, or even OPA, but oh well, Midje will do for now.

(def allow "{\"result\":{\"allow\":true}}")
(def deny "{\"result\":{\"allow\":false}}")

(def handler (wrap-opa-authorize (routes (ANY "/*" [] "foo"))))

(with-fake-routes-in-isolation
  {"http://localhost:8181/v1/data/policy" (fn [_] {:status 200 :headers {"Content-Type" "application/json"} :body allow})}
  (fact "Authorization decision delegated to OPA (allow)"
    (handler (mock/request :get "/foo")) => {:headers {"Content-Type" "text/html; charset=utf-8"}
                                             :status 200
                                             :body "foo"}))

(with-fake-routes-in-isolation
  {"http://localhost:8181/v1/data/policy" (fn [_] {:status 200 :headers {"Content-Type" "application/json"} :body deny})}
  (fact "Authorization decision delegated to OPA (deny)"
        (handler (mock/request :get "/foo")) => {:headers {"Content-Type" "text/html; charset=utf-8"}
                                                 :status 403
                                                 :body "403 Forbidden"}))