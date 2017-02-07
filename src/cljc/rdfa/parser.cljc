(ns rdfa.parser
  (:require
    [taoensso.timbre :refer [log trace debug info warn error fatal report]]
    [rdfa.profiles :refer [detect-host-language]]))

(defmulti parse (fn [source context] (:host-language context)))

(defmethod parse nil [source context]
  (warn "No parser found"))
