(ns me.narma.oauth
  (:require [oauth.client :as oauth]
            [clojure.edn]
            [clojure.java.io :as io]
            [compojure.core :as cmpj]
            ))

(defmacro unless [condition & body]
  `(when (not ~condition)
    ~@body))

(def config (clojure.edn/read-string (slurp "/etc/apps/narma.me/twitter.edn")))

(def consumer (oauth/make-consumer (:key config)
                                         (:secret config)
                                         "https://api.twitter.com/oauth/request_token"
                                         "https://api.twitter.com/oauth/access_token"
                                         "https://api.twitter.com/oauth/authorize"
                                         :hmac-sha1))

(def tokens (atom {}))

(defn get-request-token []
  (oauth/request-token consumer "https://narma.me/knock"))

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
    (do
      (swap! tokens dissoc request-token))
    access-token))

