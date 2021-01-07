(defproject clj-opa/opa "0.1.0-SNAPSHOT"
  :description "Clojure utilities and Ring middleware for app authorization using Open Policy Agent (OPA)"
  :url "https://github.com/anderseknert/clj-opa"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.10.3"]
                 [cheshire "5.10.0"]]
  :profiles
  {:dev {:dependencies [[clj-http-fake "1.0.3"]]}})
