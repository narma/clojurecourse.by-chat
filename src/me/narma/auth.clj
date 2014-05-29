(ns me.narma.auth
  (:require
            [ring.util.response :refer [redirect]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [taoensso.timbre :as timbre :refer (log debug error)]
            [me.narma.auth.twitter :refer [->TwitterBackend]]
            [me.narma.auth.github :refer [->GithubBackend]]
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
    "github" (->GithubBackend request)
    (->EmptyBackend request)))

(defn wrap-user [handler]
  "Wrap user if request is authenticated
  Insert to request :user info with cache it in session
  if auth backend doesn't exist do nothing"
  ; TODO: use statefull session (use sandbar?)
  ; for avoid complexity of session save/restore
  (fn [request]
    (if-not (authenticated? request)
      (handler request)
      (if-let [user (get-in request [:session :user])]
        (handler (assoc request :user user))
        (let [method (get-in request [:session :identity :backend])
              auth-backend (dispatch-backend method request)
              user (user-info auth-backend)]
          (let [resp (handler (assoc request :user user))]
            (if (:session resp)  ;; user code in handler must save session if he want
                                 ;; because it's default behaviour for ring session
              (assoc-in resp [:session :user] user)
              (if-not (contains? resp :session) ;; if session is nil user want remove session
                (assoc resp :session (merge (:session request) {:user user}))
                resp))))))))


