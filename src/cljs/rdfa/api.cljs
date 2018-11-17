(ns rdfa.api
  (:require
    [rdfa.dom.google]
    [rdfa.dom.hickory]
    [rdfa.parser :as parser]
    [rdfa.parser.hickory]
    [rdfa.extractor :as extractor]
    [rdfa.serializer :as serializer]
    [rdfa.profiles :refer [detect-host-language]]))

(enable-console-print!)

(defn ^:export parse []
  (let [source (.-outerHTML (.-documentElement js/document))
        location (.-URL js/document)
        context {:location      location
                 :host-language (detect-host-language location nil)}]
    (-> source
        (parser/parse context)
        (extractor/extract context)
        (serializer/serialize context))))
