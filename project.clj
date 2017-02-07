(defproject rdfa "0.6.0-SNAPSHOT"
  :description "A Clojure library for extracting triples from RDFa 1.1 in HTML/XHTML/SVG/XML"
  :url "https://github.com/niklasl/clj-rdfa"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/tools.cli "0.3.5"]
                 [com.taoensso/timbre "4.5.1"]
                 [mount "0.1.10"]
                 [environ "1.1.0"]
                 [org.jsoup/jsoup "1.10.1"]
                 [figwheel-sidecar "0.5.7"]]
  :profiles {:dev     {:plugins      [[lein-midje "3.2"]]
                       :dependencies [[midje "1.8.3"]]}
             :uberjar {:aot :all}}
  :plugins [[lein-cljsbuild "1.1.4"]]
  :cljsbuild {:builds {:dev {:figwheel     {:on-jsload "rdfa.ui/init-ui"}
                             :source-paths ["src/cljs" "src/cljc"]
                             :compiler     {:main                 rdfa.system
                                            :asset-path           "public/js/target"
                                            :output-to            "resources/public/js/clj-rdfa.js"
                                            :output-dir           "resources/public/js/target"
                                            :optimizations        :none
                                            :source-map-timestamp true
                                            :verbose              false
                                            :pretty-print         true}}}}
  :figwheel {:open-file-command "open-in-intellij"}
  :repositories {"stuartsierra-releases" "http://stuartsierra.com/maven2"}
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :clean-targets ^{:protect false} ["target" "resources/public/js/target" "resources/public/js/cls-rdfa.js"]
  :target-dir "target"
  :jar-exclusions [#"(?:^|/)\..+"]
  :main rdfa.system)
