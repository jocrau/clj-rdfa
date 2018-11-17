(defproject rdfa "0.6.0-SNAPSHOT"
  :description "A Clojure library for extracting triples from RDFa 1.1 in HTML/XHTML/SVG/XML"
  :url "https://github.com/niklasl/clj-rdfa"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :test-paths ["test/clj" "test/cljs" "test/cljc"]
  :clean-targets ^{:protect false} ["target" "resources/public/js/target" "resources/public/js/clj-rdfa.js"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [org.clojure/tools.cli "0.4.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.jsoup/jsoup "1.11.3"]
                 [hickory "0.7.1" :exclusions [org.jsoup/jsoup]]
                 [datascript "0.16.7"]
                 [enlive "1.1.6"]
                 [kioo "0.5"]]
  :profiles {:dev     {:source-paths ["src/clj" "src/cljs" "src/cljc"]
                       :dependencies [[com.bhauman/figwheel-main "0.1.9"]
                                      [com.bhauman/rebel-readline-cljs "0.1.4"]]}
             :uberjar {:main rdfa.api
                       :aot  :all}}
  :aliases {"fig-dev"  ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig-test" ["trampoline" "run" "-m" "figwheel.main" "-b" "test" "-r"]
            "fig-prod" ["trampoline" "run" "-m" "figwheel.main" "-b" "prod"]}
  :repl-options {:init-ns rdfa.api}
  :target-dir "target"
  :jar-exclusions [#"(?:^|/)\..+"]
  :min-lein-version "2.0.0")
