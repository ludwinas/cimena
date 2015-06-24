(ns cimena.lib.util
  (:require [clojure.data :as data]))

(defn int-or-nil [x]
  (if (integer? x)
    x
    (try (Integer/parseInt x)
         (catch NumberFormatException e nil))))

(defn into-a-vec [item]
  "transforms the given item into a vector, no matter if it's a collection or a
  single item"
  (if (coll? item)
    (vec item)
    [item]))

(defn get-item-with-keyword [keyword id coll]
  (some #(when (= id (keyword %)) %)
        coll))

(defn data-diff [coll1 coll2]
  "expects two list of integers, normalizes to int before comparing"
  (let [first-set (set (map int-or-nil coll1))
        second-set (set (map int-or-nil coll2))]
    (data/diff first-set second-set)))

