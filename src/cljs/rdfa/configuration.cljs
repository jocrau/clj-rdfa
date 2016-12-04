(ns rdfa.configuration
  (:require
    [mount.core :refer [defstate]]))

(def cli-options (atom {}))

(defstate configuration
          :start {:html {:parser :jsparser}})