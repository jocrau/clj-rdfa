(ns rdfa.dom.google
  (:require
    [rdfa.dom :refer [DomAccess get-name get-attr get-ns-map is-root? find-by-tag get-child-elements get-text]]
    [clojure.string :as string]
    [goog.dom :as gdom]))

(defn- node-list [nl]
  (if-not (nil? nl)
    (loop [index (dec (.-length nl)) nodes nil]
      (if (= index -1) nodes
                       (recur (dec index) (cons (.item nl index) nodes))))))

(defn- serialize-to-xml [node]
  (try (.serializeToString (js/XMLSerializer.) node)
       (catch js/Exception e
         (try (.-xml node)
              (catch js/Exception e)))))

(defn- get-values [node]
  (cons (if (= (.-nodeType node) js/Node.TEXT_NODE)
          (.-nodeValue node))
        (map get-values (node-list (.-childNodes node)))))

(extend-type js/Node
  DomAccess
  rdfa.dom/DomAccess
  (get-name [this] (.-nodeName this))
  (get-attr [this attr-name]
    (if (and (.-hasAttribute this)
             (.hasAttribute this attr-name))
      (.getAttribute this attr-name)))
  (get-ns-map [this] (into {} (for [attr (node-list (.-attributes this))
                                    :when (= (subs (get-name attr) 0 6) "xmlns:")]
                                [(subs (get-name attr) 6) (.-value attr)])))
  (is-root? [this]
    (if-let [owner-document (.-ownerDocument this)]
      (= this (.-documentElement owner-document))))
  (find-by-tag [this tag]
    (node-list (.getElementsByTagName this tag)))
  (get-child-elements [this]
    (filter #(= (.-nodeType %1) js/Node.ELEMENT_NODE)
            (node-list (.-childNodes this))))
  (get-text [this]
    (string/join (flatten (get-values this))))
  (get-inner-xml
    [this xmlns-map lang]
    (loop [nodes (node-list (.-childNodes this))
           frag (gdom/htmlToDocumentFragment "")]
      (if (seq nodes)
        (recur
          (rest nodes)
          (let [node (first nodes)]
            (do
              (if (= (.-nodeType node) js/Node.ELEMENT_NODE)
                (do
                  (if (not-empty lang)
                    (.setAttribute node "xml:lang" lang))
                  (doseq [[pfx iri] xmlns-map]
                    (let [qname (str "xmlns" (if pfx \:) pfx)]
                      (.setAttribute node qname iri)))
                  (.appendChild frag node)))
              frag)))
        (serialize-to-xml frag)))))
