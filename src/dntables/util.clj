(ns dntables.util
  (:require [clojure.string :as s]))


(defn is-relative? [font-size-text]
  (when font-size-text
    (some? (re-matches #"^[-+]\d+" font-size-text))))

(defn replace-last [v nval] (assoc v (dec (count v)) nval))

(defn ->empty->nil [x] (if (nil? x) nil (if (empty? (s/trim x)) nil (s/trim x))))