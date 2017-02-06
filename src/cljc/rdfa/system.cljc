(ns rdfa.system
  (:refer-clojure :exclude [parse-opts])
  (:require
    [mount.core :as mount :refer-macros [defstate]]
    [clojure.tools.cli :refer [parse-opts]]
    [rdfa.configuration :refer [cli-options configuration]]
    [rdfa.repr :refer [print-result]]
    [rdfa.parser :refer [parse]]
    #?(:cljs rdfa.main)
    #?(:clj rdfa.dom.w3c)
    #?(:cljs rdfa.dom.google)
    #?(:clj rdfa.parser.jsoup))
  #?(:clj
     (:gen-class))
  #?(:clj
     (:import
       [java.net URI]
       [java.io File])))

(mount/in-cljc-mode)

(defn ^:export start
  ([] (start {}))
  ([options]
    #?(:clj (reset! cli-options options))
   (mount/start)))

(defn ^:export stop []
  (mount/stop))

(defn ^:export restart
  ([] (restart {}))
  ([options]
   (stop)
   (start options)))

(def cli-args
  [["-l" "--location URL" "The url to source from."
    :parse-fn #(#?(:clj slurp :cljs str) %)]
   ["-s" "--source STRING" "The source string."]])

(defn ^:export -main [& args]
  (let [parsed-args (:options (parse-opts args cli-args))]
    #?(:clj (.addShutdownHook (Runtime/getRuntime)
                              (Thread. stop)))
    (start (:options parsed-args))
    (when-let [source (or (:file parsed-args) (:location parsed-args) (:source parsed-args))]
      (let [result (print-result (parse source))]
        #?(:clj  (do (println result)
                     (System/exit 0))
           :cljs result)))))
