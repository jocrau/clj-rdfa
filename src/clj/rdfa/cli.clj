(ns rdfa.cli
  (:require
    [rdfa.repr :refer [print-prefixes print-triples]]
    [rdfa.parser :refer [get-rdfa]]
    [rdfa.parser.jsoup])
  (:gen-class))

(defn -main [& args]
  (let [{:keys [env triples proc-triples]} (get-rdfa (first args))]
    (str (print-prefixes env)
         (print-triples triples)
         (print-triples proc-triples))))

(comment
  (-main "http://iricelino.org/rdfa/sample-annotated-page.html")
  )