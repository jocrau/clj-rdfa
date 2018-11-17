(ns rdfa.repr-test
  (:require
    [rdfa.serializer :refer [repr-term repr-triple]]
    #?(:cljs [cljs.test :refer-macros [deftest is testing run-tests]])
    #?(:clj
    [clojure.test :refer :all])
    #?(:cljs [rdfa.rdf :refer [IRI Literal BNode]]))
  #?(:clj
     (:import [rdfa.rdf IRI Literal BNode])))

(deftest term-test
  (is (= (repr-term (IRI. "http://example.org/")) "<http://example.org/>"))
  (is (= (repr-term (Literal. "hello" nil)) "\"hello\""))
  (is (= (repr-term (Literal. "hello" "en")) "\"hello\"@en"))
  (is (= (repr-term (Literal. "data" (IRI. "http://example.org/ns#data"))) "\"data\"^^<http://example.org/ns#data>")))

(deftest triple-test
  (is (= (repr-triple [(IRI. "http://example.org/thing")
                       (IRI. "http://example.org/ns#label")
                       (Literal. "thing" nil)]) "<http://example.org/thing> <http://example.org/ns#label> \"thing\" . \n")))

