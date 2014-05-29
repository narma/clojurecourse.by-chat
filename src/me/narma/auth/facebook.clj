(ns me.narma.auth.facebook
  (:require [oauth.facebook :as fb]
            [oauth.v2 :as oauth]
            [taoensso.timbre :as timbre :refer (log debug error)]
            [me.narma.config :refer [get-config]]
            [ring.util.response :refer [redirect]]
            [me.narma.auth.protocols :refer :all]))

(def client-id (get-config :facebook :client-id))
(def client-secret (get-config :facebook :client-secret))

(def auth-url (fb/oauth-authorization-url client-id "https://narma.me/knock/facebook"))

(deftype FacebookBackend [request]
  UserAuthBackend
  (authenticate [_]
     (redirect (fb/oauth-authorization-url client-id
                                           "https://narma.me/knock/facebook"
                                           :redirect_url "https://narma.me/"
                                           :redirect_uri "https://narma.me/")))

  (knock [this]
         (let [{{:keys [code state]} :params} request]
           (let [access-token (fb/oauth-access-token
                               client-id
                               client-secret
                               code
                               "https://narma.me/knock/facebook")]
           (-> (redirect "/")
               (assoc-in [:session :identity] {:token access-token :backend "facebook"})
               (assoc-in [:session :state] nil)))))

  (user-info [_]
    (let [{{{access-token :access-token} :token} :identity} request
          client (fb/oauth-client access-token)
          info (client {:method :get
                           :url "https://graph.facebook.com/me"})]
      (merge {:avatar-url (str "https://graph.facebook.com/" (:id info) "/picture")} info))))
