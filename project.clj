(defproject narmame "0.1.0-SNAPSHOT"
  :description "clojurecourse.by final exam task:
                simple chat powered by clojure/clojurescript"
  :url "https://narma.me"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  ; :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [
                [org.clojure/clojure "1.6.0"]

                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-core "1.2.2"]
                 [ring/ring-devel "1.2.2"]
                 [fogus/ring-edn "0.2.0"]
                 ;[ring/ring-anti-forgery "1.0.0-RC1"]
                 [http-kit "2.1.18"]

                 [compojure "1.1.8"]
                 [clj-json "0.5.3"]
                 ; [clojurewerkz/route-one "1.1.0"]

                 ;; [mavericklou/oauth-clj "0.1.4.1"]
                 ; [clj-oauth "1.5.1"]
                 [oauth-clj "0.1.12"]
                 [com.taoensso/timbre "3.2.1"] ;; logging

                 [jarohen/nomad "0.5.0"] ;; cfg

                 [com.taoensso/timbre "3.2.1"] ;; logging

                 ;; auth
                 [buddy "0.1.1"]

                 ;; data
                 [com.taoensso/carmine "2.6.0"]
                 [com.datomic/datomic-free "0.9.4766.16"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :profiles {:dev {:dependencies [
                  ;; remote nrepl
                 [org.clojure/tools.nrepl "0.2.3"]
                 [lein-light-nrepl "0.0.10"]
                  ;; debug
                 [org.clojure/tools.trace "0.7.8"]
                  ;;; client
                 [cljs-ajax "0.2.4"]
                 [org.clojure/clojurescript "0.0-2227"]
                 [org.clojars.narma/react "0.10.0"]
                 ; [kioo "0.4.0"]
                 ;[kioo "0.4.1-SNAPSHOT" :exclusions [om com.facebook/react]]
                 [org.clojars.narma/kioo "0.4.1-SNAPSHOT" :exclusions [om com.facebook/react]]
                 [reagent "0.4.2"]]
                   }}
  :resource-paths ["resources"]
  :cljsbuild {:builds {:dev {:source-paths ["cljs-src"]
                             :compiler {:output-to "resources/public/js/app.js"
                                        :output-dir "resources/public/js/out"
                                        :optimizations :whitespace
                                        :source-map "resources/public/js/app.js.map"}
                             :notify-command ["twmnc" "-t cljsbuild" "-c narma.dev"]}
                       :prod {:source-paths ["cljs-src"]
                              :compiler {:output-to "resources/public/js/app.min.js"
                                         :output-dir "resources/public/js/out-min"
                                         :elide-asserts true
                                         :optimizations :advanced
                                         :pretty-print false
                                         :externs ["react/externs/react.js"]
                                         :source-map "resources/public/js/app.min.js.map"}}}}

  :jvm-opts    ["-Djava.awt.headless=true"]
  :repl-options {
                 :port 3000
                 :nrepl-middleware [lighttable.nrepl.handler/lighttable-ops] }
  :global-vars {*warn-on-reflection* false
                *assert* false}
  :aliases {"reflect" ["update-in" ":global-vars" "assoc" "*warn-on-reflection*" "true" "--" "compile"]}
  :uberjar-name "narmame-standalone.jar"
  :main me.narma.server
  :aot [me.narma.server])
