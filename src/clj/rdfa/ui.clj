(ns rdfa.ui
  (:refer-clojure :exclude [parse-opts])
  (:require
    [clojure.tools.cli :refer [parse-opts]]
    [rdfa.configuration :refer [cli-options configuration]]
    [rdfa.parser :refer [parse]]
    [rdfa.extractor :refer [extract]]
    [rdfa.serializer :refer [serialize]]
    [rdfa.profiles :refer [detect-host-language]]))

(def cli-args
  [["-l" "--location URL" "The url to source from."
    :parse-fn #(slurp %)]
   ["-s" "--source STRING" "The source string."]])

(defn ^:export init [args start stop]
  (let [parsed-args (:options (parse-opts args cli-args))]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. stop))
    (start (:options parsed-args))
    (when-let [source (or (:file parsed-args) (:location parsed-args) (:source parsed-args))]
      (let [result (serialize (extract (parse source)))]
        (println result)
        (System/exit 0)))))
