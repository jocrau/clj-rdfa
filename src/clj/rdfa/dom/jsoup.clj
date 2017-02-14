(ns rdfa.dom.jsoup
  (:require [rdfa.dom :refer :all]
            [clojure.string :as string])
  (:import [org.jsoup.nodes Node Element]))

(extend-type Node
  DomAccess
  (get-name [this] (.nodeName this))
  (get-attr [this attr-name] (when (.hasAttr this attr-name) (.attr this attr-name)))
  (get-ns-map [this]
    (into {} (map (fn [[key value]]
                    (when (string/starts-with? (name key) "xmlns:")
                      [(subs (name key) 6) value]))
                  (.attributes this))))
  (is-root? [this] (= (.ownerDocument this) this))
  (find-by-tag [this tag] (.getElementsByTag this tag))
  (get-child-elements [this] (filter #(= (type %) Element) (.children this)))
  (get-text [this] (.text this)))
