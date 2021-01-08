package policy

# Example policy for testing. Start opa with:
#
# opa run --server policy.rego
#
# ..and test your app with the wrap-opa-authorize middleware enabled

default allow = false

allow {
    {"GET", "HEAD"}[input.method]
    input.path == ["public"]
}
