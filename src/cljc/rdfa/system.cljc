(ns rdfa.system
  (:refer-clojure :exclude [parse-opts])
  (:require
    [mount.core :as mount :refer-macros [defstate]]
    [clojure.tools.cli :refer [parse-opts]]
    [rdfa.configuration :refer [cli-options configuration]]
    [rdfa.repr :refer [print-result]]
    [rdfa.parser :refer [parse]]
    #?(:cljs [rdfa.main])
    #?(:clj
    rdfa.parser.jsoup))
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
  [["-p" "--parser PARSER" "The HTML parser (jsoup is currently the only option and therefore the default)."
    :parse-fn keyword
    :default :jsoup
    :validate [#(some % [:jsoup]) "'jsoup' is the only option at the moment."]]
   ["-l" "--location URL" "The url to source from."
    :parse-fn #(#?(:clj URI. :cljs str) %)]
   ["-s" "--source STRING" "The source string."]])

(defn ^:export -main [& args]
  (let [cli-options (:options (parse-opts args cli-args))]
    #?(:clj (.addShutdownHook (Runtime/getRuntime)
                              (Thread. stop)))
    (start (:options cli-options))
    (let [config @configuration]
      (when-let [source (or (:file cli-options) (:location cli-options) (:source cli-options))]
        (let [result (print-result (parse source))]
          #?(:clj (do (println result)
                      (System/exit 0))
             :cljs result))))))
