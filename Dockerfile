FROM metabase/metabase:v0.45.3

COPY /target/uberjar/druid-sql.metabase-driver.jar /plugins/druid-sql.metabase-driver.jar