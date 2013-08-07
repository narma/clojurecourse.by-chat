(defproject narmame "0.1.0-SNAPSHOT"
  :description "personal interests"
  :url "https://narma.me"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  ; :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [
                [org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-devel "1.2.0"]
                 [http-kit "2.1.8"]
                 [clj-oauth "1.4.1-SNAPSHOT"]
                 
                 [jarohen/nomad "0.5.0"] ;; cfg

                 ;; logging
                 [org.clojure/tools.logging "0.2.6"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [org.codehaus.janino/janino "2.6.1"]
                 ]
  ; :profiles {:dev {:dependencies [[]]}
  ;            }
  :jvm-opts    ["-Dfile.encoding=UTF-8"]
  :global-vars {*warn-on-reflection* false
                *assert* false}
  :aliases {"reflect" ["update-in" ":global-vars" "assoc" "*warn-on-reflection*" "true" "--" "compile"]}
  :uberjar-name "narmame-standalone.jar"
  :main me.narma.server
  :aot [me.narma.server])
