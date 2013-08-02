(ns me.narma.server
  (:use [org.httpkit.server :only [run-server]]        
        me.narma.core
        bultitude.core)
  (:gen-class))


(defn wrap-reload-if-available [handler]
  (if (some #{'ring.middleware.reload} (namespaces-on-classpath))
    (do       
      (use 'ring.middleware.reload)
      ((resolve 'wrap-reload) handler))
    handler))

(defn get-handler []
  ;we call (var app) so that when we reload our code, 
  ;the server is using the Var rather than having its own copy. 
  ;When the root binding changes, the server picks it up without 
  ;having to restart 
  (-> (var app)
    (wrap-reload-if-available)))


(defn -main [& [port]] ;; entry point
  (let [port  (if port (Integer/parseInt port) 8090)]
    (println (str "You can view the site at http://localhost:" port))
    (run-server (get-handler) {:port port :queue-size 204800})))