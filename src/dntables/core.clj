(ns dntables.core
  (:require [clj-pdf.core :as pdf]
            [clojure.java.io :as io]
            [dntables.parsers.text :as tp]))

(def black     [0   0   0])
(def white     [255 255 255])
(def grey      [209 209 209])

(def row-header black)
(def row-even grey)
(def row-odd white)
(def heading-font "fonts/Book Antiqua.ttf")
(def normal-font "fonts/ANTQUA.ttf")

(defn is-relative? [font-size-text]
  (when font-size-text
    (some? (re-matches #"^[-+]\d+" font-size-text))))

(defn make-rows [row-data & {:keys [base-size font-size relative?]}]
  (map-indexed
   (fn [idx text]
     (let [bg (if (odd? idx) row-odd row-even)]
       [[:pdf-cell {:background-color bg :align :center :valign :middle}
         [:paragraph (when (and font-size base-size) {:size (if relative? 
                                                              (+ base-size (Integer/parseInt font-size))
                                                              (Integer/parseInt font-size))})
          (str (inc idx))]]
        [:pdf-cell {:background-color bg :align :left :valign :top :padding-bottom 8 :padding-top 0 :padding-left 10}
         [:paragraph (when (and font-size base-size) {:size (if relative?
                                                              (+ base-size (Integer/parseInt font-size))
                                                              (Integer/parseInt font-size))}) text]]]))
   row-data))

(defn build-tables-from-text [file & {:keys [header-font footer-font table-font] 
                                      :or {header-font {:size 13 :color white :ttf-name heading-font}
                                           table-font  {:size 10 :ttf-name normal-font}
                                           footer-font {:size 8 :leading 10 :color white :align :center}}}]
  (let [data (tp/simple-reader file)]
    (map
     (fn [{items :items title :title source :source author :author license :license fontsize :fontsize}]
       (into [:pdf-table
              {:width-percent 100
               :no-split-rows? true
               :keep-together? true
               :cell-border false
               :header [[[:pdf-cell {:background-color black :valign :middle :align :center :padding-bottom 8 :padding-left 8}
                          [:phrase header-font
                           (str "d" (count items))]]
                         [:pdf-cell {:background-color black :valign :middle :align :center :padding-bottom 8 :padding-left 10}
                          [:phrase header-font
                           title]]]]
               :footer [
                        [[:pdf-cell {:padding-bottom 5 :padding-top 0 :colspan 2 :background-color black :align :center :valign :middle}
                          [:paragraph footer-font (str "Author: " author ";  License: " license "\n" source)]]]]
               }
              [8 92]]
             (make-rows items :base-size (:size table-font) :font-size fontsize :relative? (is-relative? fontsize)))
       )
     data)))

(defn write-tables [tables output & {:keys [image-sep
                                            body-font] 
                                     :or {image-sep false
                                          body-font {:size 10 :ttf-name normal-font}}}]
  (pdf/pdf 
   [{:register-system-fonts? true
     :font body-font}
    (if image-sep
      (interpose
       [:paragraph {:spacing-before 50 :spacing-after 60} [:image "resources/map.png"]]
       tables)
      tables)
    ]
   output))

(comment

  (write-tables
   (build-tables-from-text (io/resource "goatmansgoblet/familyweapons.txt"))
   "family-weapons-tables.pdf")

  (write-tables
   (build-tables-from-text (io/resource "dwdiscord/d6d.txt"))
   "d6d.pdf")
  
  )