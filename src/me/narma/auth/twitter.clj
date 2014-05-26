(ns me.narma.auth.twitter
  (:require [oauth.twitter :as tw]
            [me.narma.config :as config]
            [clojure.java.io :as io]
  ))


(def consumer-key (:key config/twitter))
(def consumer-secret (:secret config/twitter))

(defn custom-log [msg]
  (spit "/tmp/log.txt" (str msg "\n") :append true)
  msg)

(defn twitter []
  (let [request-token (tw/oauth-request-token
                        consumer-key
                        consumer-secret)
        ]
    (custom-log (str "request-token " request-token))
    (tw/oauth-authentication-url (:oauth-token request-token))))

(defn twitter-knock [oauth-token oauth-verifier]
  (let [access-token (tw/oauth-access-token
                       consumer-key
                       oauth-token
                       oauth-verifier)
        _ (custom-log (str "access_token " access-token))
        client (tw/oauth-client
                 consumer-key
                 consumer-secret
                 (:oauth-token access-token)
                 (:oauth-token-secret access-token))]
    (try
      (custom-log (client {:method :get
                         :url "https://api.twitter.com/1.1/account/verify_credentials.json"}))
    (catch
      Exception e (custom-log (str (.getMessage e)))))))

