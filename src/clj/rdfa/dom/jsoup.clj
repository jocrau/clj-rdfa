(ns rdfa.dom.jsoup
  (:require [rdfa.dom :refer :all])
  (:import [org.jsoup.nodes Node Element]))

(extend-type Node
  DomAccess
  (get-name [this] (.nodeName this))
  (get-attr [this attr-name] (when (.hasAttr this attr-name) (.attr this attr-name)))
  (get-ns-map [this]
    (into {} (map #(when (.startsWith (.getKey %) "xmlns:")
                     [(.substring (.getKey %) 6) (.getValue %)])
                  (.attributes this))))
  (is-root? [this] (= (.ownerDocument this) this))
  (find-by-tag [this tag] (.getElementsByTag this tag))
  (get-child-elements [this] (filter #(= (type %) Element) (.children this)))
  (get-text [this] (.text this)))
