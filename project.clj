(defproject harvesters "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.lwjgl/lwjgl "3.0.0"]]

  :java-opts ["-Dorg.lwjgl.util.Debug=true" "-Dorg.lwjgl.librarypath=native"]

  :main ^:skip-aot harvesters.core
  :resource-paths ["resources" "native"]
  :source-path "src"
  :profiles {:uberjar {:aot :all}})
