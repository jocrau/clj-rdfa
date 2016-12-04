(ns rdfa.configuration
  (:require
    [mount.core :refer [defstate]]
    [taoensso.timbre :refer [set-config! println-appender log trace debug info warn error fatal report spy]]
    [environ.core :as environ]))

(def cli-options (atom {}))

(def parse-long #(Long/parseLong %))

(def environment-mapping
  {:rdfa-html-parser {:path [:html :parser]}})

(defn config-from-env
  "Parses the ENV variables to a Clojure map given the environment mapping"
  []
  (reduce
    (fn [config [env-var {:keys [path parse] :or {parse #(when % (str %))}}]]
      (if-let [value (environ/env env-var)]
        (assoc-in config path (parse value))))
    {}
    environment-mapping))

(defstate configuration
          :start (let [parser-cli (:parser @cli-options)
                       config-env (config-from-env)
                       config (if parser-cli
                                (assoc-in config-env [:html :parser] parser-cli)
                                config-env)]
                   (set-config! {:level     (keyword (get-in config [:logging :level] :info))
                                 :appenders {:println (println-appender {:stream :auto})}})
                   config))