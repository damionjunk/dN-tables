(ns dntables.util)


(defn is-relative? [font-size-text]
  (when font-size-text
    (some? (re-matches #"^[-+]\d+" font-size-text))))
