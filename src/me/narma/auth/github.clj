(ns me.narma.auth.github
  (:require [oauth.github :as gh]
            [oauth.v2 :as oauth]
            [taoensso.timbre :as timbre :refer (log debug error)]
            [me.narma.config :refer [get-config]]
            [me.narma.auth :refer [UserAuthBackend]]
  ))


(def client-id (get-config :github :client-id))
(def client-secret (get-config :github :client-secret))
