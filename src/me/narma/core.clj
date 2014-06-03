(ns me.narma.core
  (:use ring.middleware.edn
        compojure.core)
  (:require [clojure.java.io :as io]
            [compojure.handler :as handler]
            [compojure.route :as route]
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
            [me.narma.service :as service]
            [me.narma.auth :refer [wrap-user] :as auth]
            [me.narma.auth.protocols :refer :all]
            [clj-json.core :as json]))

(def redis-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
; (defmacro wcar* [& body] `(wcar redis-conn ~@body))

; (wcar* (car/get "carmine:session:60dbe4e8-6791-4ca6-9671-36f19c71965e"))
(defn index [request]
  (str "Hi "
       (get-in request [:user :name])
       "<br/>"
       (str "<img src=\""
            (get-in request [:user :avatar-url])
            "\">")))

(defn login-ctrl [request]
  (cond
   (= (:request-method request) :get)
     (if (authenticated? request)
       (redirect "/")
       (slurp (io/resource "public/login.html")))
   (= (:request-method request) :post)
       (authenticate (auth/->DemoBackend request))))



(defroutes app-routes
  (route/resources "/" {:root ""})
  (GET "/user.js" req {
                      :status 200
                      :headers {"Content-Type" "application/javascript"}
                      :body (str "window.user_info = "
                                 (json/generate-string (:user req)))
                      })

  (context "/messages" [] service/messages-routes)

  (GET "/" [] (slurp (io/resource "public/index.html")))
  (GET "/userdebug" req (str (:user req)))
  (ANY "/login" [] login-ctrl)
  (GET "/logout" [] (-> (redirect "/")
                        (assoc :session nil)))
  (GET "/login/:method" {{method :method} :route-params :as req}
       (authenticate (auth/dispatch-backend method req)))
  (GET "/knock/:method" {{method :method} :route-params :as req}
       (knock (auth/dispatch-backend method req)))
  (route/not-found "<p>404</p>"))

(defn unauthorized-handler
  [request metadata]
  (if (authenticated? request)
    (redirect "/")
    (redirect "/login")))

(def backend (session-backend :unauthorized-handler unauthorized-handler))

(def rules [{:pattern #"^/(login|knock|logout|public).*"
             :handler (constantly true)}
            {:pattern #".*"
             :handler authenticated?}])

(defn reject-handler [request]
  (if (authenticated? request)
    {:status 403
     :headers {}
     :body "Not authorized"}
    (redirect "/login")))

(def app (-> app-routes
             handler/api
             wrap-multipart-params
             wrap-flash
             wrap-edn-params
             (wrap-user {:on-user-first service/on-user-auth})
             (wrap-access-rules :rules rules
                                :policy :reject
                                :reject-handler reject-handler)
             (wrap-authorization backend)
             (wrap-authentication backend) ;; session must be after this
             (wrap-session {:store (carmine-store redis-conn)})))
