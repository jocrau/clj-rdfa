(ns rdfa.api
  (:refer-clojure :exclude [parse-opts])
  (:require
    [clojure.tools.cli :refer [parse-opts]]
    [rdfa.parser :refer [parse]]
    #_[rdfa.parser.jsoup]
    [rdfa.parser.hickory]
    [rdfa.dom.jsoup]
    [rdfa.dom.hickory]
    [rdfa.extractor :refer [extract]]
    [rdfa.serializer :refer [serialize]]
    [rdfa.profiles :refer [detect-host-language]])
  (:gen-class))

(def cli-args
  [["-l" "--location URL" "The url to source from."]
   ["-s" "--source STRING" "The source string."]])

(defn ^:export init [args]
  (let [location (:location args)
        host-language (detect-host-language location source)
        context {:location      location
                 :host-language host-language}
        source (or (:source args) (slurp location))
        result (-> source
                   (parse context)
                   (extract context))]
    (doseq [triple (:triples result)]
      (println triple)
      (->> triple meta :element println))
    (println (serialize result context))
    (System/exit 0)))

(defn ^:export -main [& args]
  (let [parsed-args (:options (parse-opts args cli-args))]
    (init parsed-args)))

(comment
  (parse (slurp "file:///Users/jocrau/dev/workspaces/clj-rdfa/dev-resources/bake.html")
         {:location      "http://example.com/"
          :host-language :html})
  )