(ns clj-socko.util
  (:import [clojure.lang ISeq]
           [java.io InputStream File]
           [org.apache.commons.io IOUtils FileUtils]))

(defmulti  body->bytes class)
(defmethod body->bytes String [s] (.getBytes ^String s))
(defmethod body->bytes ISeq [s] (.getBytes ^String (apply str s)))
(defmethod body->bytes InputStream [s] (IOUtils/toByteArray ^InputStream s))
(defmethod body->bytes File [f] (FileUtils/readFileToByteArray f))
