# clj-opa

Ring middleware for app authorization using [Open Policy Agent](https://www.openpolicyagent.org/) (OPA). 

## Install

To install, add the following to your project `:dependencies`

```
[clj-opa/opa "0.1.0"]
```

## Usage

#### wrap-opa-authorize

The wrap-opa-authorize middleware will intercept incoming requests and delegate any authorization decision to OPA, return 

By default, the `wrap-opa-authorize` middleware expects a response containing an `allow` rule equal to `true`.

Example configuration using [Compojure](https://github.com/weavejester/compojure) for routing:

```clojure
(ns my.app
  (:require [compojure.core :refer [defroutes ANY]]
            [compojure.route :as route]
            [clj-opa-service.client :refer [wrap-opa-authorize]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defroutes app-routes 
           (ANY "/public" [] "Public endpoint")
           (ANY "/*" [] "Protected endpoint")
           (route/not-found "Not Found"))

(def app (-> (wrap-defaults app-routes api-defaults)
             (wrap-opa-authorize)))
```

##### Configuration

The `wrap-opa-authorize` function takes a configuration map as an optional argument:

```clojure
{
 :server-addr "http://localhost:8181"
 :policy-path "/policy"
 :input-fn (fn [request] "...")
}
```
