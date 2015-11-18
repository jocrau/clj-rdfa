(ns rdfa.parser.jsoup
  (:use rdfa.parser
        rdfa.dom
        [clojure.string :only (join split)])
  (:require rdfa.core)
  (:import [java.net URI]
           [org.jsoup Jsoup]
           [org.jsoup.select Elements]
           [org.jsoup.nodes Node Attribute Attributes Comment DataNode Document Element TextNode]))

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

(extend-type String
  Parser
  (get-rdfa
    ([source] (get-rdfa source {}))
    ([source {:keys [profile location]
              :or {location source
                   profile (rdfa.profiles/detect-host-language :location source)}}]
      (try
        (try
          (rdfa.core/extract-rdfa profile (.get (Jsoup/connect (str (java.net.URI. source)))) location)
          (catch Exception e
            (rdfa.core/extract-rdfa profile (Jsoup/parse source) location)))
        (catch Exception e
          (rdfa.core/error-results (.getMessage e) "en"))))))
