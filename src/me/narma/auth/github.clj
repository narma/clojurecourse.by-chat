(ns me.narma.auth.github
  (:require [oauth.github :as gh]
            [oauth.v2 :as oauth]
            [taoensso.timbre :as timbre :refer (log debug error)]
            [me.narma.config :refer [get-config]]
            [ring.util.response :refer [redirect]]
            [me.narma.auth.protocols :refer :all]))

(defn make-uuid [] (str (java.util.UUID/randomUUID)))

(def client-id (get-config :github :client-id))
(def client-secret (get-config :github :client-secret))

(def auth-url (gh/oauth-authorization-url client-id "https://narma.me/knock/github"))

(deftype GithubBackend [request]
  UserAuthBackend
  (authenticate [_]
     (let [uuid (make-uuid)]
       (-> (redirect (gh/oauth-authorization-url client-id
                                                 "https://narma.me/knock/github"
                                                 :state uuid))
           (assoc-in [:session :state] uuid))))


  (knock [this]
         (let [{{:keys [code state]} :params} request]
           (if-not (= state
                      (get-in request [:session :state]))
             (do
               (assoc-in [:session :state] nil)
               {:status 401
                :body "State mismath"})
           (let [access-token (gh/oauth-access-token
                               client-id
                               client-secret
                               code
                               "https://narma.me/knock/github")]
           (-> (redirect "/")
               (assoc-in [:session :identity] {:token access-token :backend "github"})
               (assoc-in [:session :state] nil))))))

  (user-info [_]
    (let [{{{access-token :access-token} :token} :identity} request
          client (gh/oauth-client access-token)
          info (client {:method :get
                           :url "https://api.github.com/user"})]
      info)))
