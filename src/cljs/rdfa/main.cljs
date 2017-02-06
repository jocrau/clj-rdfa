(ns rdfa.main
  (:require
    [rdfa.core :refer [extract-rdfa]]
    [rdfa.repr :refer [print-result]]
    [goog.string :as gstring]))

(enable-console-print!)

(defn ^:export get-data
  ([]
   (get-data js/document))
  ([doc]
   (let [document-element (.-documentElement doc)
         location (.-URL doc)]
     (extract-rdfa :html document-element location))))

(defn ^:export render-rdfa [container-id]
  (let [element (.getElementById js/document container-id)
        data (get-data)]
    (aset element "innerHTML" (gstring/htmlEscape (print-result data)))
    nil))

(defn ^:export reload-rdfa []
  (render-rdfa "rdf-container"))
