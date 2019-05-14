(ns dntables.parsers.text
  (:require [clojure.java.io :as io]
            [clojure.string :as s]))

(defn source? [text]
  (when text (some? (re-find #"^::source" (s/trim text)))))

(defn parse-source [text]
  (second (re-find #"^::source\s+(.*)" text)))

(defn author? [text]
  (when text (some? (re-find #"^::author" (s/trim text)))))

(defn parse-author [text]
  (second (re-find #"^::author\s+(.*)" text)))

(defn license? [text]
  (when text (some? (re-find #"^::license" (s/trim text)))))

(defn parse-license [text]
  (second (re-find #"^::license\s+(.*)" text)))

(defn fontsize? [text]
  (when text (some? (re-find #"^::fontsize\s+[-+]?\d+\s?$" (s/trim text)))))

(defn parse-fontsize [text]
  (second (re-find #"^::fontsize\s+([-+]?\d+)" text)))

(defn title? [text]
  (when text (some? (re-find #"^[\[\()]|^[A-Za-z!-.:$#@]" (s/trim text)))))

(defn parse-title [text]
  (or (second (re-find #"^[\[\(]\s?\d?\s?d\s?\d+\s?[\]\)]\s?(.*)" text))
      (second (re-find #"^(.*)[\[\(]\s?\d?\s?d\s?\d+\s?[\]\)]\s?$" text))))

(defn entry? [text]
  (when text (some? (re-find #"^\d" (s/trim text)))))

(defn parse-entry [text]
  (second (re-find #"^\d+\s?[A-Za-z]{0,1}[\s-:.=\>]+(.*)" text)))

(defn ->empty->nil [x] (if (nil? x) nil (if (empty? (s/trim x)) nil (s/trim x))))

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
               (cond
                 (source? line) (assoc m :source (->empty->nil (parse-source line)))
                 (author? line) (assoc m :author (->empty->nil (parse-author line)))
                 (license? line) (assoc m :license (->empty->nil (parse-license line)))
                 (fontsize? line) (assoc m :fontsize (->empty->nil (parse-fontsize line)))

                 ;; Add the item to the items list.
                 (and collecting? (entry? line)) (if-let [item (->empty->nil (parse-entry line))]
                                                   (assoc m :items (conj (or items []) item))
                                                   m)

                 ;; Add the title, prep for collecting.
                 (and (not collecting?) (title? line)) (if-let [title (->empty->nil (parse-title line))]
                                                         (assoc m :title title :collecting? true)
                                                         m)

                 ;; Store previous and prep for next round.
                 (and collecting? (title? line)) (if-let [title (->empty->nil (parse-title line))]
                                                   (assoc m
                                                          :parsed (conj (or parsed [])
                                                                        {:title (:title m)
                                                                         :author (:author m)
                                                                         :license (:license m)
                                                                         :fontsize (:fontsize m)
                                                                         :source (:source m)
                                                                         :count (count (:items m))
                                                                         :items (:items m)})
                                                          :items []
                                                          :fontsize nil
                                                          :title title)
                                                   m)
                 :default m)
               m))
           {}
           (line-seq rdr))]

      ;; End of the file, there is probably data in the state map that needs added
      ;; to items.
      ;; 
      (if (pos? (count (:items everything)))
        (conj (or (:parsed everything) [])
              {:title (:title everything)
               :author (:author everything)
               :source (:source everything)
               :license (or (:license everything) "")
               :fontsize (:fontsize everything)
               :count (count (:items everything))
               :items (:items everything)})
        (:parsed everything)))))


(comment
  
  (parse-title "So, we're exploring The Dread Pit of Zeiram the Lich. What's guarding the front door? (d6)")

  (parse-title "this is a title [ d 12 ]")

  (clojure.pprint/pprint
   (simple-reader (io/resource "goatmansgoblet/familyweapons.txt")))  
  )