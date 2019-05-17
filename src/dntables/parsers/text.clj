(ns dntables.parsers.text
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [dntables.util :refer [replace-last]]))

(def metakeys [:doctitle :author :license :fontsize :source :licenseurl])
(def dice-alias-offsets {"d66" 1})

(defn meta? [skw text]
  (when text (some? (re-find (re-pattern (str "^:" skw "\\s+")) (s/trim text)))))

(defn parse-meta [skw text]
  (second (re-find (re-pattern (str "^:" skw "\\s+(.*)")) text)))

(defn title? [text]
  (when text (some? (re-find #"^[\[\()]|^[A-Za-z!\-\.:$#@]" (s/trim text)))))

(defn parse-title [text]
  (or (second (re-find #"^[\[\(]\s?\d?\s?[Dd]\s?\d+\s?[\]\)]\s?(.*)" text))
      (second (re-find #"^(.*)[\[\(]\s?\d?\s?[Dd]\s?\d+\s?[\]\)]\s?$" text))))

(defn parse-source-die [text]
  (or (second (re-find #"^[\[\(]\s?(\d?\s?[Dd]\s?\d+)\s?[\]\)]\s?.*" text))
      (second (re-find #"^.*[\[\(]\s?(\d?\s?[Dd]\s?\d+)\s?[\]\)]\s?$" text))) )

(defn entry? [text]
  (when text (some? (re-find #"^\d" (s/trim text)))))

(defn parse-entry [text]
  (second (re-find #"^\d+\s?[A-Za-z]{0,1}[\s\-:\.=\)\>]+(.*)" text)))

(defn subentry? [text]
  (when text (some? (re-find #"^[A-Za-z]{1,2}[\):\-\.=\>]+" (s/trim text)))))

(defn parse-subentry [text]
  (second (re-find #"^[A-Za-z]{1,2}[\s-:\.=\)\>]+(.*)" text)))

(defn ->empty->nil [x] (if (nil? x) nil (if (empty? (s/trim x)) nil (s/trim x))))

(defn one-chop [t] (if (re-find #"^1[\sDd]+" t) (subs t 1) t))
(defn die-normalize [t] (when t
                          (-> t
                              s/lower-case
                              one-chop)))

(defn add-metadata
  "Checks `line` for any meta-data present `kws`. If found, adds it under the same keyword to the 
   destination map `dest-map`."
  [kws dest-map line]
  (reduce
   (fn [m check-kw]     
     (if (meta? check-kw line) (assoc m check-kw (->empty->nil (parse-meta check-kw line)))
         m))
   dest-map
   kws))

(defn- die-start [dnd]
  (when dnd 
    (let [[_ ds] (re-find #"^(\d+)d" dnd)]
      (Integer/parseInt ds))))
  
(defn find-offset [title-die]
  (when title-die
    (let [dn (die-normalize title-die)
          offs (get dice-alias-offsets dn)]
      (or offs (when (re-find #"^d" dn) 0) (dec (die-start dn))))))

(defn make-parsed-item
  "Takes a map and returns another map that represents the final rollup for the current
   position in the parse of the original text."
  [m]
  (->
    ;; copy all the metadata into the new map
   (reduce (fn [mp mkw] (assoc mp mkw (mkw m))) {} metakeys)
   ;; then the normal stuff
   (assoc :title (:title m))
   (assoc :title-die (:title-die m))
   (assoc :items (:items m))
   (assoc :offset (find-offset (:title-die m)))
   (assoc :count (count (:items m)))))

(defn simple-reader
  "Has the following assumptions about a text file:
 
   The first text encountered will be the title of the random table. 
   The title may start with, or end with `\\[d\\d+\\]` to indicate the dice type, 
   even though it is implied by the number of entires, so this is not necessary.

   Each following line represents an entry in the random table.
   Each entry must be prefixed with `\\d+\\s+[.-:\\s]`
   Entries continue until EOL or a new title is encountered."
  [file]
  (with-open [rdr (io/reader file)]
    (let [everything
          (reduce
           (fn [{collecting? :collecting? items :items title :title parsed :parsed :as m} line]
             (if-let [line (->empty->nil line)]
               (let [m (add-metadata metakeys m line)]
                 ;; The order in (cond) matters, to ignore noise and the like.
                 (cond
                   ;; Add the item to the items list.
                   (and collecting? (entry? line)) 
                   (if-let [item (->empty->nil (parse-entry line))]
                     (assoc m :items 
                            (conj (or items [])
                                  {:item item}))
                     m)
                   
                   ;; a. Subentry collection and attachment to previous entry.
                   (and collecting? (subentry? line))
                   (if-let [se (->empty->nil (parse-subentry line))]
                     (if-let [pe (last items)]
                       (let [pei (or (:items pe) [])]
                         (assoc m :items
                                (replace-last
                                 items
                                 (assoc pe :items (conj pei {:item se})))))
                       m)
                     m) 
                   
                   ;; Add the title, prep for collecting.
                   (and (not collecting?) (title? line)) (if-let [title (->empty->nil (parse-title line))]
                                                           (assoc
                                                            m
                                                            :title title :collecting? true
                                                            :title-die (parse-source-die line))
                                                           m)
                   ;; Store previous and prep for next round.
                   (and collecting? (title? line)) (if-let [title (->empty->nil (parse-title line))]
                                                     (assoc m
                                                            :parsed (conj (or parsed []) (make-parsed-item m))
                                                            :items []
                                                            :fontsize nil ; reset after each round.
                                                            :title-die (parse-source-die line)
                                                            :title title)
                                                     m)
                   :default m))
               m))
           {}
           (line-seq rdr))]      
      ;; End of the file, there is probably data in the state map that needs added
      ;; to items.
      ;; 
      (if (pos? (count (:items everything)))
        (conj (or (:parsed everything) []) (make-parsed-item everything))
        (:parsed everything)))))


(comment
  
  (subentry? "d8 Damage Weapons [d12]")
  
  (-> 
   (io/file "inputexamples/rpgtalk/small.txt")
   (simple-reader)
   (first)
   (select-keys [:items])
   )
  
  )