# clj-opa

[![Clojars Project](https://img.shields.io/clojars/v/clj-opa.svg)](https://clojars.org/clj-opa)

Ring middleware for app authorization using [Open Policy Agent](https://www.openpolicyagent.org/) (OPA). 

## Install

To install, add the following to your project `:dependencies`

```
[clj-opa "0.1.1"]
```

## Usage

#### wrap-opa-authorize

The `wrap-opa-authorize` middleware will intercept any incoming request and delegate the authorization decision to OPA.
By default, the `wrap-opa-authorize` middleware expects a response containing an `allow` attribute equal to `true` or 
else the `:enforce-fn` will be called, which in the default configuration serves a **403 Forbidden** to the caller.

Example configuration using [Compojure](https://github.com/weavejester/compojure) for routing:

```clojure
(ns my.app
  (:require [compojure.core :refer [defroutes ANY]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]] 
            [opa.middleware :refer [wrap-opa-authorize]]))

(defroutes app-routes 
           (ANY "/public" [] "Public endpoint")
           (ANY "/*" [] "Protected endpoint"))

(def app (-> (wrap-defaults app-routes api-defaults)
             (wrap-opa-authorize)))
```

Example Rego policy that allows `GET` requests on the `/public` endpoint:

```rego
package policy

default allow = false

allow {
    input.method == "GET"
    input.path[0] == "public"
}
```

##### Configuration

The `wrap-opa-authorize` function takes a configuration map as an optional argument, offering the following options:

| Option | Description | Default |
|--------|-------------|---------|
| `:server-addr` | Base URL of OPA server | http://localhost:8181 |
| `:policy-path` | Path to policy document (excluding `/v1/data`) | /policy |
| `:input-fn`    | Function taking request and returning the input map to provide OPA | See below |
| `:enforce-fn`  | Function to call when authorization fails | Function returning a 403 Forbidden response |

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

Example, custom configuration:

```clojure
(def app 
  (-> (wrap-defaults app-routes api-defaults)
      ; Query the OPA server at `https://opa.example.com` for authorization decisions on the 
      ; `/authz/policy/rules` path with input built from a custom request header `"X-Username"`
      ; and with and enforcement function that returns a 401 Unauthorized response on failures 
      ; and forwards the response body from OPA to the caller.
      (wrap-opa-authorize {:server-addr "https://opa.example.com"
                           :policy-path "/authz/policy/rules"
                           :input-fn (fn [request] {:username (get-in request [:headers :x-username])})
                           :enforce-fn (fn [opa-response] {:status 401 :body opa-response})})))
```

#### Other utilities and helper functions

See the [API reference](https://cljdoc.org/d/clj-opa/clj-opa/0.1.0/doc/readme).
