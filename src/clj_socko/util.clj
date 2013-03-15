(ns clj-socko.util
  (:import [clojure.lang ISeq]
           [java.io InputStream File]
           [org.apache.commons.io IOUtils FileUtils]))

(defmulti  body->bytes class)
(defmethod body->bytes String [s] (.getBytes s))
(defmethod body->bytes ISeq [s] (.getBytes (apply str s)))
(defmethod body->bytes InputStream [s] (IOUtils/toByteArray s))
(defmethod body->bytes File [f] (FileUtils/readFileToByteArray f))
