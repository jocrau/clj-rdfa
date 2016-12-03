(ns rdfa.utils
  (:import [java.net URI]))

(defn resolve-iri [iref base]
  (if (not-empty iref)
    ; NOTE: work around bug in java.net.URI for resolving against query string
    ; See e.g. org.apache.http.client.utils.
    ;   URIUtils#resolveReferenceStartingWithQueryString
    (if (.startsWith iref "?")
      (str (let [i (.indexOf base "?")]
             (if (> i -1) (subs base 0 i) base)) iref)
      (.. (URI. base) (resolve iref) (normalize) (toString)))
    base))

;; This (scary) regular expression matches arbritrary URLs and URIs). It was taken from http://daringfireball.net/2010/07/improved_regex_for_matching_urls.
;; Thanks to John Gruber who made this public domain.
(def iri-regex #"(?i)\b((?:[a-z][\w-]+:(?:/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'\".,<>?«»“”‘’]))")

(defn iri-string? [thing]
  (and (string? thing) (re-matches iri-regex (name thing))))
