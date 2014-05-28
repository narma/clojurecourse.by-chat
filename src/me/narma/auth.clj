(ns me.narma.auth
  (:require
            [ring.util.response :refer [redirect]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [taoensso.timbre :as timbre :refer (log debug error)]
            [me.narma.auth.twitter :refer [->TwitterBackend]]
            [me.narma.auth.protocols :refer :all]))

(deftype EmptyBackend [request]
  UserAuthBackend
  (authenticate [_]
                {:status 404
                 :body "Unknown provider"})
  (knock [_]
         (error "Oauth knock from unknown provider" request)
         {:status 404
          :body "not found"})
  (user-info [_]))

(defn dispatch-backend [method request]
  (case method
    "twitter" (->TwitterBackend request)
    (->EmptyBackend request)))

(defn inject-user-info [request]
  (if-let [method (get-in request [:identity :backend])]
    (let [auth-backend (dispatch-backend method request)]
      (-> request
          (assoc :oauth-provider auth-backend) ; TODO: necessary?
          (assoc :user (user-info auth-backend))))
    request))

(defn login-required [handler]
  "Login required ring middleware"
  (fn [request]
    (if-not (authenticated? request)
      (redirect "/login")
      (handler (inject-user-info request)))))
