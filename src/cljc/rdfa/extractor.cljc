(ns rdfa.extractor
  (:require
    [clojure.string :as string]
    #?(:clj
    [net.cgrand.enlive-html :as html])
    #?(:cljs [kioo.reagent :as html])
    [rdfa.dom :as dom]
    [rdfa.dom.hickory]
    #?(:clj
    [rdfa.dom.jsoup])
    #?(:cljs [rdfa.dom.google])
    #?(:clj
    [rdfa.rdf])
    #?(:cljs [rdfa.rdf :refer [IRI BNode Literal]])
    [rdfa.profiles :refer [detect-host-language extended-data get-host-env]]
    [rdfa.iri :refer [resolve-iri]])
  #?(:clj
     (:import
       [rdfa.rdf IRI BNode Literal])))

(def gen-bnode-prefix "GEN")

(def bnode-counter (atom 0))

(defn next-bnode []
  (BNode. (str gen-bnode-prefix
               (swap! bnode-counter inc))))

(let [rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#"]
  (def rdf:type (IRI. (str rdf "type")))
  (def rdf:XMLLiteral (IRI. (str rdf "XMLLiteral")))
  (def rdf:first (IRI. (str rdf "first")))
  (def rdf:rest (IRI. (str rdf "rest")))
  (def rdf:nil (IRI. (str rdf "nil"))))

(let [rdfa "http://www.w3.org/ns/rdfa#"]
  (def rdfa:usesVocabulary (IRI. (str rdfa "usesVocabulary")))
  (def rdfa:Error (IRI. (str rdfa "Error")))
  (def rdfa:Warning (IRI. (str rdfa "Warning")))
  (def dc:description (IRI. "http://purl.org/dc/terms/description")))

(def xhv "http://www.w3.org/1999/xhtml/vocab#")

(defn to-iri [s base]
  (IRI. (resolve-iri s base)))

(defn parse-safe-curie [repr]
  (let [match (re-matches #"^\[(.*?)\]$" repr)]
    [(if match (match 1) repr) (vector? match)]))

(defn expand-term-or-curie [{:keys [base prefix-map term-map vocab]} repr]
  (let [[repr is-safe] (parse-safe-curie repr)]
    (cond
      (empty? repr)
      [nil nil]

      (> (.indexOf repr ":") -1)
      (let [[pfx term] (string/split repr #":" 2)
            is-empty (empty? pfx)
            is-bnode (if-not is-empty (= pfx "_"))
            pfx-vocab (if-not is-bnode (prefix-map pfx))]
        (cond
          (and (or pfx-vocab is-bnode is-bnode)
               (= (take 2 term) '(\/ \/)))
          [nil {:malformed-curie repr}]

          is-empty
          [(IRI. (str xhv term)) nil]

          is-bnode
          [(BNode. (or (not-empty term) gen-bnode-prefix)) nil]

          pfx-vocab
          [(IRI. (str pfx-vocab term)) nil]

          :else
          (if-let [iri (if-not is-safe (to-iri repr ""))]
            [iri nil]
            [nil {:undefined-prefix pfx}])))

      (not-empty vocab)
      [(to-iri (str vocab repr) base) nil]

      (nil? term-map)
      [nil nil]

      :else
      (if-let [iri (or (term-map repr)
                       (term-map (string/lower-case repr)))]
        [(to-iri iri base) nil]
        [nil {:undefined-term repr}]))))

(defn to-curie-or-iri [env repr]
  (let [is-safe (= (first repr) \[)
        [iri err] (expand-term-or-curie (select-keys env [:base :prefix-map]) repr)
        res (or iri (if-not is-safe (to-iri repr (:base env))))]
    [res err]))

(defn to-nodes [env expr]
  (if (not-empty expr)
    (let [tokens (string/split (string/trim expr) #"\s+")
          coll (map #(expand-term-or-curie env %) tokens)
          nodes (keep #(first %) coll)
          errs (keep #(second %) coll)]
      [nodes errs])))

(defn parse-prefix [prefix]
  (if (empty? prefix) nil
                      (let [initial (apply hash-map
                                           (string/split (string/trim prefix) #":?\s+"))
                            valid-prefix? #(re-matches #"^[\w_][\w_\-\.]*$" %1)]
                        (select-keys initial (filter valid-prefix? (keys initial))))))

(defn init-env
  [location {prefix-map :prefix-map
             term-map   :term-map
             vocab      :vocab
             base       :base
             profile    :profile}]
  (let [base (or base location)
        base (let [i (.indexOf base "#")] (if (> i -1) (subs base 0 i) base))]
    {:profile            profile
     :base               base
     :parent-object      (IRI. base)
     :incomplete         {}
     :incomplete-subject nil
     :list-map           {}
     :lang               nil
     :xmlns-map          nil
     :prefix-map         prefix-map
     :prefixes           #{}
     :term-map           term-map
     :vocab              vocab}))

(defn get-data [el]
  (let [attr #(dom/get-attr el %1)
        xmlns-map (dom/get-ns-map el)
        prefix-map (parse-prefix (attr "prefix"))]
    {:element    el
     :is-root    (dom/is-root? el)
     :xmlns-map  (if-let [xmlns (attr "xmlns")]
                   (assoc xmlns-map nil xmlns) xmlns-map)
     :prefix-map (merge xmlns-map prefix-map)
     :vocab      (attr "vocab")
     :base       nil
     :about      (attr "about")
     :property   (attr "property")
     :rel        (attr "rel")
     :rev        (attr "rev")
     :resources  (remove nil? [(attr "resource") (attr "href") (attr "src")])
     :typeof     (attr "typeof")
     :inlist     (attr "inlist")
     :lang       (or (attr "xml:lang") (attr "lang"))
     :content    (attr "content")
     :datatype   (attr "datatype")}))

(defn update-mappings [env data]
  (let [env (if-let [base (data :base)]
              (assoc env :base base)
              env)
        env (if-let [lang (data :lang)]
              (assoc env :lang lang)
              env)
        env (update-in env [:xmlns-map]
                       #(merge %1 (data :xmlns-map)))
        env (update-in env [:prefix-map]
                       #(merge %1 (data :prefix-map)))
        env (if-let [vocab (data :vocab)]
              (assoc env :vocab (if (empty? vocab) nil
                                                   (resolve-iri vocab (env :base))))
              env)]
    env))

(defn get-resolved-resource [env candidates]
  ; TODO: collect all errs?
  (let [resolved (map #(to-curie-or-iri env %) candidates)]
    (or (first (filter #(first %1) resolved))
        (first resolved))))

(defn get-subject [env data]
  (let [new-pred (or (data :rel) (data :rev) (data :property))
        about-and-err (if-let [about (data :about)]
                        (to-curie-or-iri env about))
        resource-and-err (get-resolved-resource env (data :resources))
        use-resource (and (not (first about-and-err))
                          (or (and (data :property)
                                   (or (data :content) (data :datatype)))
                              (not new-pred)))
        subject (or (if use-resource resource-and-err about-and-err)
                    (if (data :is-root) [nil nil]))]
    (cond
      subject subject

      (and (data :typeof) (not new-pred)
           (not (first resource-and-err)))
      [(next-bnode) nil])))

(defn get-literal [env data]
  (let [el (data :element)
        as-literal (and (data :property)
                        (or
                          (and (data :datatype)
                               (not (or (data :rel) (data :rev))))
                          (or (data :rel) (data :rev))
                          (not (or (not-empty (data :resources))
                                   (and (data :typeof)
                                        (not (data :about)))))))
        [datatype dt-err] (when-let [dt (not-empty (data :datatype))]
                            (expand-term-or-curie env dt))
        as-xml (= datatype rdf:XMLLiteral)
        repr (or (data :content)
                 (if as-literal (if as-xml
                                  (dom/get-inner-xml el (env :xmlns-map) (env :lang))
                                  (dom/get-text el))))
        literal (when repr
                  (Literal. repr (or datatype
                                     (or (data :lang) (env :lang)))))]
    [literal dt-err]))

(defn get-object [env data]
  (let [link (or (data :rel) (data :rev))
        prop (data :property)
        typeof (data :typeof)
        resources (data :resources)]
    (cond
      (and (not link) prop
           (or (data :content) (data :datatype)))
      nil

      (not-empty resources)
      (get-resolved-resource env resources)

      (or (and link (not (data :about)) typeof) (and prop typeof))
      [(next-bnode) nil])))

(defn get-props-rels-revs-lists [env data]
  (let [inlist (data :inlist)
        to-predicates (fn [repr]
                        (filter #(not= (type (first %1)) BNode)
                                (to-nodes env repr)))
        [props prop-errs] (to-predicates (data :property))
        [rels rel-errs] (to-predicates (data :rel))
        [revs rev-errs] (to-predicates (data :rev))
        errs (concat prop-errs rel-errs rev-errs)]
    [(if inlist
       [nil nil revs (or props rels)]
       [props rels revs nil]) errs]))

(defn parse-element [parent-env el]
  (let [data (extended-data parent-env (get-data el))
        env (update-mappings parent-env data)
        [subject s-err] (get-subject env data)
        [[props rels revs list-ps]
         p-errs] (get-props-rels-revs-lists env data)
        [object-resource o-err] (get-object env data)
        [object-literal dt-err] (get-literal env data)
        [types t-errs] (if-let [typeof (:typeof data)] (to-nodes env typeof))
        errs (concat (remove nil? [s-err o-err dt-err]) p-errs t-errs)]
    [subject types object-resource object-literal
     props rels revs list-ps
     env data errs]))

(defn get-hanging [data]
  (if (and (or (data :rel) (data :rev))
           (empty? (data :resources))
           (or (data :about) (not (data :typeof)))
           (not (data :inlist)))
    (next-bnode)))

(defn expand-attr [env attr]
  (fn [el]
    (update-in el [:attrs attr] (fn [value]
                                  (let [[iris err] (to-nodes env value)]
                                    (string/join " " (map :id iris)))))))

(defn expand-curies [env el]
  (condp some (-> el :attrs keys set)
    #{:property} ((expand-attr env :property) el)
    #{:typeof} ((expand-attr env :typeof) el)
    el))

(defn- create-triple [env el [s p o]]
  (let [element (expand-curies env el)]
    (with-meta [s p o] {:element element})))

(defn create-warning-triples [env el warning]
  (let [warn-node (next-bnode)
        descr (Literal. (str warning) "en")]
    [(create-triple env el [warn-node rdf:type rdfa:Warning])
     (create-triple env el [warn-node dc:description descr])]))

(defn process-element [parent-env el]
  (let [[subject types o-resource o-literal
         props rels revs list-ps
         env data errs] (parse-element parent-env el)
        parent-o (env :parent-object)
        incomplete-s (env :incomplete-subject)
        incomplete (env :incomplete)
        has-about (data :about)
        has-p (or (data :property) (data :rel) (data :rev))
        completing-s (or subject (if has-p incomplete-s) o-resource)
        active-s (or subject (if has-p incomplete-s) parent-o)
        active-o (or o-resource o-literal)
        inherited-o-r (if (or (not (data :property))
                              (data :typeof)
                              (data :rel) (data :rev)) o-resource)
        next-parent-o (or inherited-o-r active-s)
        next-incomplete-s (if (not (or subject has-p))
                            incomplete-s
                            (get-hanging data))
        ; TODO: list-rels and list-props (for inlist with both o-l and o-r)
        new-list-map (into {} (concat
                                (for [p list-ps] [p (if active-o [active-o] [])])
                                (if (or subject o-resource)
                                  (for [p (incomplete :list-ps)]
                                    [p [next-parent-o]]))))
        regular-triples (concat
                          (if o-literal
                            (for [p props] (create-triple env el [active-s p o-literal])))
                          (if o-resource
                            (concat (for [p (if o-literal rels
                                                          (concat props rels))]
                                      (create-triple env el [active-s p o-resource]))
                                    (for [p revs] (create-triple env el [o-resource p active-s])))))
        type-triples (let [ts (if (or has-about (not o-resource))
                                active-s o-resource)]
                       (for [t types] (create-triple env el [ts rdf:type t])))
        completed-triples (if completing-s
                            (let [{rels :rels revs :revs} incomplete]
                              (concat
                                (for [rel rels] (create-triple env el [parent-o rel completing-s]))
                                (for [rev revs] (create-triple env el [completing-s rev parent-o])))))
        vocab-triples (if (not-empty (data :vocab))
                        [(create-triple env el [(IRI. (env :base)) rdfa:usesVocabulary (IRI. (env :vocab))])])
        proc-triples (mapcat (partial create-warning-triples env el) errs)
        next-incomplete (cond
                          (and (or rels revs list-ps) (not active-o))
                          {:rels rels :revs revs :list-ps list-ps}
                          (not-empty completed-triples) {}
                          :else incomplete)
        env (assoc env
              :incomplete next-incomplete
              :incomplete-subject next-incomplete-s
              :parent-object next-parent-o
              :list-map new-list-map)
        env (if (not= parent-o next-parent-o)
              (assoc-in env [:incomplete :list-ps] {})
              env)]
    [env data (concat type-triples
                      completed-triples
                      regular-triples
                      vocab-triples) proc-triples]))

(defn gen-list-triples [env el s p l]
  (loop [s s, p p, l l, triples nil]
    (if (empty? l)
      (conj triples [s p rdf:nil])
      (let [node (next-bnode)
            triples (concat triples
                            [(create-triple env el [s p node])
                             (create-triple env el [node rdf:first (first l)])])]
        (recur node rdf:rest (rest l) triples)))))

(declare visit-element)

(defn combine-element-visits [[prev-env
                               prev-triples
                               prev-proc-triples] child]
  (let [{env          :env
         triples      :triples
         proc-triples :proc-triples} (visit-element prev-env child)
        list-map (:list-map env)]
    [(update (if (empty? list-map)
               prev-env
               (assoc prev-env :list-map list-map))
             :prefixes into (:prefixes env))
     (concat prev-triples triples)
     (concat prev-proc-triples proc-triples)]))

(defn visit-element [parent-env el]
  (let [[env data triples proc-triples] (process-element parent-env el)
        has-about (data :about)
        s (:parent-object env)
        changed-s (not= s (:parent-object parent-env))
        new-list-map (:list-map env)
        current-list-map (merge-with concat
                                     (:list-map parent-env)
                                     (if has-about {} new-list-map))
        local-env (assoc env :list-map (if changed-s
                                         (if has-about new-list-map {})
                                         current-list-map))
        [child-env
         child-triples
         child-proc-triples] (reduce combine-element-visits
                                     [local-env nil nil]
                                     (dom/get-child-elements el))
        combined-list-map (:list-map child-env)
        list-triples (apply concat
                            (for [[p l] combined-list-map
                                  :when (or changed-s
                                            (not (contains? current-list-map p)))]
                              (gen-list-triples env el s p l)))
        result-env (assoc env
                     :list-map (cond
                                 changed-s current-list-map
                                 (empty? list-triples) combined-list-map
                                 :else current-list-map))]
    {:env          result-env
     :triples      (concat triples child-triples list-triples)
     :proc-triples (concat proc-triples child-proc-triples)}))

(defn error-results [err-msg lang]
  (let [err-node (next-bnode)
        descr (Literal. err-msg lang)]
    {:env          nil
     :triples      nil
     :proc-triples [[err-node rdf:type rdfa:Error]
                    [err-node dc:description descr]]}))

(defn extract [root context]
  (try
    (let [base-env (init-env (:location context) (get-host-env (:host-language context) root))]
      (visit-element base-env root))
    (catch #?(:clj Exception :cljs js/Error) e
      (error-results #?(:clj (.getMessage e) :cljs (aget e "message")) "en"))))
