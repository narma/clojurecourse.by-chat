(ns me.narma.core
  (:use 
    [compojure.core]
    [ring.util.response :only [redirect]])
  (:require 
    [compojure.handler :as handler]
    [compojure.route :as route]
    [me.narma.oauth :as auth]))

(defn index
  "First page"
  []
  "Hello world!")

(defn twitter [] 
  redirect (auth/get-twitter-url (auth/get-request-token)))

(defroutes app-routes
  (GET "/" [] (index))
  (GET "/twtter" [] (twitter))
  (GET "/knock" [oauth_token oauth_verifier] (str (auth/get-access-token-twitter oauth_token oauth_verifier)))
  ;;(GET "/save" [] handler)     ;; websocket
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

(def app (handler/site app-routes))
