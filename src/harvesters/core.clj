(ns harvesters.core
  (:gen-class)
  (:require [harvesters.core]
            [harvesters.config :refer [cfg]]
            [clojure.tools.logging :as log])
  (:import (org.lwjgl.glfw GLFWErrorCallback)
           (org.lwjgl.opengl GL GL11)
           (org.lwjgl.glfw GLFW GLFWErrorCallback GLFWKeyCallback)))


(def window-title "Harvesters")

(defonce globals (atom {:errorCallback nil
                        :keyCallback   nil
                        :window        nil
                        :width         0
                        :height        0
                        :title         "none"
                        :angle         0.0
                        :last-time     0}))

(defn- mk-window [{:keys [width height maximized] :or {width 800 height 600}}]
  (log/debug "Trying to create window using params:" [width height maximized])
  (GLFW/glfwSetErrorCallback (GLFWErrorCallback/createPrint System/err))
  (when-not (GLFW/glfwInit)
    (throw (IllegalStateException. "Unable to initialize GLFW")))
  (GLFW/glfwDefaultWindowHints)
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)
  (when maximized (GLFW/glfwWindowHint GLFW/GLFW_MAXIMIZED GLFW/GLFW_TRUE))
  (let [window (or (GLFW/glfwCreateWindow width height window-title 0 0) (throw (RuntimeException. "Failed to create the GLFW window")))
        [res-width res-height] (if-not maximized [width height]
                                                 (let [width-buf (int-array 1)
                                                       height-buf (int-array 1)]
                                                   (GLFW/glfwGetWindowSize window width-buf height-buf)
                                                   [(aget width-buf 0) (aget height-buf 0)]))]
    (GLFW/glfwMakeContextCurrent window)
    (GLFW/glfwSwapInterval 1)
    (GLFW/glfwShowWindow window)
    {:window window
     :width  res-width
     :height res-height}))

(defn init-gl [width height]
  (GL/createCapabilities)
  (println "OpenGL version:" (GL11/glGetString GL11/GL_VERSION))
  (GL11/glClearColor 0.0 0.0 0.0 0.0)
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glOrtho 0.0 width
                0.0 height
                -1.0 1.0)
  (GL11/glMatrixMode GL11/GL_MODELVIEW))

(defn draw [width height]
  (let [{:keys [angle]} @globals
        w2 (/ width 2.0)
        h2 (/ height 2.0)]
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
    (GL11/glLoadIdentity)
    (GL11/glTranslatef w2 h2 0)
    (GL11/glRotatef angle 0 0 1)
    (GL11/glScalef 2 2 1)
    (GL11/glBegin GL11/GL_TRIANGLES)
    (do
      (GL11/glColor3f 1.0 0.0 0.0)
      (GL11/glVertex2i 100 0)
      (GL11/glColor3f 0.0 1.0 0.0)
      (GL11/glVertex2i -50 86.6)
      (GL11/glColor3f 0.0 0.0 1.0)
      (GL11/glVertex2i -50 -86.6))
    (GL11/glEnd)))

(defn update-globals []
  (let [{:keys [angle last-time]} @globals
        cur-time (System/currentTimeMillis)
        delta-time (- cur-time last-time)
        next-angle (+ (* delta-time 0.00005) angle)
        next-angle (if (>= next-angle 360.0)
                     (- next-angle 360.0)
                     next-angle)]
    (swap! globals assoc
           :angle next-angle
           :last-time cur-time)))

(defn main-loop [window width height]
  (while (not (GLFW/glfwWindowShouldClose window))
    (update-globals)
    (draw width height)
    (GLFW/glfwSwapBuffers window)
    (GLFW/glfwPollEvents)))

(defn -main []
  (try
    (let [{:keys [window width height]} (mk-window (:window @cfg))]
      (init-gl width height)
      (main-loop window width height)
      #_(.free (:errorCallback @globals))
      #_(.free (:keyCallback @globals))
      (GLFW/glfwDestroyWindow window))
    (finally
      (GLFW/glfwTerminate))))