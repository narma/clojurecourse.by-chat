(defproject narmame "0.1.0-SNAPSHOT"
  :description "personal interests"
  :url "https://narma.me"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring/ring-core "1.2.0"]
                 [http-kit "2.1.8"]
                 ;; oauth
                 [clj-oauth "1.4.1-SNAPSHOT"]
                 
                 [bultitude "0.2.2"]
                 ;; Dommy
                 ]
  :profiles {:dev {:dependencies [[ring/ring-devel "1.2.0"]]}}
  :jvm-opts    ["-Dfile.encoding=UTF-8"]
  :global-vars {*warn-on-reflection* false
                *assert* false}
  :aliases {"reflect" ["update-in" ":global-vars" "assoc" "*warn-on-reflection*" "true" "--" "compile"]}
  :uberjar-name "narmame-standalone.jar"
  :main me.narma.server
  :aot [me.narma.server])
