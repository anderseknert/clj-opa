# clj-opa

[![Clojars Project](https://img.shields.io/clojars/v/clj-opa.svg)](https://clojars.org/clj-opa)

Ring middleware for app authorization using [Open Policy Agent](https://www.openpolicyagent.org/) (OPA). 

## Install

To install, add the following to your project `:dependencies`

```
[clj-opa "0.1.0"]
```

## Usage

#### wrap-opa-authorize

The wrap-opa-authorize middleware will intercept incoming requests and delegate any authorization decision to OPA, return 

By default, the `wrap-opa-authorize` middleware expects a response containing an `allow` rule equal to `true`.

Example configuration using [Compojure](https://github.com/weavejester/compojure) for routing:

TODO: fix imports below

```clojure
(ns my.app
  (:require [compojure.core :refer [defroutes ANY]]
            [clj-opa-service.client :refer [wrap-opa-authorize]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defroutes app-routes 
           (ANY "/public" [] "Public endpoint")
           (ANY "/*" [] "Protected endpoint"))

(def app (-> (wrap-defaults app-routes api-defaults)
             (wrap-opa-authorize)))
```

##### Configuration

The `wrap-opa-authorize` function takes a configuration map as an optional argument, offering the following options:

| Option | Description | Default |
|--------|-------------|---------|
| `:server-addr` | Base URL of OPA server | http://localhost:8181 |
| `:policy-path` | Path to policy document (excluding `/v1/data`) | /policy |
| `:input-fn`    | Function taking request and returning the input map to provide OPA | See below |

Unless a custom `:input-fn` is provided, an input map containing the path components (split on /), the request method 
and - if found in the request authorization header - a bearer token to use for authN/authZ:

```json
{
  "path": ["public", "assets", "images"],
  "method": "GET",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmRlcnNla25lcnQifQ.odt_88oGgYHoYU2SRdMjLYA0aG-NXDyeYqj_x9voAa4"
}
```
**Note** that the input map should _not_ contain the `input` attribute itself - that is added by the middleware.

If you would like to change or extend the default input map with custom attributes, you may do so by calling the 
`opa.middleware/default-input-fn` and change the result to your liking.
