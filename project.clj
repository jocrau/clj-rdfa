(defproject
  rdfa "0.5.1-SNAPSHOT"
  :description "A Clojure library for extracting triples from RDFa 1.1 in HTML/XHTML/SVG/XML"
  :url "https://github.com/niklasl/clj-rdfa"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0-RC2"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [org.jsoup/jsoup "1.8.3"]
                 [net.sourceforge.nekohtml/nekohtml "1.9.22"]]
  :profiles {:dev {:plugins      [[lein-midje "3.2"]]
                   :dependencies [[midje "1.8.2"]]}}
  :plugins [[lein-cljsbuild "1.1.1"]]
  :cljsbuild {:builds [{:source-paths ["src/cljs" "src/cljc"]
                        :compiler     {:output-to             "resources/public/js/main.js"
                                       :print-input-delimiter true
                                       :pretty-print          true}}]}
  :repositories {"stuartsierra-releases" "http://stuartsierra.com/maven2"}
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :target-dir "target"
  :jar-exclusions [#"(?:^|/)\..+"]
  :main rdfa.cli)
