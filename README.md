
**NOTE:** This driver implementation is no longer relevant. Starting from [Metabase v50](https://www.metabase.com/releases/metabase-50#smaller-but-still-cool-stuff) Druid JDBC driver is included as a built-in driver.
 
# Metabase Druid Driver (SQL version)

The driver allows Metabase to query Apache Druid database using [Druid SQL](https://druid.apache.org/docs/latest/querying/sql-jdbc.html) language.

Unlike the built-in driver for Druid, this driver leverages a JDBC connection and inherits advantageous features from the parent `sql-jdbc` driver.

## Build

Build command: `lein uberjar`

Result file location: `/targer/uberjar/druid-sql.metabase-driver.jar`

## Installation

1. build the jar
2. put the driver jar file into metabase plugin folder
3. restart metabase

## Run locally

```bash

lein do clean, uberjar

docker build -t metabase/druid .

docker run --name metabase-druid -p 3000:3000 metabase/druid

```
