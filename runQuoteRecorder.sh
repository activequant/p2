#!/bin/sh
# Runs the data relay  
java  -Dlog4j.configuration=file://./src/main/resources/log4jconfigs/quoterecorder/log4j.xml  -classpath target/activequant-p2-1.3-SNAPSHOT-jar-with-dependencies.jar:target/ -DJMS_HOST=localhost -DJMS_PORT=7676 -DARCHIVE_BASE_FOLDER=/var/www/archive2 org.activequant.util.QuoteRecorder


