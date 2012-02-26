(ns rdfa.test.util
  (:use midje.sweet)
  (:use [rdfa.util] :reload)
  (:import [rdfa.core IRI Literal BNode]))


(facts
  (repr-term (IRI. "http://example.org/"))
  => "<http://example.org/>"
  (repr-term (Literal. "hello" nil))
  => "\"hello\""
  (repr-term (Literal. "hello" "en"))
  => "\"hello\"@en"
  (repr-term (Literal.
               "data" (IRI. "http://example.org/ns#data")))
  => "\"data\"^^<http://example.org/ns#data>" )

