(ns harvesters.config
  (:require [clojure.edn :as e]))

(def cfg-path ^:const "./cfg/config.clj")

(def cfg (promise))

(defn update-config [] )

(deliver cfg (e/read-string (slurp cfg-path)))





