(ns me.narma.auth.twitter
  (:require [oauth.twitter :as tw]
            [oauth.v1 :as oauth]
            [taoensso.timbre :as timbre :refer (log debug error)]
            [me.narma.config :refer [get-config]]
            [ring.util.response :refer [redirect]]
            [me.narma.auth.protocols :refer :all]
            ))


(def consumer-key (get-config :twitter :key))
(def consumer-secret (get-config :twitter :secret))

(assert consumer-key)

(deftype TwitterBackend [request]
  UserAuthBackend
  (authenticate [_]
     (let [request-token (tw/oauth-request-token
                          consumer-key
                          consumer-secret)
           ]
       (if (oauth/oauth-callback-confirmed? request-token)
         (let [oauth-token (:oauth-token request-token)
               resp (redirect (tw/oauth-authentication-url oauth-token))]
           (assoc-in resp [:session :oauth-token] oauth-token))
         {:status 401
          :body "Not confirmed"})))

  (user-info [_]
     (let [{{access-token :token} :identity} request
           client (tw/oauth-client
                   consumer-key
                   consumer-secret
                   (:oauth-token access-token)
                   (:oauth-token-secret access-token))
           tw-info (client {:method :get
                            :url "https://api.twitter.com/1.1/account/verify_credentials.json"})]
       {:name (:screen-name tw-info)
        :avatar-url (clojure.string/replace
                     (:profile-image-url tw-info)
                     "http://"
                     "https://")}))

  (knock [this]
         (let [{{:keys [oauth_token oauth_verifier]} :params} request]
           (if-not (= oauth_token
                      (get-in request [:session :oauth-token]))
             (do
               (assoc-in [:session :oauth-token] nil)
               {:status 401
                :body "Token mismath"})
           (let [access-token (tw/oauth-access-token
                               consumer-key
                               oauth_token
                               oauth_verifier)]
           (-> (redirect "/")
               (assoc-in [:session :identity] {:token access-token :backend "twitter"})
               (assoc-in [:session :oauth-token] nil)))))))
