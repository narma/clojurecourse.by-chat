(ns me.narma.auth.google
  (:require [oauth.google :as oauth-provider]
            [oauth.v2 :as oauth]
            [taoensso.timbre :as timbre :refer (log debug error)]
            [me.narma.config :refer [get-config]]
            [ring.util.response :refer [redirect]]
            [me.narma.auth.protocols :refer :all]))

(def client-id (get-config :google :client-id))
(def client-secret (get-config :google :client-secret))

(def auth-url (oauth-provider/oauth-authorization-url client-id "https://narma.me/knock/google"))

(deftype GoogleBackend [request]
  UserAuthBackend
  (authenticate [_]
     (redirect (oauth-provider/oauth-authorization-url client-id
                                           "https://narma.me/knock/google")))

  (knock [this]
         (let [{{:keys [code state]} :params} request]
           (let [access-token (oauth-provider/oauth-access-token
                               client-id
                               client-secret
                               code
                               "https://narma.me/knock/google")]
           (-> (redirect "/")
               (assoc-in [:session :identity] {:token access-token :backend "google"})
               (assoc-in [:session :state] nil)))))

  (user-info [_]
    (let [{{{access-token :access-token} :token} :identity} request
          client (oauth-provider/oauth-client access-token)
          info (oauth-provider/user-info client)]
      (merge {:avatar-url (:picture info)} info))))
