(ns me.narma.server
  (:use me.narma.core)
  (:require
    [org.httpkit.server :refer [run-server]]
    [clojure.java.io :as io]
    nomad
    [clojure.tools.logging :as logging] 
    [me.narma.log :as log]
            )
  (:gen-class))

(def cfg (nomad/defconfig my-config (io/resource "config.edn")))

(defn wrap-reload-if-enabled [handler]
  (if (:wrap-reload (cfg))
    (do 
      (use 'ring.middleware.reload)
      ((resolve 'wrap-reload) handler))
  handler))

(defn wrap-stacktrace-log-if-enabled [handler]
  (if (:wrap-stacktrace-log (cfg))
    (do 
      (alter-var-root (var *out*) 
          (fn [out] (java.io.OutputStreamWriter. (logging/log-stream :info "me.narma.stdout"))))
      (alter-var-root (var *err*) 
          (fn [err] (java.io.OutputStreamWriter. (logging/log-stream :error "me.narma.stderr"))))
      
       ;(logging/with-logs "me.narma.exception" 
      (use 'ring.middleware.stacktrace)
              ((resolve 'wrap-stacktrace-log) handler))
       ;)
  handler))

(defn wrap-stacktrace-web-if-enabled [handler]
  (if (:wrap-stacktrace-web (cfg))
    (do 
      (use 'ring.middleware.stacktrace)
      ((resolve 'wrap-stacktrace-web) handler))
  handler))

(defn get-handler []
  ;we call (var app) so that when we reload our code, 
  ;the server is using the Var rather than having its own copy. 
  ;When the root binding changes, the server picks it up without 
  ;having to restart 
  (-> (var app)
    wrap-reload-if-enabled
    wrap-stacktrace-log-if-enabled
    wrap-stacktrace-web-if-enabled
    ))


(defn -main [& [port]] ;; entry point
  (let [port  (if port (Integer/parseInt port) 8090)]
    (println (str "You can view the site at http://localhost:" port))
    (run-server (get-handler) {:port port :queue-size 204800})))