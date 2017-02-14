(ns rdfa.parser
  (:require [taoensso.timbre :refer [warn]]))

(defmulti parse (fn [source context] (:host-language context)))

(defmethod parse nil [source context] (warn "No parser found"))
