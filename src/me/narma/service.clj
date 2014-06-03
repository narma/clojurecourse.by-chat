(ns me.narma.service
  (:use compojure.core
        org.httpkit.server)
  (:require
            [datomic.api :as d]
            [taoensso.timbre :as timbre :refer (log debug)]
            [clojure.java.io :as io]
            [clj-json.core :as json]))

(def db-url "datomic:free://127.0.0.1:4334/chat")
(def schema-tx (read-string (slurp (io/resource "db/schema.edn"))))

(defn now []
  (java.util.Date.))

;; init db

(def clients (atom {}))

(defn response [data & [status]]
  {:status (or status 200)
   :header {"Content-Type" "application/edn; charset=utf-8"}
   :body (pr-str data)})

(d/delete-database db-url)
(d/create-database db-url)
(def conn (d/connect db-url))
@(d/transact conn schema-tx)

(defn poll-mesg [req]
  (with-channel req channel
    (swap! clients assoc channel (get-in req [:user :dbid]))
    (on-close channel (fn [status]
                        (swap! clients dissoc channel)))))

(defn send-all! [msg]
  (for [ch (keys @clients)]
    (send! ch (response msg))))

(defn create-user [& [{:keys [provider id name avatar]}]]
   @(d/transact conn [
        {:db/id (d/tempid :db.part/user)
         :user/pid id
         :user/provider provider
         :user/name name
         :user/avatar avatar}]))

(defn users []
  (d/q '[:find ?id ?name ?provider ?pid
         :where [?id :user/name ?name]
                [?id :user/provider ?provider]
                [?id :user/pid ?pid]]
       (d/db conn)))

(defn get-user [db pid provider]
  (-> (d/q '[:find ?id ?name ?avatar
         :in $ ?pid ?provider
         :where
           [?id :user/pid ?pid]
           [?id :user/provider ?provider]
           [?id :user/name ?name]
           [?id :user/avatar ?avatar]]
       db pid provider)
      first))

(defn on-user-auth [user-info request]
  (let [backend (get-in request [:session :identity :backend])
        id (str (:id user-info))
        user (get-user (d/db conn) id backend)]
    (if user
      {:dbid (first user)}
      {:dbid (-> (create-user
           {:provider backend
            :name (:name user-info)
            :avatar (:avatar-url user-info)
            :id id})
          :tempids
          vals
          first)}
      )))

(defn message-create [msg user]
  (let [created (now)]
    @(d/transact conn [
          {:db/id (d/tempid :db.part/user)
           :message/body msg
           :message/author (:dbid user)
           :message/created created
           }])
  (send-all! {:type "message"
              :msg msg
              :created created
              :user user
             })))

(defn messages-list [req]
  (->> (d/q '[:find ?created ?uid ?name ?body ?avatar
              :where
              [?uid :user/name ?name]
              [?uid :user/avatar ?avatar]
              [?id :message/author ?uid]
              [?id :message/body ?body]
              [?id :message/created ?created]]
            (d/db conn))
       (sort-by first)
       (mapv (fn [[created uid uname body avatar]]
              {:msg body
                :created created
                :user {:dbid uid
                       :name uname
                       :avatar-url avatar}}))
       response))



(defroutes messages-routes
  (GET "/poll" [] poll-mesg)
  (POST "/create" {{:keys [msg]} :params user :user} (message-create msg user))
  (GET "/list" [] messages-list))
