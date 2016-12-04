(ns rdfa.repr
  (:require
    rdfa.core
    #?(:cljs [rdfa.core :refer [IRI Literal BNode]])
    [clojure.string :as string])
  #?(:clj
     (:import [rdfa.core IRI Literal BNode])))

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

(defn ^:export print-result [{:keys [env triples proc-triples]}]
  (str (print-prefixes env)
       (print-triples triples)
       (print-triples proc-triples)))