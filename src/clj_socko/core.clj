(ns clj-socko.core
  (:use [okku core]
        [clj-socko util]
        [clojure walk])
  (:require [clojure.string :as string])
  (:import [java.net SocketAddress]
           [clj_socko ServerFactory Handler]
           [org.jboss.netty.buffer ChannelBufferInputStream]
           [org.jboss.netty.channel Channel]
           [akka.actor ActorSystem]
           [org.mashupbots.socko.events
             HttpResponseStatus HttpResponseMessage HttpRequestEvent]))

(defn event->request [^HttpRequestEvent event]
  (let [msg (.request event)
        chan ^Channel (.channel event)
        socket-adr (.getLocalAddress chan)
        ep (.endPoint msg)
        netty-req (.nettyHttpRequest msg)
        headers (into {} (ServerFactory/headersAsJava (.headers msg)))]
    {:server-port        (.getPort socket-adr)
     :server-name        (.getHostName socket-adr)
     :remote-addr        (-> ^String (.host ep) (.split ":") first)
     :uri                (.path ep)
     :query-string       (.queryString ep)
     :scheme             (-> (headers "X-Scheme" "http") string/lower-case keyword)
     :request-method     (-> ep .method string/lower-case keyword)
     :headers            headers
     :content-type       (.contentType msg)
     :content-length     (.contentLength msg)
     :character-encoding (headers "Content-Encoding")
     :body               (ChannelBufferInputStream. (.getContent netty-req))
     :keep-alive         (.isKeepAlive msg)}))

(defn ring-actor [handler]
  (actor
    (onReceive [^HttpRequestEvent event]
      (let [ring-rsp (-> event event->request handler)
            status (HttpResponseStatus. (:status ring-rsp))
            headers (:headers ring-rsp)
            ctype (headers "Content-Type" (headers "content-type" "application/octet-stream"))
            body (body->bytes (:body ring-rsp))
            socko-rsp ^HttpResponseMessage (.response event)]
        (.write socko-rsp status body ctype (ServerFactory/headersAsScala headers))
        (stop)))))

(defn handler [actor]
  (reify Handler
    (apply [this system]
      (spawn actor :in system))))

(defn run-server
  ([handlers conf] (run-server handlers conf (ActorSystem/create "as")))
  ([handlers conf system]
   (ServerFactory/runServer
     (ServerFactory/makeServer (stringify-keys handlers) (stringify-keys conf) system))))
