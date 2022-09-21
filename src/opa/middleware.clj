(ns opa.middleware
  (:require [opa.client :as opa]
            [clojure.string :as str]))

(defn- bearer-token
  [request]
  (when-let [authz-header (get-in request [:headers :authorization])]
    (when (str/starts-with? (str/lower-case authz-header) "bearer")
      (let [token (last (str/split (str/trim authz-header) #" "))]
        token))))

(defn default-input-fn
  "Default function for building the input map sent to OPA. Contains:

  :path   - list of path components
  :method - request method (uppercase)
  :token  - if found in authorization request header"
  [request]
  (let [input {:path (filter (complement str/blank?) (str/split (:uri request) #"/"))
               :method (str/upper-case (name (:request-method request)))}
        token (bearer-token request)]
    (cond-> input (some? token) (assoc :token token))))

(defn default-enforce-fn
  "Default function to call for enforcement, i.e. when a request is unauthorized"
  [_]                                                        ; opa-response-body
  {:status 403 :body "403 Forbidden" :headers {"Content-Type" "text/html; charset=utf-8"}})

(def default-options {:server-addr "http://localhost:8181"
                      :policy-path "/policy"
                      :input-fn default-input-fn
                      :enforce-fn default-enforce-fn})

(defn wrap-opa-authorize
  "Middleware for delegating authorization decisions to OPA and enforcing the
  decision returned. The options parameter is an optional map with the following keys
  provided as possible options:

  :server-addr - address of server (default http://localhost:8181)
  :policy-path - path to authorization policy document (excluding /v1/data)
  :input-fn    - function that takes the request and returns the input map to provide OPA
  :enforce-fn  - function called on authorization failure - provided OPA response as single argument"
  ([handler]
   (wrap-opa-authorize handler default-options))
  ([handler options]
   (fn
     ([request]
      (let [options (merge default-options options)
            response (opa/data-query options ((:input-fn options) request))
            body (:body response)]
        (if (= 200 (:status response))
          (if (true? (get-in body [:result :allow]))
            (handler request)
            ((:enforce-fn options) body))
          {:status 500 :body "500 Internal Server Error" :headers {"Content-Type" "text/html; charset=utf-8"}}))))))
