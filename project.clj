(defproject harvesters "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.lwjgl/lwjgl "3.0.0"]
                 [org.apache.logging.log4j/log4j-core "2.4"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.4"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.2.391"]]

  :java-opts ["-Dorg.lwjgl.util.Debug=true" "-Dorg.lwjgl.librarypath=native"]

  :main ^:skip-aot harvesters.core
  :resource-paths ["resources" "cfg"]
  :source-path "src"
  :profiles {:uberjar {:aot :all}})
