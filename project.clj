(defproject metabase/druid-sql-driver "0.1.0"

  :dependencies [[org.apache.calcite.avatica/avatica "1.23.0"]]

  :repl-options {:init-ns metabase.driver.druid-sql}

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.11.1"]
                                       [metabase/metabase "0.45.3"]
                                       [clojure.jdbc/clojure.jdbc-c3p0 "0.3.3"]]}
             :uberjar  {:auto-clean   true
                        :aot          :all
                        :javac-options ["-target" "1.8", "-source" "1.8"]
                        :target-path  "target/uberjar"
                        :uberjar-name "druid-sql.metabase-driver.jar"}}
  )
