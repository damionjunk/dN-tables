(defproject dntables "0.1.0-SNAPSHOT"
  :description ""
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[nrepl/lein-nrepl "0.3.2"]]
  :main dntables.core
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [cheshire "5.8.1"]
                 [selmer "1.12.12"]
                 [org.clojure/tools.cli "0.4.2"]
                 [clj-pdf "2.3.4"]])
