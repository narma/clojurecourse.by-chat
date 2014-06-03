(ns chat.application
  (:require [goog.events :as e]
            [chat.service :as service]
            [chat.render :as render]))


(defn ^:export main []
    (enable-console-print!)
    (service/start)
    (render/main true))
