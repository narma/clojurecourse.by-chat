(ns me.narma.auth
  (:require [oauth.twitter :as tw]
            [me.narma.config :as config]
            [clojure.java.io :as io]
            [compojure.core :as cmpj]
            [me.narma.log :as log]
  ))


(defmacro unless [condition & body]
  `(when (not ~condition)
    ~@body))

(def consumer-key (:key config/twitter))
(def consumer-secret (:secret config/twitter))


(defn get-request-token [] (let [
                                 token 
                                 (tw/oauth-request-token consumer-key consumer-secret)]
                             (log/info "request token is " token)
                             token))
                            

(def tokens (atom {}))

(defn get-twitter-url [request-token]    
  (swap! tokens assoc (:oauth-token request-token) request-token)
  ;; TODO: clean after timeout for avoud memory leaks
    (tw/oauth-authentication-url  
                             (:oauth-token request-token)))
  
  
(defn get-access-token-twitter [request-token verify]
  (let [token (get @tokens request-token)
        access-token (tw/oauth-access-token consumer-key
                                            (:oauth-token token)
                                         verify)]
    (log/info "access-token is " access-token)
    (do
      (swap! tokens dissoc request-token))
    access-token))

