(ns rdfa.main
  (:require [rdfa.core :as core]
            [rdfa.jsdom :as jsdom]
            [rdfa.repr :refer [print-result]]))

(defn ^:export get-data
  ([]
   (get-data js/document))
  ([doc]
   (let [document-element (.-documentElement doc)
         location (.-URL doc)]
     (core/extract-rdfa :html document-element location))))
