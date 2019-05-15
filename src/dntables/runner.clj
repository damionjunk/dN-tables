(ns dntables.runner
  (:require [dntables.tex :as tex]
            [dntables.pdf :as pdf]
            [cheshire.core :as json]
            [clojure.pprint :as pp]
            [dntables.parsers.text :as text]))

(defmulti dispatch (fn [in out opts]
                     (cond
                       (:latex opts) :latex
                       (:pdf opts) :pdf
                       (:edn opts) :edn
                       (:json opts) :json
                       :else nil)))

(defmethod dispatch :latex [in out opts]
  (tex/->tex in out))
  
(defmethod dispatch :pdf [in out opts]
  (pdf/write-tables
   (pdf/build-tables-from-text in)
   out))

(defmethod dispatch :edn [in out opts]
  (spit out (with-out-str (pp/pprint (text/simple-reader in)))))

(defmethod dispatch :json [in out opts]
  (spit out (json/generate-string (text/simple-reader in) {:pretty true})))