(ns me.narma.core
  (:use
    compojure.core
    [ring.util.response :only [redirect]])
  (:require
    [compojure.handler :as handler]
    [taoensso.carmine.ring :refer [carmine-store]]
    [compojure.route :as route]
    ;[bidi :refer [make-handler]]

    [me.narma.auth.twitter :as auth]))

(defn index
  "First page"
  []
  "Hello")

(defn error-debug []
  (str (/ 1 0)))



(defroutes app-routes
  (GET "/" [] (index))
  (GET "/debug" [] (error-debug))
  (GET "/login/twitter" [] (redirect (auth/twitter)))
  (GET "/knock/twitter" [oauth_token oauth_verifier]
       (auth/twitter-knock oauth_token oauth_verifier))
  ;;(GET "/save" [] handler)     ;; websocket
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

(defonce session-store (carmine-store {:expiration-secs (* 60 60 24 7)}))

(def app (-> app-routes
             handler/site
             ))
