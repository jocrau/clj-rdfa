(ns rdfa.repr
  (:require rdfa.core
            [clojure.string :as string])
  (:import [rdfa.core IRI Literal BNode]))

(defn repr-term [term]
  (condp = (type term)
    rdfa.core.IRI (str "<" (:id term) ">")
    rdfa.core.Literal (let [{value :value tag :tag} term
                  escaped-value (string/escape value {\u0022 "\\\""})
                  quotes (if (re-find #"\n|\r|\t" escaped-value)
                           "\"\"\""
                           \")]
              (str quotes escaped-value quotes
                   (cond
                     (= (type tag) IRI) (str "^^" (repr-term tag))
                     (not-empty tag) (str "@" tag))))
    rdfa.core.BNode (str "_:" (:id term))))

(defn repr-triple [[s p o]]
  (str (repr-term s) " " (repr-term p) " " (repr-term o) " . \n"))

(defn print-triples [triples]
  (loop [triples triples
         representation ""]
    (if (seq triples)
      (recur (rest triples)
             (str representation (repr-triple (first triples))))
      representation)))

(defn print-prefix [pfx iri]
  (str "@prefix " pfx ": <" iri "> ."))

; TODO: would be more useful if env contained data about *used* prefixes.
(defn print-prefixes [{prefix-map :prefix-map vocab :vocab}]
  (str (apply str
              (interpose "\n"
                         (map (fn [[pfx iri]]
                                (print-prefix pfx iri))
                              prefix-map)))
       "\n"))
