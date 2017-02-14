(ns rdfa.rdf)

(defrecord BNode [id])
(defrecord IRI [id])
(defrecord Literal [value tag])
