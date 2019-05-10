(ns dntables.core
  (:require [clj-pdf.core :as pdf]
            [clojure.java.io :as io]
            [dntables.parsers.text :as tp]))

(def black [0   0   0])
(def white [255 255 255])
(def grey  [179 178 178])

(def row-header black)
(def row-even grey)
(def row-odd white)

(defn make-rows [row-data]
  (map-indexed
   (fn [idx text]
     (let [bg (if (odd? idx) row-odd row-even)]
       [[:pdf-cell {:background-color bg :align :center :valign :middle}
         (str (inc idx))]
        [:pdf-cell {:background-color bg :align :left :valign :top :padding-bottom 8 :padding-top 0 :padding-left 10}
         text]]))
   row-data))

(defn make-table [{items :items title :title source :source author :author} output]
  (pdf/pdf
   [{:register-system-fonts? true
     :font {:size 12 :ttf-name "fonts/Book Antiqua.ttf"}}
    (into [:pdf-table
           {:width-percent 100
            :header [[[:pdf-cell {:background-color [0 0 0] :align :center :padding-bottom 8 :padding-left 8}
                       [:phrase {:size 15 :color [255 255 255] :ttf-name "fonts/Duvall.ttf"}
                        (str "d" (count items))]]
                      [:pdf-cell {:background-color [0 0 0] :valign :center :align :center :padding-bottom 8 :padding-left 10}
                       [:phrase {:size 15 :color [255 255 255] :ttf-name "fonts/Duvall.ttf"}
                        title]]]]}
           [8 92]]
          (make-rows items))]
   output))

(defn build-tables-from-text [file]
  (let [data (tp/simple-reader file)]
    (map
     (fn [{items :items title :title source :source author :author}]
       (into [:pdf-table
              {:width-percent 100
               :no-split-rows? true
               :keep-together? true
               :header [[[:pdf-cell {:background-color [0 0 0] :align :center :padding-bottom 8 :padding-left 8}
                          [:phrase {:size 15 :color [255 255 255] :ttf-name "fonts/Duvall.ttf"}
                           (str "d" (count items))]]
                         [:pdf-cell {:background-color [0 0 0] :valign :center :align :center :padding-bottom 8 :padding-left 10}
                          [:phrase {:size 15 :color [255 255 255] :ttf-name "fonts/Duvall.ttf"}
                           title]]]]}
              [8 92]]
             (make-rows items)))
     data)))

(defn write-tables [tables output]
  (pdf/pdf
   [{:register-system-fonts? true
     :font {:size 12 :ttf-name "fonts/Book Antiqua.ttf"}}
    tables]
   output))

(comment
  
 (write-tables
  (build-tables-from-text (io/resource "goatmansgoblet/familyweapons.txt"))
  "family-weapons-tables.pdf")
 
  (make-table
   {:source "http://www.goatmansgoblet.com/2019/04/ose-weapons-for-family-ties-by-damage.html"
    :author "Brian Richmond"
    :title "d4 Damage Weapons"
    :items ["Your [Relative]’s walking stick, from a time when walking holidays were common but equally arduous. [As club]"
            "A fine cast-iron skillet, properly seasoned and damn near impossible to dent. Your [Relative] has used it to bash in a few skulls, though hopefully you won’t need it for that. [As club]"
            "A dagger of curved longhorn ivory, allegedly won in some melee by your [Relative] . [As dagger]"
            "A boar-sticking javelin that served your [Relative] well, never missed with it. Or so they said. [As javelin]"
            "his gnarled oaken stave was given to your [Relative] by a hooded Watcher of the Wood, likely for some nefarious purpose. [As staff]"
            "Your [Relative] claims this knife was almost plunged into their belly by pagan cultists, though that’s something of their catch-all excuse. [as dagger]"
            "This long shaft of whalebone was sharpened and used for fishing when times were lean on the ship your [Relative] worked on some time ago. [As javelin]"
            "This knotted, knobby, whorled piece of wood has served your [Relative] well; both in bashing knees and bashing heads. [As club.]"
            "Your [Relative] stole this javelin from an athletic competition down past County Colm, back when they were competing in high-stakes hurling with High-Hankle. [As javelin.]"
            "This dagger is all that’s left from your [Relative], as their compatriots claim they died stabbing and gutting some great beast with the blade; hateful and defiant to the end. [As dagger.]"
            "Your [Relative] claims this crooked stave was blessed by a holy-man of “the Powers that Be” but it seems far more likely it was pilfered from some votive shrine. [As staff.]"
            "Your [Relative] made this for you when you were very young, allegedly from the wood of a thunderstruck tree. You carved little emblems into it, and now you carry it with you to more violent means. [As staff.]"]}
   "doc.pdf")
  )