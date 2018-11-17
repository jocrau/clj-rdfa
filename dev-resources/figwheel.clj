(require '[figwheel.main.api :as fig])
(fig/start {:id      "dev"
            :options {:main                 rdfa.api
                      :asset-path           "public/js/target"
                      :output-to            "resources/public/js/clj-rdfa.js"
                      :output-dir           "resources/public/js/target"
                      :optimizations        :none
                      :source-map-timestamp true
                      :verbose              false
                      :pretty-print         true}
            :config  {:open-url       false
                      :rebel-readline false
                      :auto-test      true
                      :watch-dirs     ["src/cljc" "src/cljs" "test/cljc"]}})