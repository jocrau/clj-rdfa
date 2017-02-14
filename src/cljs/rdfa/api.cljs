(ns rdfa.api
  (:require
    [rdfa.dom.google]
    [rdfa.dom.hickory]
    [rdfa.parser :refer [parse]]
    [rdfa.parser.hickory]
    [rdfa.extractor :refer [extract]]
    [rdfa.serializer :refer [serialize]]
    [rdfa.profiles :refer [detect-host-language]]
    [goog.string :as string]))

(enable-console-print!)

(defn ^:export init []
  (let [location (.-URL js/document)
        host-language (detect-host-language location nil)
        context {:location      location
                 :host-language host-language}
        source (.-outerHTML (.-documentElement js/document))
        result (-> source
                   (parse context)
                   (extract context))]
    (doseq [triple (:triples result)]
      (println triple)
      (->> triple meta :element println))
    (println (serialize result context))
    nil))
