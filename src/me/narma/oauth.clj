(ns me.narma.oauth
  (:require [oauth.client :as oauth]
            [me.narma.config :as config]
            [clojure.java.io :as io]
            [compojure.core :as cmpj]
            [me.narma.log :as log]
  ))


(defmacro unless [condition & body]
  `(when (not ~condition)
    ~@body))

(def consumer (oauth/make-consumer (:key config/twitter)
                                         (:secret config/twitter)
                                         "https://api.twitter.com/oauth/request_token"
                                         "https://api.twitter.com/oauth/access_token"
                                         "https://api.twitter.com/oauth/authorize"
                                         :hmac-sha1))

(def tokens (atom {}))

(defn get-request-token []
  (let [token (oauth/request-token consumer "https://narma.me/knock")]
    (log/info "requested token is " token)
  token))

(defn get-twitter-url [request-token]    
  (swap! tokens assoc (:oauth_token request-token) request-token)
  ;; TODO: clean after timeout for avoud memory leaks
    (oauth/user-approval-uri consumer 
                             (:oauth_token request-token)))
  
  
(defn get-access-token-twitter [request-token verify]
  (let [token (get @tokens request-token)
        access-token (oauth/access-token consumer
                                         request-token
                                         verify)]
    (log/info "token response is" token)
    (log/info "verify is " verify)
    (log/info "str token is " request-token)
    (log/info "access-token is " access-token)
    (do
      (swap! tokens dissoc request-token))
    access-token))

