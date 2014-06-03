(ns chat.service
  (:require
   ;; <other stuff>
   [ajax.core :refer [GET POST]]
   [reagent.core :as reagent :refer [atom]]
  ))

(def messages (atom []))
(def channels (atom []))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "Something bad happened: " status " " status-text)))


(defn msg-send [msg]
  (POST "/messages/create"
        {:params {:msg msg}
        :error-handler error-handler}))

(defn load-history []
  (GET "/messages/list"
       {:error-handler error-handler
        :handler #(doseq [msg %]
                    (println "import " msg)
                       (swap! messages conj msg))}))

(defn pooling []
  (GET "/messages/poll"
       {:error-handler error-handler
        :handler (fn [event]
                   (case (:type event)
                     "message"
                     (swap! messages conj event)
                     (println "unknown msg" event))
                   (pooling))}
       ))

(defn start []
  (pooling))
  ; start pooling

