(ns harvesters.core
  (:gen-class)
  (:require [harvesters.core]
            [harvesters.config :refer [cfg]]
            [harvesters.events-processing :as e-p]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async :refer [chan >!!]])
  (:import (org.lwjgl.glfw GLFWErrorCallback)
           (org.lwjgl.opengl GL GL11)
           (org.lwjgl.glfw GLFW GLFWErrorCallback GLFWFramebufferSizeCallback)))


(def window-title "Harvesters")

(def ^:private resize-chan (chan (async/sliding-buffer 6)))
(def resize-pub (async/pub resize-chan :event-type))




(defn- init-window [{:keys [width height maximized] :or {width 800 height 600}}]
  (log/debug "Trying to create window using params:" [width height maximized])
  (GLFW/glfwSetErrorCallback (GLFWErrorCallback/createPrint System/err))
  (when-not (GLFW/glfwInit)
    (throw (IllegalStateException. "Unable to initialize GLFW")))
  (GLFW/glfwDefaultWindowHints)
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)
  (when maximized (GLFW/glfwWindowHint GLFW/GLFW_MAXIMIZED GLFW/GLFW_TRUE))
  (let [window (or (GLFW/glfwCreateWindow width height window-title 0 0) (throw (RuntimeException. "Failed to create the GLFW window")))
        width-buf (int-array 1)
        height-buf (int-array 1)]
    (GLFW/glfwMakeContextCurrent window)
    (GLFW/glfwSwapInterval 1)
    (GLFW/glfwShowWindow window)
    (GLFW/glfwGetFramebufferSize window width-buf height-buf)
    {:window              window
     :frame-buffer-width  (aget width-buf 0)
     :frame-buffer-height (aget height-buf 0)}))

(defn init-gl [width height]
  (GL/createCapabilities)
  (log/debug "OpenGL version:" (GL11/glGetString GL11/GL_VERSION))
  (GL11/glClearColor 0.0 0.0 0.0 0.0)
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glOrtho 0.0 width
                0.0 height
                -1.0 1.0)
  (GL11/glMatrixMode GL11/GL_MODELVIEW))

(defn draw []
  (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
  (GL11/glLoadIdentity)
  (GL11/glBegin GL11/GL_TRIANGLES)
  (do
    (GL11/glColor3f 1.0 0.0 0.0)
    (GL11/glVertex2i 100 200)
    (GL11/glColor3f 0.0 1.0 0.0)
    (GL11/glVertex2i 100 100)
    (GL11/glColor3f 0.0 0.0 1.0)
    (GL11/glVertex2i 200 100))
  (GL11/glEnd))

(defn main-loop [window]
  (while (not (GLFW/glfwWindowShouldClose window))
    (draw)
    (GLFW/glfwSwapBuffers window)
    (GLFW/glfwPollEvents)))

(defn -main []
  (log/info (apply str (take 80 (repeat "*"))))
  (try
    (let [{:keys [window frame-buffer-width frame-buffer-height]} (init-window (:window @cfg))]
      (init-gl frame-buffer-width frame-buffer-height)
      (e-p/attach-event-callbacks window)
      (GLFW/glfwSetFramebufferSizeCallback window (proxy [GLFWFramebufferSizeCallback] []
                                                    (invoke [])))
      (main-loop window)
      (GLFW/glfwDestroyWindow window))
    (finally
      (GLFW/glfwTerminate))))
