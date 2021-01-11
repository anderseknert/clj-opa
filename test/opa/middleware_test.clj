(ns opa.middleware-test
  (:require [clojure.test :refer [deftest is]]
            [clj-http.client :as client]
            [compojure.core :refer [routes ANY]]
            [ring.mock.request :as mock]
            [opa.middleware :refer [wrap-opa-authorize]]))

(def handler (wrap-opa-authorize (routes (ANY "/*" [] "foo"))))

(defn- opa-response [status body]
  {:status status :headers {"Content-Type" "application/json"} :body body})

(deftest authorization-delegated-to-opa-allow
  (with-redefs [client/post (fn [_ _] (opa-response 200 {:result {:allow true}}))]
    (is (= (handler (mock/request :get "/foo"))
           {:headers {"Content-Type" "text/html; charset=utf-8"}
            :status 200
            :body "foo"}))))

(deftest authorization-delegated-to-opa-deny
  (with-redefs [client/post (fn [_ _] (opa-response 200 {:result {:allow false}}))]
    (is (= (handler (mock/request :get "/foo"))
           {:headers {"Content-Type" "text/html; charset=utf-8"}
            :status 403
            :body "403 Forbidden"}))))

(deftest authorization-delegated-to-opa-error
  (with-redefs [client/post (fn [_ _] {:status 500 :headers {"Content-Type" "application/json"} :body {"error" "yes"}})]
    (is (= (handler (mock/request :get "/foo"))
           {:headers {"Content-Type" "text/html; charset=utf-8"}
            :status 500
            :body "500 Internal Server Error"}))))

