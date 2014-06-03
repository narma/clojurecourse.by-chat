(ns me.narma.server
  (:use me.narma.core)
  (:require
    [org.httpkit.server :refer [run-server]]
    [clojure.java.io :as io]
    nomad
    [taoensso.timbre :as timbre :refer (log debug)]
    [clojure.tools.nrepl.server :as nrepl]
    )
  (:gen-class))

(def cfg (nomad/defconfig my-config (io/resource "config.edn")))

(defn wrap-reload-if-enabled [handler]
  (if (:wrap-reload (cfg))
    (do
      (use 'ring.middleware.reload)
      ((resolve 'wrap-reload) handler))
  handler))

(defn log-filepath [& more]
  (let [log-path (-> (System/getProperty "java.class.path")
                     (java.io.File.)
                     (.getAbsoluteFile) ;; running jar file
                     (.getParentFile)
                     (.getPath)
                     (str "/logs"))]
    (str log-path "/" (apply str (interpose "/" more)))))



(defn output-stream-via-fn [fun]
  "(output-stream-via-fn %(spit \"/tmp/log.txt\" % :append true)"
   (-> (proxy [java.io.ByteArrayOutputStream] []
         (flush []
                (let [^java.io.ByteArrayOutputStream this this]
                  (proxy-super flush)
                  (let [message (.trim (.toString this))]
                    (proxy-super reset)
                    (fun message)))))
       (java.io.PrintStream. true)
       (java.io.OutputStreamWriter.)))


(defn wrap-stacktrace-log-if-enabled [handler]
  (if (:wrap-stacktrace-log (cfg))
    (do
      (alter-var-root (var *out*)
          (fn [out]
            (output-stream-via-fn #(spit (log-filepath "out.log") % :append true))))
      (alter-var-root (var *err*)
          (fn [err]
            (output-stream-via-fn #(spit (log-filepath "err.log") % :append true))))

      (use 'ring.middleware.stacktrace)
              ((resolve 'wrap-stacktrace-log) handler))
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
    ;wrap-stacktrace-log-if-enabled
    wrap-stacktrace-web-if-enabled))



(defn -main [& [port]] ;; entry point
  ;(require '[lighttable.nrepl.handler :refer [lighttable-ops]])

  (timbre/set-config! [:appenders :spit :enabled?] true)
  (timbre/set-config! [:shared-appender-config :spit-filename] (log-filepath "narma.me.log"))

  (let [port  (if port (Integer/parseInt port) 8090)
   ;     nrepl-server
    ;     (nrepl/start-server :port 55415
    ;                   :handler (nrepl/default-handler (ns-resolve *ns* (symbol "lighttable-ops"))))
        ]
    (println (str "You can view the site at http://localhost:" port))
    ;(.addShutdownHook (Runtime/getRuntime)
    ;                  (Thread. #(nrepl/stop-server nrepl-server)))
    (run-server (get-handler) {:port port :queue-size 204800})))
