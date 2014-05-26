(defproject narmame "0.1.0-SNAPSHOT"
  :description "personal interests"
  :url "https://narma.me"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  ; :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [
                [org.clojure/clojure "1.6.0"]

                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-core "1.2.2"]
                 [ring/ring-devel "1.2.2"]
                 [http-kit "2.1.18"]

                 [compojure "1.1.8"]
                 [clojurewerkz/route-one "1.1.0"]

                 ;; [mavericklou/oauth-clj "0.1.4.1"]
                 ; [clj-oauth "1.5.1"]
                 [oauth-clj "0.1.12"]
                 [com.taoensso/timbre "3.2.1"] ;; logging

                 [jarohen/nomad "0.5.0"] ;; cfg

                 [com.taoensso/timbre "3.2.1"] ;; logging


                 ;; remote nrepl
                 [org.clojure/tools.nrepl "0.2.3"]
                 [lein-light-nrepl "0.0.14"]

                 ;; auth
                 [buddy "0.1.1"]

                 ;; redis
                 [com.taoensso/carmine "2.6.0"]
                 ]
  ; :profiles {:dev {:dependencies [[]]}
  ;            }
  :jvm-opts    ["-Dfile.encoding=UTF-8 -Djava.awt.headless=true"]
  :repl-options {
                 :port 3000
                 :nrepl-middleware [lighttable.nrepl.handler/lighttable-ops] }
  :global-vars {*warn-on-reflection* false
                *assert* false}
  :aliases {"reflect" ["update-in" ":global-vars" "assoc" "*warn-on-reflection*" "true" "--" "compile"]}
  :uberjar-name "narmame-standalone.jar"
  :main me.narma.server
  :aot [me.narma.server])
