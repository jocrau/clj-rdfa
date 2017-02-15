(ns rdfa.api
  (:refer-clojure :exclude [parse-opts])
  (:require
    [clojure.tools.cli :refer [parse-opts]]
    [datascript.core :as d]
    [net.cgrand.enlive-html :as html]
    [rdfa.parser :refer [parse]]
    #_[rdfa.parser.jsoup]
    [rdfa.parser.hickory]
    #_[rdfa.dom.jsoup]
    [rdfa.dom.hickory]
    [rdfa.extractor :refer [extract]]
    [rdfa.serializer :refer [serialize]]
    [rdfa.profiles :refer [detect-host-language]])
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
        host-language (detect-host-language location source)
        context {:location      location
                 :host-language host-language}
        result (-> source
                   (parse context)
                   (extract context))]
    (println (serialize result context))
    (System/exit 0)))

(defn ^:export -main [& args]
  (let [parsed-args (:options (parse-opts args cli-args))]
    (init parsed-args)))

(defn persist [result]
  (let [transactions (map (fn [triple]
                            (let [[s p o] triple]
                              (merge {:s (pr-str s)
                                      :p (pr-str p)
                                      :o (pr-str o)}
                                     (when-let [e (-> triple meta :element)]
                                       {:e e}))))
                          (:triples result))]
    (d/transact conn transactions)))

(comment

  (let [context {:location      "http://example.com/"
                 :host-language :html}]
    (-> (slurp "file:///Users/jocrau/dev/workspaces/clj-rdfa/dev-resources/bake.html")
        (parse context)
        (extract context)))

  (let [context {:location      "http://example.com/"
                 :host-language :html}
        document "<html xmlns=\"http://www.w3.org/1999/xhtml\" version=\"XHTML+RDFa 1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/1999/xhtml http://www.w3.org/MarkUp/SCHEMA/xhtml-rdfa-2.xsd\" lang=\"en\" xml:lang=\"en\" typeof=\"http://xmlns.com/foaf/0.1/Document\">\n<head>\n    <meta charset=\"UTF-8\" />\n    <base href=\"../resources/\" />\n    <title property=\"rdfs:label\">Recipes Template</title>\n\n    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.2/css/bootstrap.min.css\" type=\"text/css\" />\n    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.2/css/bootstrap-responsive.min.css\" type=\"text/css\" />\n    <link rel=\"stylesheet\" href=\"http://code.jquery.com/ui/1.9.1/themes/smoothness/jquery-ui.css\" type=\"text/css\" />\n    <style>\n\n        .navbar-fixed-top {\n\tmargin-bottom: 20px;\n\tposition: static;\n}\n\n.footer {\n  margin-top: 45px;\n  padding: 35px 0 36px;\n  border-top: 1px solid #e5e5e5;\n}\n.footer p {\n  margin-bottom: 0;\n  color: #555;\n}\n\n.span9 *[rel~=\"schema:image\"] {\n\tmax-width: 40%;\n\tmax-height: 200px;\n\tmargin: 10px;\n}\n\n.span3 *[rel~=\"http://xmlns.com/foaf/0.1/depiction\"] {\n\tmax-width: 100%;\n\tmax-height: 200px;\n\tmargin: 0px;\n}\n\n.media img {\n\tmax-width: 100px;\n\tmax-height: 100px;\n}\n\n\n.highlight {\n\tbackground-color: #a5dca3;\n}\n\n.highlight[typeof~=\"http://bakepedia.org/ontology#Step\"] {\n\tbackground-color: #d5d6ff;\n}\n\n*[typeof~=\"schema:VideoObject\"], *[typeof~=\"http://www.w3.org/ns/ma-ont#MediaResource\"]  {\n\tposition: relative;\n}\n\n    </style>\n</head>\n<body>\n<div class=\"navbar navbar-fixed-top\">\n    <div class=\"navbar-inner\">\n        <div class=\"container\">\n            <a class=\"brand\" href=\"#\">Bake</a>\n            <ul class=\"nav\">\n                <li><a href=\"recipes\">Recipes</a></li>\n                <li><a href=\"ingredients\">Ingredients</a></li>\n                <li><a href=\"manuals\">Manuals</a></li>\n            </ul>\n        </div>\n    </div>\n</div>\n\n<div class=\"container\">\n    <div class=\"span12\">\n\n        <div rel=\"http://knowl-edge.org/ontology/core#contains\">\n            <div about=\"http://bakeopedia.org/recipe/moms-world-famous-banana-bread\" typeof=\"schema:Recipe\">\n\n                <h2 property=\"schema:name\">Mom's World Famous Banana Bread</h2>\n\n                <p>By <span rel=\"schema:creator\">\n\t\t\t\t\t\t<span about=\"http://dbpedia.org/resource/Peter_Pan\" typeof=\"foaf:Person\"><span property=\"foaf:name rdfs:label\">Peter Pan</span></span></span>,\n                    <span property=\"schema:datePublished\" content=\"2009-05-08\">May 8, 2009</span>\n                </p>\n\n                <div class=\"row\">\n                    <div class=\"span6\">\n\n                        <p><img rel=\"schema:image\" src=\"http://placehold.it/300x200\" class=\"img-polaroid\"/></p>\n\n                        <div rel=\"schema:video\">\n                            <div about=\"http://example.com/foo/sdafsdf\" typeof=\"http://www.w3.org/ns/ma-ont#MediaResource schema:VideoObject\">\n                                <!-- <iframe id=\"player\" rel=\"http://www.w3.org/ns/ma-ont#locator\" src=\"https://www.youtube.com/embed/xyz?enablejsapi=1&origin=http://localhost:8080/\" width=\"100%\" height=\"315\" frameborder=\"0\" wmode=\"transparent\" allowfullscreen class=\"img-polaroid\"></iframe> -->\n                                <div rel=\"http://www.w3.org/ns/ma-ont#hasFragment\">\n                                    <div about=\"http://example.com/foo/1212123dsfasdffd\" typeof=\"http://knowl-edge.org/ontology/core#TemporalFragment\">\n                                        <div rel=\"schema:mentions\"><div about=\"http://example.com/something\" typeof=\"http://bakepedia.org/ontology#Component http://bakepedia.org/ontology#Step http://www.w3.org/2002/07/owl#Thing http://dbpedia.org/class/yago/KitchenwareBrands\"></div></div>\n                                        <div property=\"http://knowl-edge.org/ontology/core#start\"></div>\n                                        <div property=\"http://knowl-edge.org/ontology/core#end\"></div>\n                                    </div>\n                                    <div about=\"http://example.com/foo/12156476745dfgdfg\" typeof=\"http://knowl-edge.org/ontology/core#SpatialFragment\">\n                                        <div rel=\"schema:mentions\"><div about=\"http://example.com/somethingelse\" typeof=\"http://bakepedia.org/ontology#Guide http://bakepedia.org/ontology#Ingredient http://bakepedia.org/ontology#Appliance http://purl.org/goodrelations/v1#Offering\"></div></div>\n                                        <div property=\"http://knowl-edge.org/ontology/core#start\"></div>\n                                        <div property=\"http://knowl-edge.org/ontology/core#end\"></div>\n                                        <div property=\"http://knowl-edge.org/ontology/core#top\"></div>\n                                        <div property=\"http://knowl-edge.org/ontology/core#left\"></div>\n                                        <div property=\"http://knowl-edge.org/ontology/core#width\"></div>\n                                        <div property=\"http://knowl-edge.org/ontology/core#height\"></div>\n                                    </div>\n                                </div>\n                            </div>\n                        </div>\n\n                    </div>\n\n                    <div class=\"span6\">\n\n                        <h4>Ingredients</h4>\n                        <ul inlist=\"\" rel=\"http://bakepedia.org/ontology#components\">\n                            <li about=\"http://bakepedia.org/resource/12343422\" typeof=\"http://bakepedia.org/ontology#Component\">\n                                <span property=\"http://bakepedia.org/ontology#quantity\">2 or 3</span>\n                                <span property=\"http://bakepedia.org/ontology#preCondition\"> </span>\n                                <span rel=\"http://bakepedia.org/ontology#ingredient\">\n\t\t\t\t\t\t\t\t\t\t<span typeof=\"http://bakepedia.org/ontology#Ingredient\">\n\t\t\t\t\t\t\t\t\t\t\t<span property=\"http://bakepedia.org/ontology#name\">Bananas</span>\n\t\t\t\t\t\t\t\t\t\t\t<span rel=\"rdfs:seeAlso\" style=\"display: none;\">\n\t\t\t\t\t\t\t\t\t\t\t\t<span typeof=\"http://dbpedia.org/class/yago/LeaveningAgents\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\thttp://dbpedia.org/class/yago/Cereals\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\thttp://dbpedia.org/class/yago/FoodAdditives\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\trdfs:Resource\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\thttp://www.w3.org/2002/07/owl#Thing\">\n\t\t\t\t\t\t\t\t\t\t\t\t\t<img rel=\"http://dbpedia.org/ontology/thumbnail schema:image http://xmlns.com/foaf/0.1/depiction\" src=\"http://placehold.it/300x200\" class=\"img-polaroid pull-right\"/>\n\t\t\t\t\t\t\t\t\t\t\t\t\t<span property=\"rdfs:comment\"> </span>\n\t\t\t\t\t\t\t\t\t\t\t\t\t<a rel=\"http://xmlns.com/foaf/0.1/isPrimaryTopicOf http://knowl-edge.org/ontology/core#externalLink http://dbpedia.org/ontology/wikiPageExternalLink\" href=\"#\" class=\"btn btn-small pull-right\">Buy Online</a>\n\t\t\t\t\t\t\t\t\t\t\t\t</span>\n\t\t\t\t\t\t\t\t\t\t\t</span>\n\t\t\t\t\t\t\t\t\t\t</span>\n\t\t\t\t\t\t\t\t\t</span>\n                                <span property=\"http://bakepedia.org/ontology#comment\"> </span>\n                            </li>\n                        </ul>\n\n                        <h4>Preparation</h4>\n\n                        <p property=\"schema:recipeInstructions\">\n                            Preheat the oven to 350 degrees. Mix in the ingredients in a bowl. Add\n                            the flour last. Pour the mixture into a loaf pan and bake for one hour.\n                        </p>\n\n                    </div>\n                </div>\n                <div class=\"well well-small row\">\n                    <div class=\"span3\">\n                        <h4>Nutritional Information</h4>\n\n                        <p>1 serving contains</p>\n\n                        <div rel=\"schema:nutrition\">\n                            <div typeof=\"schema:NutritionInformation\">\n                                <div>Calories <span property=\"schema:calories\">42</span>kcal</div>\n                                <div>Fat <span property=\"schema:fatContent\">42</span>g</div>\n                                <div>Saturated Fat <span property=\"schema:saturatedFatContent\">42</span>g</div>\n                                <div>Cholesterol <span property=\"schema:cholesterolContent\">42</span>mg</div>\n                                <div>Carbohydrates <span property=\"schema:carbohydrateContent\">42</span>g</div>\n                                <div>Dietary Fiber <span property=\"schema:fiberContent\">42</span>g</div>\n                                <div>Total Sugars <span property=\"schema:sugarContent\">42</span>g</div>\n                                <div>Protein <span property=\"schema:proteinContent\">42</span>g</div>\n                                <div>Sodium <span property=\"schema:sodiumContent\">42</span>mg</div>\n                            </div>\n                        </div>\n                    </div>\n                    <div class=\"span3\">\n                        <h4>Ratings</h4>\n\n                        <p property=\"schema:aggregateRating\" typeof=\"schema:AggregateRating\">\n                            <span property=\"schema:ratingValue\">4</span> stars - based on <span property=\"schema:reviewCount\">250</span> reviews\n                        </p>\n                    </div>\n                    <div class=\"span3\">\n                        <h4>More Information</h4>\n\n                        <p>\n                            Prep Time: <span property=\"schema:prepTime\" content=\"PT15M\">15 minutes</span><br />\n                            Total time: <span property=\"schema:totalTime\" content=\"PT3H\">3 hour</span><br/>\n                            Yield: <span property=\"schema:recipeYield\">1 loaf</span>\n                        </p>\n\n                        <p><a rel=\"http://knowl-edge.org/ontology/core#externalLink\" href=\"#\">Read More</a></p>\n                    </div>\n                </div>\n            </div>\n        </div>\n    </div>\n</div>\n\n<pre id=\"rdf-container\"></pre>\n\n<script type=\"text/javascript\" src=\"http://code.jquery.com/jquery.min.js\"></script>\n<script type=\"text/javascript\" src=\"http://code.jquery.com/ui/1.9.1/jquery-ui.min.js\"></script>\n<script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.2/js/bootstrap.min.js\"></script>\n<script type=\"text/javascript\" src=\"public/js/clj-rdfa.js\"></script>\n<script type=\"text/javascript\">rdfa.api.init();</script>\n\n</body>\n</html>"
        #_(slurp "file:///Users/jocrau/dev/workspaces/clj-rdfa/dev-resources/bake.html")
        ]
    (-> document
        (parse context)
        (extract context)
        (serialize context)
        println))

  (let [context {:location      "http://example.com/"
                 :host-language :html}
        document (slurp "file:///Users/jocrau/dev/workspaces/clj-rdfa/dev-resources/bake.html")]
    (-> document
        (parse context)
        (extract context)
        (persist)
        println))

  (d/q '[:find ?e .
         :where
         [?node :o "#rdfa.rdf.IRI{:id \"http://schema.org/Recipe\"}"]
         [?node :e ?e]]
       @conn)

  (let [source (d/q '[:find ?e .
                      :where
                      [?node :o "#rdfa.rdf.IRI{:id \"http://schema.org/Recipe\"}"]
                      [?node :e ?e]]
                    @conn)]
    ((html/snippet* (html/select source [:div]) []
                    [:div] (html/content "sdfsdfa"))))

  )