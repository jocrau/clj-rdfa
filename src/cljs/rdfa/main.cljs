(ns rdfa.main
  (:require [rdfa.core :refer [extract-rdfa]]
            [rdfa.repr :refer [print-result]]))

(defn ^:export get-data
  ([]
   (get-data js/document))
  ([doc]
   (let [document-element (.-documentElement doc)
         location (.-URL doc)]
     (extract-rdfa :html document-element location))))
