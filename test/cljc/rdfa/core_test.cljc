(ns rdfa.core-test
  (:require
    [rdfa.extractor :refer [init-env expand-term-or-curie to-curie-or-iri parse-prefix next-bnode]]
    #?(:cljs [cljs.test :refer-macros [deftest is testing run-tests]])
    #?(:clj
    [clojure.test :refer :all])
    #?(:cljs [rdfa.rdf :refer [IRI Literal BNode]]))
  #?(:clj
     (:import [rdfa.rdf IRI Literal BNode])))

(def env (init-env "./"
                   {:prefix-map {"ns" "http://example.org/ns#"}
                    :term-map   {"role" "http://example.org/ns#role"}
                    :vocab      nil}))

(def env-w-vocab (assoc env :vocab "http://example.org/vocab#"))

(deftest base-test
  (is (= (:base (init-env "path#frag" {})) "path")))

(deftest expand-term-or-curie-test
  (is (= (expand-term-or-curie env "ns:name") [(IRI. "http://example.org/ns#name") nil]))
  (is (= (expand-term-or-curie env "ns:") [(IRI. "http://example.org/ns#") nil]))
  (is (= (expand-term-or-curie env "ns:name:first") [(IRI. "http://example.org/ns#name:first") nil]))
  (is (= (expand-term-or-curie env "ns:/name") [(IRI. "http://example.org/ns#/name") nil]))
  (is (= (expand-term-or-curie env "ns://name") [nil {:malformed-curie "ns://name"}]))
  (is (= (expand-term-or-curie env "[ns:name]") [(IRI. "http://example.org/ns#name") nil]))
  (is (= (expand-term-or-curie env "[unknown:name]") [nil {:undefined-prefix "unknown"}]))
  (is (= (expand-term-or-curie env "_:a") [(BNode. "a") nil]))
  (is (= (expand-term-or-curie env "role") [(IRI. "http://example.org/ns#role") nil]))
  (is (= (expand-term-or-curie env "other") [nil {:undefined-term "other"}]))
  (is (= (expand-term-or-curie env-w-vocab "Role") [(IRI. "http://example.org/vocab#Role") nil]))
  (is (= (expand-term-or-curie env-w-vocab "role") [(IRI. "http://example.org/vocab#role") nil]))
  (is (= (expand-term-or-curie env-w-vocab "other") [(IRI. "http://example.org/vocab#other") nil]))
  (is (= (to-curie-or-iri env-w-vocab "other") [(IRI. "other") nil])))


(deftest prefix-test
  (is (= (parse-prefix "ns: http://example.org/ns#") {"ns" "http://example.org/ns#"}))
  (is (= (parse-prefix "  ns:   http://example.org/ns#
    voc: http://example.org/vocab#  ") {"ns" "http://example.org/ns#", "voc" "http://example.org/vocab#"}))
  (is (= (parse-prefix "  ns:   http://example.org/ns#
                  $invalid: http://example.org/any#
                  voc: http://example.org/vocab#  ") {"ns" "http://example.org/ns#", "voc" "http://example.org/vocab#"}))

  (is (= (parse-prefix "") nil)))

(deftest bnodes-test
  (is (not= (next-bnode) (next-bnode))))

