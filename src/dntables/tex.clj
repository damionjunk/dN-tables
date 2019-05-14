(ns dntables.tex
  (:require [dntables.util :refer [is-relative?]]
            [clojure.java.io :as io]
            [selmer.parser :as sp]
            [selmer.util :refer [without-escaping]]
            [selmer.filters :as f]
            [clojure.string :as s]
            [dntables.parsers.text :as p]))

(def escape-chars {\\ "\\\\" \{ "\\{" \} "\\}" \_ "\\_" \^ "\\^" \# "\\#" \& "\\&" \$ "\\$" \% "\\%" \~ "\\~"})
(defn latex-escape [t] (s/escape t escape-chars))

(f/add-filter! :latexescape latex-escape)

(def tags
  {:tag-open \(
   :tag-close \)})

(defn ->tabularx [element]
  (without-escaping
    (sp/render-file "tex/tpl-table.tex" element tags)))

(defn ->document [elements]
  (without-escaping
    (sp/render-file "tex/tpl-document.tex"
                    {:tables (map ->tabularx elements)}
                    tags)))

(defn ->tex [source output]
  (spit output (-> (p/simple-reader source) ->document)))

(comment
  
  (->tex (io/resource "goatmansgoblet/familyweapons.txt") "resources/tex/go.tex")
  
  (->tex (io/resource "dwdiscord/d6d.txt") "resources/tex/go.tex")
  
  (as-> "goatmansgoblet/familyweapons.txt" $
    (io/resource $)
    (p/simple-reader $)
    (last $)
    (->document [$])
    (println $)
    )
  
  
  (as-> "goatmansgoblet/familyweapons.txt" $
    (io/resource $)
    (p/simple-reader $)
    (last $)
    (->tabularx $)
    (println $))

  )