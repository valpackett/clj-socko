(ns clj-socko.core
  (:use [okku core])
  (:require [clojure.string :as string])
  (:import [clj_socko ServerFactory]
           [clojure.lang ISeq]
           [java.io InputStream File]
           [org.apache.commons.io IOUtils FileUtils]
           [org.jboss.netty.buffer ChannelBufferInputStream]
           [org.mashupbots.socko.events
             HttpResponseStatus HttpResponseMessage HttpRequestEvent]))

(defmulti  body->bytes class)
(defmethod body->bytes String [s] (.getBytes s))
(defmethod body->bytes ISeq [s] (.getBytes (apply str s)))
(defmethod body->bytes InputStream [s] (IOUtils/toByteArray s))
(defmethod body->bytes File [f] (FileUtils/readFileToByteArray f))

(defn event->request [event]
  (let [msg (.request event)
        chan (.channel event)
        socket-adr (.getLocalAddress chan)
        ep (.endPoint msg)
        netty-req (.nettyHttpRequest msg)
        headers (into {} (ServerFactory/headersAsJava (.headers msg)))]
    {:server-port        (.getPort socket-adr)
     :server-name        (.getHostName socket-adr)
     :remote-addr        (-> ep .host (.split ":") first)
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

(defn run-server
  ([a as] (run-server a as {"port" 8088}))
  ([a as conf]
   (ServerFactory/runServer
     (ServerFactory/makeServer a conf as))))
