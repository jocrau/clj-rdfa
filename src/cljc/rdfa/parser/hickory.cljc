(ns rdfa.parser.hickory
  (:require
    [rdfa.parser :refer [parse]]
    [hickory.core :as h]))

(defn parse* [source context]
  (h/as-hickory (h/parse source)))

(defmethod parse :html [source context]
  (parse* source context))

(defmethod parse :xhtml [source context]
  (parse* source context))

(defmethod parse :xml [source context]
  (parse* source context))