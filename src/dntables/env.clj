(ns dntables.env
  (:require [cprop.core :refer [load-config]]
            [cprop.source :refer [from-props-file]]))

(def e nil)

(defn loadc []
  (def e (when (not *compile-files*)
           (load-config :resource "conf.edn"
                        :merge [(try
                                  (from-props-file "dntables.properties")
                                  (catch Exception e
                                    {}))])))
  e)
;; ^^
;; ... should probably be using mount instead of all this indirection:
(defn env [] (if e e (loadc)))