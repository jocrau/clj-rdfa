(ns rdfa.dom)

(defprotocol DomAccess
  (get-name [this])
  (set-attr [this attr-name value])
  (get-attr [this attr-name])
  (get-ns-map [this])
  (is-root? [this])
  (find-by-tag [this tag])
  (get-child-elements [this])
  (get-text [this])
  (get-inner-xml [this xmlns-map lang]))
