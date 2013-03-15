(defproject clj-socko "0.1.0-SNAPSHOT"
  :description "Clojure wrapper for the Akka-based Socko web server"
  :license {:name "WTFPL"
            :url "http://www.wtfpl.net/about/"}
  :url "https://github.com/myfreeweb/clj-socko"
  :scala-source-path "src/scala"
  :prep-tasks ["scalac"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure.gaverhae/okku "0.1.3"]
                 [org.mashupbots.socko/socko-webserver_2.10 "0.2.4"]
                 [org.apache.commons/commons-io "1.3.2"]]
  :plugins [[org.scala-lang/scala-compiler "2.10.1"]
            [lein-scalac "0.1.0" :exclusions [org.scala-lang/scala-compiler]]
            [lein-midje "3.0.0"]
            [lein-release "1.0.0"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]
                                  [lein-release "1.0.0"]]}}
  :bootclasspath true
  :lein-release {:deploy-via :lein-deploy}
  :repositories [["snapshots" {:url "https://clojars.org/repo" :creds :gpg}]
                 ["releases"  {:url "https://clojars.org/repo" :creds :gpg}]])
