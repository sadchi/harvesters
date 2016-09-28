(ns harvesters.config
  (:require [clojure.edn :as e]))

(def cfg-path ^:const "./cfg/config.clj")

(def cfg (promise))

(deliver cfg (e/read-string (slurp cfg-path)))





