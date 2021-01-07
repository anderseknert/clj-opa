package policy

default allow = false

allow {
    {"GET", "HEAD"}[input.method]
    input.path == ["public"]
}

p := input.path
