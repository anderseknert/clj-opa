(defproject clj-opa "0.1.1"
  :description "Clojure utilities and Ring middleware for app authorization using Open Policy Agent (OPA)"
  :url "https://github.com/anderseknert/clj-opa"
  :license {:name "Apache License 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [clj-http "3.12.3"]
                 [cheshire "5.10.1"]]
  :deploy-repositories [["clojars" {:url "https://repo.clojars.org"
                                    :username :env/clojars_username
                                    :password :env/clojars_password
                                    ; TODO: fix at some point
                                    :sign-releases false}]]
  :profiles {:dev {:dependencies [[ring/ring-mock "0.4.0"]
                                  [compojure "1.6.2"]]
                   :plugins [[lein-ancient "0.6.15"]]}})

