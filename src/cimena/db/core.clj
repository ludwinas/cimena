(ns cimena.db.core
  (:require
    [clojure.java.jdbc :as jdbc]
    [yesql.core :refer [defqueries]]
    [cheshire.core :refer [generate-string parse-string]])
  (:import org.postgresql.util.PGobject
           org.postgresql.jdbc4.Jdbc4Array
           clojure.lang.IPersistentMap
           clojure.lang.IPersistentVector
           [java.sql Date Timestamp PreparedStatement]))

(defn init-db-spec
  [cfg]
  {:subprotocol "postgresql"
   :subname (format "//%s/%s" (:hostname cfg) (:dbname cfg))
   :user (:user cfg)
   :password (:password cfg)})

(defonce ^:dynamic *db-spec* nil)

(defn wrap-with-db-spec
  "Wrap query functions generated by yesql to use the *db-spec* by default."
  [query-fn-vars]
  (doseq [qvar query-fn-vars]
    ;; We wrap the functions while keeping their names by altering the Vars that
    ;; store the functions. We swap out their value (the query fn) with a
    ;; wrapped copy. Afterwards, any usage of the Var (e.g. a function call)
    ;; will use the wrapped function.
    ;;
    ;; Note that the query functions can still be called with custom call
    ;; options, which can containn a custom :connection that overrides the
    ;; default of using *db-spec*.
    (alter-var-root
     qvar
     (fn apply-wrapper [wrapped-fn]
       ;; Return the actual wrapped function that inserts the db spec, keeping
       ;; the old meta intact (note that the meta is now slightly wrong in its
       ;; arglists)
       (with-meta
         (fn db-spec-wrapper-fn
           ([]
            (db-spec-wrapper-fn {} {}))
           ([args]
            (db-spec-wrapper-fn args {}))
           ([args call-options]
            ;; Insert our dynamic var into call opts, if :connection is not yet
            ;; present there
            (wrapped-fn args (merge {:connection *db-spec*} call-options))))
         (meta wrapped-fn))))))

(wrap-with-db-spec
 (defqueries "sql/queries.sql"))

(defn to-date [sql-date]
  (-> sql-date (.getTime) (java.util.Date.)))

(extend-protocol jdbc/IResultSetReadColumn
  Date
  (result-set-read-column [v _ _] (to-date v))

  Timestamp
  (result-set-read-column [v _ _] (to-date v))

  Jdbc4Array
  (result-set-read-column [v _ _] (vec (.getArray v)))

  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (parse-string value true)
        "jsonb" (parse-string value true)
        "citext" (str value)
        value))))

(extend-type java.util.Date
  jdbc/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (Timestamp. (.getTime v)))))

(defn to-pg-json [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(extend-protocol jdbc/ISQLValue
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value)))
