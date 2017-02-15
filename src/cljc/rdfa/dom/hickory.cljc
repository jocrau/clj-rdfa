(ns rdfa.dom.hickory
  (:require [rdfa.dom :refer [DomAccess get-name get-attr get-ns-map is-root? find-by-tag get-child-elements get-text]]
            [clojure.string :as string])
  #?(:clj
     (:import [clojure.lang PersistentHashMap PersistentArrayMap])))

(defn- get-values [this]
  (cond
    (string? this) this
    (map? this) (map get-values (:content this))
    :else nil))

(extend-type PersistentArrayMap
  DomAccess
  (get-name [this] (when (keyword? (:tag this))
                     (name (:tag this))))
  (get-attr [this attr-name]
    (get-in this [:attrs (keyword attr-name)]))
  (get-ns-map [this]
    (into {} (map (fn [[key value]]
                    (when (string/starts-with? (name key) "xmlns:")
                      [(subs (name key) 6) value]))
                  (:attrs this))))
  (is-root? [this]
    (some-> this meta :is-root))
  (find-by-tag [this tag] (some
                            (fn [[k v]]
                              (cond (= v tag) [k]
                                    (map? v) (if-let [r (find-by-tag v tag)]
                                               (into [k] r))))
                            this))
  (get-child-elements [this] (filter map? (:content this)))
  (get-text [this] (string/join (flatten (map get-values (:content this))))))
