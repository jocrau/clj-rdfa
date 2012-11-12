(ns rdfa.repr
  (:require rdfa.core
            [clojure.string :as string])
  (:import [rdfa.core IRI Literal BNode]))

(defn repr-term [term]
  (condp = (type term)
    IRI (str "<" (:id term) ">")
    Literal (let [{value :value tag :tag} term
                  escaped-value (string/escape value {\u0022 "\\\""})
                  quotes (if (re-find #"\n|\r|\t" escaped-value)
                           "\"\"\""
                           \")]
              (str quotes escaped-value quotes
                   (cond
                     (= (type tag) IRI) (str "^^" (repr-term tag))
                     (not-empty tag) (str "@" tag))))
    BNode (str "_:" (:id term))))

(defn repr-triple [[s p o]]
  (str (repr-term s) " " (repr-term p) " " (repr-term o) " . \n"))

(defn print-triples [triples]
  (loop [triples triples
         representation ""]
    (if (seq triples)
      (recur (rest triples)
             (str representation (repr-triple (first triples))))
      representation)))
