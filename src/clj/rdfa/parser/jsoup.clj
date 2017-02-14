(ns rdfa.parser.jsoup
  (:require
    [rdfa.parser :refer :all])
  (:import
    [org.jsoup Jsoup]))

(defn parse* [source context]
  (Jsoup/parse source (:location context)))

(defmethod parse :html [source context]
  (parse* source context))

(defmethod parse :xhtml [source context]
  (parse* source context))

(defmethod parse :xml [source context]
  (parse* source context))