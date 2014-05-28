(ns me.narma.core
  (:require [clojure.java.io :as io]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer :all]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.util.response :refer [redirect]]
            [taoensso.carmine :as car :refer (wcar)]
            [taoensso.timbre :as timbre :refer (log debug)]
            [buddy.auth.middleware :refer [wrap-authentication wrap-access-rules wrap-authorization]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [taoensso.carmine.ring :refer [carmine-store]]
            [me.narma.auth :refer [login-required] :as auth]
            [me.narma.auth.protocols :refer :all]))

(def redis-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
; (defmacro wcar* [& body] `(wcar redis-conn ~@body))

; (wcar* (car/get "carmine:session:60dbe4e8-6791-4ca6-9671-36f19c71965e"))


(defn unauthorized-handler
  [request metadata]
  (if (authenticated? request)
    (redirect "/")
    (redirect "/login")))



(defn index [request]
  (str "Hello "
       (get-in request [:user :name])
       "<br/>"
       (str "<img src=\""
            (get-in request [:user :avatar-url])
            "\">")))

(defroutes app-routes
  (GET "/" [] (login-required index))
  (GET "/test" [] {:session {:my-var "hello"}})
  (GET "/login" req (if (authenticated? req)
       (redirect "/")
       (slurp (io/resource "public/html/login.html"))))
  (GET "/login/:method" {{method :method} :route-params :as req}
       (authenticate (auth/dispatch-backend method req)))
  (GET "/knock/:method" {{method :method} :route-params :as req}
       (knock (auth/dispatch-backend method req)))
  (route/not-found "<p>404</p>"))

(def backend (session-backend :unauthorized-handler unauthorized-handler))
(def app (-> app-routes
             (handler/api)
             (wrap-multipart-params)
             (wrap-flash)
             (wrap-authentication backend) ;; session must be after this
             (wrap-session {:store (carmine-store redis-conn)})))
