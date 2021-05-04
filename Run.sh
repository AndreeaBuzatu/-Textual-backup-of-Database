#!/bin/bash
javac *.java
echo "Original database name: "
read dbName
java -classpath ".:sqlite-jdbc-3.7.2.jar" Coursework $dbName.db  > statements.sql
echo "Duplicate database name: "
read duplicate
sqlite3 $duplicate.db < statements.sql



