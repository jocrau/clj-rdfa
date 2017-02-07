(ns rdfa.ui
  (:require
    [rdfa.extractor :refer [extract]]
    [rdfa.serializer :refer [serialize]]
    [goog.string :as string]))

(enable-console-print!)

(defn ^:export get-data [source]
  (extract (.-documentElement source) {:location (.-URL source)}))

(defn ^:export render-rdfa [container-id]
  (let [element (.getElementById js/document container-id)
        data (get-data js/document)]
    (aset element "innerHTML" (string/htmlEscape (serialize data)))
    nil))

(defn ^:export init
  ([] (render-rdfa "rdf-container"))
  ([args start stop]
   (render-rdfa "rdf-container")))
