(ns me.narma.config
  (:require [clojure.edn] [me.narma.log :as log]))

(defn- read-config [filename]
  (try (-> filename slurp clojure.edn/read-string) (catch java.io.FileNotFoundException _ {})))

(def twitter (read-config "/etc/apps/narma.me/twitter.edn"))

; (defn get-config [configs]
;   (apply into (map read-config configs)))
