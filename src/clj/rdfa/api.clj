(ns rdfa.api
  (:refer-clojure :exclude [parse-opts])
  (:require
    [clojure.tools.cli :refer [parse-opts]]
    [rdfa.parser :refer [parse]]
    [rdfa.parser.jsoup]
    [rdfa.dom.jsoup]
    [rdfa.extractor :refer [extract]]
    [rdfa.serializer :refer [serialize]]
    [rdfa.profiles :refer [detect-host-language]])
  (:gen-class))

(def cli-args
  [["-l" "--location URL" "The url to source from."]
   ["-s" "--source STRING" "The source string."]])

(defn ^:export init [args]
  (let [location (:location args)
        source (or (:source args) (slurp location))
        host-language (detect-host-language location source)
        context {:location      location
                 :host-language host-language}]
    (let [result (-> source
                     (parse context)
                     (extract context)
                     (serialize context))]
      (println result)
      (System/exit 0))))

(defn ^:export -main [& args]
  (let [parsed-args (:options (parse-opts args cli-args))]
    (init parsed-args)))
