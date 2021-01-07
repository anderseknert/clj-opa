(ns opa.client
  (:require [clojure.string :refer [blank? split starts-with? lower-case upper-case trim]]
            [clj-http.client :as client]
            [clj-http.conn-mgr :as conn-mgr])
  (:import (java.io IOException)))

; Use a connection pool for the OPA client
(def cm (conn-mgr/make-reusable-conn-manager {:default-per-route 10 :timeout 5 :threads 10}))

; Close connection pool on shutdown
(.addShutdownHook (Runtime/getRuntime) (Thread. #(conn-mgr/shutdown-manager cm)))

(defn data-query
  "Query the OPA server's data API for decisions"
  [options input]
  (let [url (str (:server-addr options) "/v1/data" (:policy-path options))
        request {:accept :json
                 :connection-manager cm
                 :content-type :json
                 :redirect-strategy :none
                 :socket-timeout 3000
                 :connection-timeout 3000
                 :throw-exceptions false
                 :form-params {:input input}
                 :as :json
                 :coerce :always}]
    (try
      (client/post url request)
      (catch IOException ioe
        (let [message (.getMessage ioe)]
          (println message)
          {:status 500 :body {:error message}})))))
