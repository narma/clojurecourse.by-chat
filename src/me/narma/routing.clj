(ns me.narma.core
  (:use
    [ring.util.response :only [redirect]])
  (:require
    [compojure.route :as route]
    [compojure.handler :as handler]
    [compojure.core :as compojure]
    [taoensso.carmine.ring :refer [carmine-store]]

    [clojurewerkz.route-one.compojure :refer :all]

    [me.narma.auth.twitter :as auth]))

(defn index
  "First page"
  []
  "Hello")

(defn error-debug []
  (str (/ 1 0)))


(compojure/defroutes app-routes
  (GET index "/" request (index))
  (compojure/GET "/errordebug" [] (error-debug))
  (compojure/GET "/login/twitter" [] (redirect (auth/twitter)))
  (compojure/GET "/knock/twitter" [oauth_token oauth_verifier]
       (auth/twitter-knock oauth_token oauth_verifier))
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

; (defonce session-store (carmine-store {:expiration-secs (* 60 60 24 7)}))

(def app (-> app-routes
             handler/site))
