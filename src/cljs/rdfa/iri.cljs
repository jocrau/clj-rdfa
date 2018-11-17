(ns rdfa.iri)

(defn resolve-iri [iref base]
  (if (not-empty iref)
    (.. (js/goog.Uri. base) (resolve (js/goog.Uri. iref)) (toString))
    base))