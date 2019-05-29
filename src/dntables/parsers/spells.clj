(ns dntables.spells
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.set :as set]
            [dntables.util :as util]
            [dntables.tex :as tex]
            [dntables.env :refer [env]]))

(def metanames [:name :level :school :classes])
(defn parse-meta [text]
  (some identity
        (map (fn [r]
               (let [[_ tp element] (re-find 
                                     (re-pattern (str "^(" (name r) "):\\s+(.*)")) 
                                     (s/trim text))]
                 
                 (when (and tp element)
                   {(keyword (s/lower-case tp)) element})))
             metanames)))

(def attribs [#"^\*\*Casting (Time):\*\* (.*)" #"^\*\*(Range):\*\* (.*)" #"^\*\*(Components):\*\* (.*)" #"^\*\*(Duration):\*\* (.*)"])
(defn parse-attribute [text]
  (some identity 
   (map (fn [r]
          (let [[_ tp element] (re-find r (s/trim text))]
            (when (and tp element)
              {(keyword (s/lower-case tp)) element :attribute true})
            ))
        attribs)))

(defn parse-note-text [text]
  (if-let [[_ n t] (re-find #"^\*\*(.*)\*\*(.*)" text)]
    {:notes [[n t]]})
  )

(defn parse-plain [text]
  (if-let [t (first (re-find #"^([A-Za-z0-9]+.*)" text))]
    {:text [t]}))


(defn parse-spell-file [f]
  (let [spell (atom {:class-collection? false
                     :text-collection? false})]
    (with-open [rdr (io/reader f)]
      (doseq [line (line-seq rdr)]
        (let [line (util/->empty->nil line)
              ld   (when line (or (parse-meta line) (parse-attribute line) (parse-plain line) (parse-note-text line)))]
          (when ld
            (when (:attribute ld)
              (swap! spell assoc :class-collection? false :text-collection? true))
            (case (first (keys ld))
              :classes (do
                         (swap! spell assoc :class-collection? true)
                         (swap! spell assoc :classes #{(:classes ld)}))
              :level (swap! spell assoc :level (Long/parseLong (:level ld)))
              :text (do
                      (if (:class-collection? @spell)
                        (swap! spell update :classes conj (first (:text ld)))
                        (swap! spell update :text concat (:text ld))))
              (swap! spell merge (dissoc ld :attribute)))))))
    (dissoc @spell :class-collection? :text-collection?)))


(defn parse-spell-files [source-dir]
  (let [files (-> source-dir io/file file-seq)]
    (map parse-spell-file (filter #(not (.isDirectory %)) files))))

(defn get-spells [pc-class levels & [location]]
  (let [location (or location (get-in (env) [:5e :srd :spells]))
        spells   (parse-spell-files location)]
    (filter
     (fn [spell]
       (and (contains? (:classes spell) pc-class)
            (contains? levels (:level spell))))
     spells)))

(comment

  (clojure.pprint/pprint
   (first
    (get-spells "druid" #{1})))

  (first (group-by :level (get-spells "druid" #{1 2})))

  (let [spells (->> (get-spells "druid" #{1 2 3})
                    (sort-by :name)
                    (group-by :level))]
    (tex/tex-template (io/resource "tex/tpl-5e-spell-tables.tex")
                      "table.tex"
                      {:spells spells
                       :keys (sort (keys spells))}))

  (parse-spell-files (get-in (env) [:5e :srd :spells]))

  (parse-spell-file (str (get-in (env) [:5e :srd :spells]) "/wall_of_ice.md"))

  (get-in (env) [:5e :srd :spells])

  )