(ns metabase.driver.druid-sql
  (:require [metabase.driver :as driver]
            [metabase.driver.sql.query-processor :as sql.qp]
            [metabase.driver.sql-jdbc.connection :as sql-jdbc.conn]
            [metabase.driver.sql-jdbc.sync :as sql-jdbc.sync]
            [metabase.driver.sql-jdbc.execute :as sql-jdbc.exec]
            [metabase.driver.sql.util.unprepare :as sql-jdbc.unprepare]
            [metabase.util.honeysql-extensions :as hx]
            [honeysql.core :as hsql]
            [java-time.format :as time-format])
  (:import (java.sql Types)
           (java.time ZonedDateTime)))

;;; +----------------------------------------------------------------------------------------------------------------+
;;; |                                             metabase.driver impls                                              |
;;; +----------------------------------------------------------------------------------------------------------------+

(driver/register! :druid-sql, :parent #{:sql-jdbc})

(defmethod driver/database-supports? [:druid-sql :case-sensitivity-string-filter-options] [_ _ _] true)
(defmethod driver/database-supports? [:druid-sql :date-arithmetics] [_ _ _] true)
(defmethod driver/database-supports? [:druid-sql :temporal-extract] [_ _ _] true)
(defmethod driver/supports? [:druid-sql :standard-deviation-aggregations] [_ _] false)

;;; +----------------------------------------------------------------------------------------------------------------+
;;; |                                         metabase.driver.sql-jdbc impls                                         |
;;; +----------------------------------------------------------------------------------------------------------------+

(defmethod sql-jdbc.conn/connection-details->spec :druid-sql [_ {:keys [host port]}]
  {:classname   "org.apache.calcite.avatica.remote.Driver"
   :subprotocol "avatica"
   :subname     (str "remote:url=http://" host ":" port "/druid/v2/sql/avatica/")})

(def ^:private druid-base-types
  "Map of Druid column types -> Field base types."
  {:TIMESTAMP :type/DateTime
   :VARCHAR   :type/Text
   :DECIMAL   :type/Decimal
   :FLOAT     :type/Float
   :REAL      :type/Float
   :DOUBLE    :type/Float
   :BIGINT    :type/BigInteger})

(defmethod sql-jdbc.sync/database-type->base-type :druid-sql
  [_ column]
  (druid-base-types column :type/*))

(defmethod sql-jdbc.exec/read-column-thunk [:druid-sql Types/TIMESTAMP]
  [_ rs _ i]
  (fn []
    (.getObject rs i)))

(defmethod sql-jdbc.exec/set-parameter [:druid-sql ZonedDateTime]
  [_driver ps i t]
  (sql-jdbc.exec/set-parameter _driver ps i (time-format/format "yyyy-MM-dd HH:mm:ss" t)))

(defmethod sql-jdbc.unprepare/unprepare-value [:druid-sql ZonedDateTime]
  [_ t]
  (format "'%s'" (time-format/format "yyyy-MM-dd HH:mm:ss" t)))

;;; +----------------------------------------------------------------------------------------------------------------+
;;; |                                           metabase.driver.sql impls                                            |
;;; +----------------------------------------------------------------------------------------------------------------+

(defn- date-trunc [unit expr] (hsql/call :date_trunc (hx/literal unit) expr))

(defmethod sql.qp/date [:druid-sql :default] [_ _ expr] expr)
(defmethod sql.qp/date [:druid-sql :minute] [_ _ expr] (date-trunc :minute expr))
(defmethod sql.qp/date [:druid-sql :hour] [_ _ expr] (date-trunc :hour expr))
(defmethod sql.qp/date [:druid-sql :day] [_ _ expr] (date-trunc :day expr))
(defmethod sql.qp/date [:druid-sql :week] [_ _ expr] (date-trunc :week expr))
(defmethod sql.qp/date [:druid-sql :month] [_ _ expr] (date-trunc :month expr))
(defmethod sql.qp/date [:druid-sql :quarter] [_ _ expr] (date-trunc :quarter expr))
(defmethod sql.qp/date [:druid-sql :year] [_ _ expr] (date-trunc :year expr))

(defn- time-extract [unit expr] (hsql/call :time_extract expr (hx/literal unit)))

(defmethod sql.qp/date [:druid-sql :minute-of-hour] [_ _ expr] (time-extract :minute expr))
(defmethod sql.qp/date [:druid-sql :hour-of-day] [_ _ expr] (time-extract :hour expr))
(defmethod sql.qp/date [:druid-sql :day-of-week] [_ _ expr] (time-extract :dow expr))
(defmethod sql.qp/date [:druid-sql :day-of-month] [_ _ expr] (time-extract :day expr))
(defmethod sql.qp/date [:druid-sql :day-of-year] [_ _ expr] (time-extract :doy expr))
(defmethod sql.qp/date [:druid-sql :week-of-year] [_ _ expr] (time-extract :week expr))
(defmethod sql.qp/date [:druid-sql :month-of-year] [_ _ expr] (time-extract :month expr))
(defmethod sql.qp/date [:druid-sql :quarter-of-year] [_ _ expr] (time-extract :quarter expr))

(defmethod sql.qp/current-datetime-honeysql-form :druid-sql
  [_]
  (hsql/raw "CURRENT_TIMESTAMP"))

(defmethod sql.qp/add-interval-honeysql-form :druid-sql
  [_ hsql-form amount unit]
  (hsql/call :TIMESTAMPADD (hsql/raw (name unit)) amount hsql-form))
