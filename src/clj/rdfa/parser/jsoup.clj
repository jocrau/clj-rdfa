(ns rdfa.parser.jsoup
  (:require
    [rdfa.parser :refer :all]
    [clojure.string :refer [join split]]
    [rdfa.extractor :refer [extract error-results]]
    [rdfa.profiles :refer [detect-host-language]])
  (:import
    [org.jsoup Jsoup]
    [java.net URI]))

(extend-type String
  Parser
  (parse
    ([source] (parse source {}))
    ([source {:keys [profile location]
              :or   {location ""
                     profile  (detect-host-language :location (str source))}
              :as   options}]
     (try
       (Jsoup/parse source location)
       (catch Exception e
         (error-results (.getMessage e) "en"))))))

(extend-type URI
  Parser
  (parse
    ([source] (parse source {}))
    ([source {:keys [profile location]
              :or   {location (str source)
                     profile  (detect-host-language :location (str source))}
              :as   options}]
     (try
       (.get (Jsoup/connect (str source)))
       (catch Exception e
         (error-results (.getMessage e) "en"))))))
