(ns me.narma.core
  (:use 
    compojure.core
    [ring.util.response :only [redirect]])
  (:require 
    [compojure.handler :as handler]
    [compojure.route :as route]
    [me.narma.oauth :as auth]
    [me.narma.log :as log]))

(defn index
  "First page"
  []
  (log/info "index page")
  "Hello")

(defn error-debug []
  (str (/ 1 0)))

(defn twitter [] 
  (-> (auth/get-request-token) auth/get-twitter-url redirect))

(defroutes app-routes
  (GET "/" [] (index))
  (GET "/debug" [] (error-debug))
  (GET "/twitter" [] (twitter))
  (GET "/knock" [oauth_token oauth_verifier] (str (auth/get-access-token-twitter oauth_token oauth_verifier)))
  ;;(GET "/save" [] handler)     ;; websocket
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

(def app (handler/site app-routes))
