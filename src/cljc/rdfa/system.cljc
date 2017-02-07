(ns rdfa.system
  (:refer-clojure :exclude [parse-opts])
  (:require
    [mount.core :as mount :refer-macros [defstate]]
    [clojure.tools.cli :refer [parse-opts]]
    [rdfa.configuration :refer [cli-options configuration]]
    [rdfa.parser :refer [parse]]
    [rdfa.extractor :refer [extract]]
    [rdfa.serializer :refer [serialize]]
    [rdfa.ui :refer [init]]
    #?(:cljs rdfa.dom.google)
    #?(:clj rdfa.dom.jsoup)
    #?(:clj rdfa.parser.jsoup))
  #?(:clj
     (:import
       [java.net URI]
       [java.io File]))
  #?(:clj
     (:gen-class)))

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
  (init args start stop))
