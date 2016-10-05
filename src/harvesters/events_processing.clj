(ns harvesters.events-processing
  (:require [clojure.core.async :as async :refer [chan >!!]]
            [clojure.core.async.impl.protocols :as p])
  (:import (org.lwjgl.glfw GLFW GLFWKeyCallback GLFWCursorPosCallback GLFWMouseButtonCallback GLFWCharCallback)))

(set! *warn-on-reflection* true)

(def ^:private events-consumers-queue (atom []))

(defn- propagate-event [event-type-keyword & opts]
  (let [output-chan (last @events-consumers-queue)]
    (when (satisfies? p/WritePort output-chan)
      (>!! output-chan [event-type-keyword opts]))))


(defn push-consumer [x]
  (swap! events-consumers-queue conj x))

(defn pop-consumer []
  (swap! events-consumers-queue pop))

(defn attach-event-callbacks [window]
  (GLFW/glfwSetKeyCallback window (proxy [GLFWKeyCallback] []
                                    (invoke [window key scancode action mods]
                                      (propagate-event :key key scancode action mods))))
  (GLFW/glfwSetCursorPosCallback window (proxy [GLFWCursorPosCallback] []
                                          (invoke [window xpos ypos]
                                            (propagate-event :cursor-pos xpos ypos))))
  (GLFW/glfwSetMouseButtonCallback window (proxy [GLFWMouseButtonCallback] []
                                            (invoke [window button action mods]
                                              (propagate-event :mouse-button button action mods)))))


