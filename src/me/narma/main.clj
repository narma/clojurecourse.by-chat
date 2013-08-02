(ns me.narma.main
  (:use [org.httpkit.server :only [run-server]]
        [compojure.handler :only [site]]
        [compojure.core]
        [ring.util.response :only [redirect]]
      )
  (:require [ring.middleware.reload :as reload]
            [compojure.route :as route]
            [me.narma.core :as core]
            [me.narma.oauth :as auth]
            )
  (:gen-class))


(defroutes all-routes
  (GET "/" [] (core/index))
  (GET "/twtter" [] (redirect (auth/get-twitter-url (auth/get-request-token))))
  (GET "/knock" [oauth_token oauth_verifier] (str (auth/get-access-token-twitter oauth_token oauth_verifier)))
  ;;(GET "/save" [] handler)     ;; websocket
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

(defn -main [& [is-dev? & rest]] ;; entry point
  (let [handler (if (= is-dev? "dev")
                  (reload/wrap-reload (site #'all-routes)) ;; only reload when dev
                  (site all-routes))]
    (run-server handler {:port 8090 :queue-size 204800})))