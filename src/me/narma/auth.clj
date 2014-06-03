(ns me.narma.auth
  (:require
            [ring.util.response :refer [redirect]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [taoensso.timbre :as timbre :refer (log debug error)]
            [me.narma.auth.twitter :refer [->TwitterBackend]]
            [me.narma.auth.github :refer [->GithubBackend]]
            [me.narma.auth.facebook :refer [->FacebookBackend]]
            [me.narma.auth.google :refer [->GoogleBackend]]
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

(deftype DemoBackend [request]
  UserAuthBackend
  (authenticate [_]
                (-> (redirect "/")
                    (assoc-in  [:session :identity] {:token true
                                                :backend "demo"})))
  (knock [_] true)
  (user-info [_] (let [user-id (rand-int 1000)]
                  {:name (str "Demo" user-id) :avatar-url "/public/images/ava.png" :id user-id})))

(defn dispatch-backend [method request]
  (case method
    "twitter" (->TwitterBackend request)
    "github" (->GithubBackend request)
    "facebook" (->FacebookBackend request)
    "google" (->GoogleBackend request)
    "demo" (->DemoBackend request)
    (->EmptyBackend request)))

(defn wrap-user [handler & [{:keys [on-user on-user-first]}]]
  "Wrap user if request is authenticated
  Insert to request :user info with cache it in session
  if auth backend doesn't exist do nothing
  Optional (on-user user req) callend if user found"
  ; may be use statefull session (use sandbar?)
  ; for avoid complexity of session save/restore
  (fn [request]
    (if-not (authenticated? request)
      (handler request)
      (if-let [user (get-in request [:session :user])]
        (do
          (when on-user (on-user user request))
          (handler (assoc request :user user))
          )
        (let [method (get-in request [:session :identity :backend])
              auth-backend (dispatch-backend method request)
              user1 (user-info auth-backend)
              opt-data (or (when on-user-first
                         (on-user-first user1 request)) {})
              user (merge user1 opt-data)
              ]
          (let [resp (handler (assoc request :user user))]
            (if (:session resp)  ;; user code in handler must save session if he want
                                 ;; because it's default behaviour for ring session
              (assoc-in resp [:session :user] user)
              (if-not (contains? resp :session) ;; if session is nil user want remove session
                (assoc resp :session (merge (:session request) ;; save current session
                                            {:user user}))
                resp))))))))


