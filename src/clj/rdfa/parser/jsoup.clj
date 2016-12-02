(ns rdfa.parser.jsoup
  (:use rdfa.parser
        rdfa.dom)
  (:require [clojure.string :refer [join split]]
            [rdfa.core :refer [extract-rdfa error-results]]
            [rdfa.profiles :refer [detect-host-language]])
  (:import [java.net URI]
           [org.jsoup Jsoup]
           [org.jsoup.nodes Node Element]))

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
              :or   {location (str source)
                     profile  (detect-host-language :location (str source))}}]
     (try
       (let [string (.get (Jsoup/connect (str source)))]
         (extract-rdfa profile string location))
       (catch Exception e
         (error-results (.getMessage e) "en"))))))
