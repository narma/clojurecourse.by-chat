(ns me.narma.core
  (:use 
    compojure.core
    [ring.util.response :only [redirect]]
    clojure.tools.logging
    clj-logging-config.log4j)
  (:require 
    [compojure.handler :as handler]
    [compojure.route :as route]
    [me.narma.oauth :as auth]))

(set-logger! :level :info
  :out (org.apache.log4j.DailyRollingFileAppender. 
         (org.apache.log4j.PatternLayout. "%d{ISO8601} [%t] %-5p - %m%n") 
         "/var/log/narmame.log" "'.'yyyy-MM-dd")
  :file "/var/log/narmame.log"
  :pattern "%d{ISO8601} [%t] %-5p - %m%n"
  )

(defn index
  "First page"
  []
  (info "hello")
  "Hello world!")

(defn twitter [] 
  (-> (auth/get-request-token) auth/get-twitter-url redirect))

(defroutes app-routes
  (GET "/" [] (index))
  (GET "/twitter" [] (twitter))
  (GET "/knock" [oauth_token oauth_verifier] (str (auth/get-access-token-twitter oauth_token oauth_verifier)))
  ;;(GET "/save" [] handler)     ;; websocket
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

(def app (handler/site app-routes))
