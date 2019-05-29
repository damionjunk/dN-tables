(ns dntables.tex
  (:require [dntables.util :refer [is-relative?]]
            [clojure.java.io :as io]
            [selmer.parser :as sp]
            [selmer.util :refer [without-escaping]]
            [selmer.filters :as f]
            [clojure.string :as s]
            [dntables.env :refer [env]]
            [dntables.parsers.text :as p]))

(def escape-chars {\\ "\\\\" \{ "\\{" \} "\\}" \_ "\\_" \^ "\\^" \# "\\#" \& "\\&" \$ "\\$" \% "\\%" \~ "\\~"})
(defn latex-escape [t] (when t (s/escape t escape-chars)))

(f/add-filter! :latexescape latex-escape)
(f/add-filter! :dienormalize p/die-normalize)
(f/add-filter! :even even?)

(def tags {:tag-open \( :tag-close \)})

(defn ->tabularx [element]
  (without-escaping
    (sp/render-file (or (when-let [tt (get-in (env) [:tex :table])] (io/file tt)) (io/resource "tex/tpl-table.tex")) element tags)))

(defn ->document [elements]
  (without-escaping
    (sp/render-file (or (when-let [tt (get-in (env) [:tex :frame])] (io/file tt)) 
                        (io/resource "tex/tpl-memoir.tex"))
                    ;; grab keys from the first table item, since the metadata is attached
                    ;; to each one, and covers the entire set.
                    (merge 
                     {:tables (map ->tabularx elements)}
                     (select-keys (first elements) p/metakeys))
                    tags)))

(defn ->tex [source output]
  (let [troot (get-in (env) [:tex :templateroot])]
    (if (and (get-in (env) [:tex :frame]) (get-in (env) [:tex :table]))
      (let [root (or troot (-> (java.io.File. ".") .getCanonicalPath))]
        (println "Setting template root to: " root)
        (selmer.parser/set-resource-path! root))
      (println "Using classpath (included) LaTeX templates."))
    (spit output (-> (p/simple-reader source) ->document))))


(defn tex-template [source dest data]
  (let [troot (get-in (env) [:tex :templateroot])
        root (or troot (-> (java.io.File. ".") .getCanonicalPath))]
    (println "Setting template root to: " root)
    (selmer.parser/set-resource-path! root)
    (spit dest
          (without-escaping
           (sp/render-file source data tags)))))

(comment
 
 
 (->tex (io/file "inputexamples/rpgtalk/big.txt") "go.tex")
 
 (->tex (io/file "inputexamples/goatmansgoblet/familyweapons.txt") "go.tex")
  
  (->tex (io/file "inputexamples/dwdiscord/d6d.txt") "go.tex")
  
  (as-> "goatmansgoblet/familyweapons.txt" $
    (io/resource $)
    (p/simple-reader $)
    (last $)
    (->document [$])
    (println $))
  
  (as-> "goatmansgoblet/familyweapons.txt" $
    (io/resource $)
    (p/simple-reader $)
    (last $)
    (->tabularx $)
    (println $))

  )