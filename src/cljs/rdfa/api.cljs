(ns rdfa.api
  (:require
    [rdfa.dom.google]
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
        result (-> (.-documentElement js/document)
                   (extract context)
                   (serialize context))]
    (aset (.getElementById js/document "rdf-container") "innerHTML" (string/htmlEscape result))
    nil))
