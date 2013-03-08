(ns rdfa.parser.jsoup
  (:use rdfa.parser
        rdfa.dom
        [clojure.string :only (join split)])
  (:require rdfa.core)
  (:import [org.jsoup Jsoup]
           [org.jsoup.select Elements]
           [org.jsoup.nodes Node Attribute Attributes Comment DataNode Document Element TextNode]))

(extend-type Node
  DomAccess
  (get-name [this] (.nodeName this))
  (get-attr [this attr-name] (.attr this attr-name))
  (get-ns-map [this]
    (reduce
      (fn [ns-map attr]
        (condp #(.startsWith %1 %2) (.getKey attr)
          "xmlns:" (assoc ns-map (.substring (.getKey attr) 6) (.getValue attr))
          "prefix" (into ns-map
                          (reduce 
                            (fn [inner-ns-map mapping] (assoc inner-ns-map (apply str (drop-last (first mapping))) (second mapping)))
                            {} (partition 2 (split (.getValue attr) #"\s+"))))
          ns-map))
      {} (.attributes this)))
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
                   profile (rdfa.profiles/detect-host-language :location location)}}]
      (try
        (rdfa.core/extract-rdfa profile (Jsoup/parse (slurp source)) location)
        (catch Exception e
          (rdfa.core/error-results (.getMessage e) "en"))))))
