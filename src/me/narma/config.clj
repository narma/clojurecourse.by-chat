(ns me.narma.config
  (:require [clojure.edn]))

(defn read-config [filename]
  (try (-> filename
           slurp
           clojure.edn/read-string)
    (catch java.io.FileNotFoundException _ {})))

(def config (read-config "/etc/apps/narma.me/config.edn"))

(defn get-config [& keys]
  (get-in config keys))

; (defn get-config [configs]
;   (apply into (map read-config configs)))
