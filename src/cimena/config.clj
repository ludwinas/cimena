(ns cimena.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [environ.core :refer [env]]
            [taoensso.timbre :as timbre]))

(defonce config-data (atom nil))

(def CONFIG-URI (io/resource
                 (if (env :dev)
                   "config/config.dev.edn"
                   "config/config.edn")))

(defn load-config
  [filename]
  (edn/read-string (slurp filename)))

(defn get-config
  []
  @config-data)

(defn init-config
  []
  (let [conf (load-config CONFIG-URI)]
    (reset! config-data conf)))
