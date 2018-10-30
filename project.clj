(defproject rdfa "0.6.0-SNAPSHOT"
  :description "A Clojure library for extracting triples from RDFa 1.1 in HTML/XHTML/SVG/XML"
  :url "https://github.com/niklasl/clj-rdfa"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :clean-targets ^{:protect false} ["target" "resources/public/js/target" "resources/public/js/cls-rdfa.js"]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [org.clojure/tools.cli "0.4.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.jsoup/jsoup "1.11.3"]
                 [hickory "0.7.1" :exclusions [org.jsoup/jsoup]]
                 [datascript "0.16.7"]
                 [enlive "1.1.6"]
                 [kioo "0.5" :exclusions [org.omcljs/om]]]
  :profiles {:dev     {:plugins      [[lein-figwheel "0.5.9" :exclusions [org.clojure/clojure]]]
                       :dependencies [[figwheel-sidecar "0.5.17"]]}
             :uberjar {:main rdfa.api
                       :aot  :all}}
  :cljsbuild {:builds {:dev {:figwheel     {:on-jsload "rdfa.api/init"}
                             :source-paths ["src/cljs" "src/cljc"]
                             :compiler     {:main                 rdfa.api
                                            :asset-path           "public/js/target"
                                            :output-to            "resources/public/js/clj-rdfa.js"
                                            :output-dir           "resources/public/js/target"
                                            :optimizations        :none
                                            :source-map-timestamp true
                                            :verbose              false
                                            :pretty-print         true}}}}

  :repl-options {:init-ns rdfa.api}
  :target-dir "target"
  :jar-exclusions [#"(?:^|/)\..+"]
  :min-lein-version "2.0.0")
