#!/bin/sh
# Runs the data relay  
java  -classpath target/activequant-p2-1.3-SNAPSHOT-jar-with-dependencies.jar:target/ -DJMS_HOST=localhost -DJMS_PORT=7676 -DSPECIFICATION_ID=85 org.activequant.util.DebugQuoteDumper
