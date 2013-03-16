# clj-socko

A Clojure wrapper for the [Akka](http://akka.io)-based [Socko](http://sockoweb.org) web server.  
Uses [Okku](https://github.com/gaverhae/okku) as the Akka wrapper.  

Streaming and websocket coming soon.

## Usage

Basic Ring serving:

```clojure
(ns your.app
  (:use clj-socko.core))

(defn app [req]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body "{\"hello\": \"world\"}"})

(run-server {:http (actor-handler (ring-actor app))} {:port 8080})
```

With some routing:

```clojure
(ns your.app
  (:use clj-socko.core))

(defn app-one [req]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello World"})

(defn app-two [req]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello POST"})

(defn not-found [req]
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "Not found"})

(run-server
  {:http (cond-handler
           (every-pred get?  (path "/hello")) (ring-actor app-one)
           (every-pred post? (path "/testpost")) (ring-actor app-two)
           not-found? (ring-actor not-found))}
  {:port 8080})
```

This example routes `GET /hello` to `app-one`, `POST /testpost` to `app-two`, everything else to `not-found`.

`cond-handler` performs `cond` matching before sending the message to the actor.
That means you can route requests to non-Clojure actors.
clj-socko expects `Props` of the actor, not the class.

[every-pred](http://clojure.github.com/clojure/clojure.core-api.html#clojure.core/every-pred) is a standard Clojure function.
`not-found?` is a predicate that always returns true, no magic there.

**Note:** you shouldn't use this as your app's main routing thing.
Leave that to server-independent libraries like [Compojure](https://github.com/weavejester/compojure).
But this is useful for eg. routing static file requests to Socko's StaticContentHandler.

## Performance

On a completely unrealistic benchmark (instantly return a response), 2x slower than [http-kit](http://http-kit.org/).
On a more realistic benchmark (`(Thread/sleep 75)`), about the same.

## License

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                   Version 2, December 2004

Copyright (C) 2013 Greg V

Everyone is permitted to copy and distribute verbatim or modified
copies of this license document, and changing it is allowed as long
as the name is changed.

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

 0. You just DO WHAT THE FUCK YOU WANT TO.
