(ns rdfa.api
  (:refer-clojure :exclude [parse-opts])
  (:require
    [clojure.tools.cli :refer [parse-opts]]
    [datascript.core :as d]
    [net.cgrand.enlive-html :as html]
    [rdfa.parser :as parser]
    [rdfa.parser.hickory]
    [rdfa.dom.hickory]
    [rdfa.extractor :as extractor]
    [rdfa.serializer :as serializer]
    [rdfa.profiles :as profiles])
  (:gen-class))

(def schema
  {:child {:db/valueType   :db.type/ref
           :db/cardinality :db.cardinality/many}})

(def conn (d/create-conn schema))

(def cli-args
  [["-l" "--location URL" "The url to source from."]
   ["-s" "--source STRING" "The source string."]])

(defn ^:export init [args]
  (let [location (:location args)
        source (or (:source args) (slurp location))
        host-language (profiles/detect-host-language location source)
        context {:location      location
                 :host-language host-language}]
    (-> source
        (parser/parse context)
        (extractor/extract context)
        (serializer/serialize context)
        (println))
    (System/exit 0)))

(defn ^:export -main [& args]
  (let [args (:options (parse-opts args cli-args))]
    (init args)))

(defn persist [result]
  (let [transactions (map (fn [triple]
                            (let [[s p o] triple]
                              (merge {:s (or (:id s) (pr-str s))
                                      :p (or (:id p) (pr-str p))
                                      :o (or (:id o) (pr-str o))}
                                     (when-let [e (-> triple meta :element)]
                                       {:e e}))))
                          (:triples result))]
    (d/transact conn transactions)))

(comment

  (let [context {:location      "http://example.com/"
                 :host-language :html}]
    (-> (slurp "./resources/bake.html")
        (parse context)
        (extract context)
        (persist))
    :done)

  (d/q '[:find ?s ?p ?o ?e
         :where
         [?node :s ?s]
         [?node :p ?p]
         [?node :o ?o]
         [?node :e ?e]]
       @conn)

  (let [source (d/q '[:find ?e
                      :where
                      [?node :e ?e]
                      [?node :o "http://schema.org/NutritionInformation"]]
                    @conn)]
    (->> ((html/snippet* source []
                         [(html/attr= :property "http://schema.org/proteinContent")] (html/content "sdfsdfa")))
         (html/emit*)
         (apply str)
         println))

  )