#!/bin/sh
# Runs the data relay  
cd /home/ustaudinger/p2-work/activequant-p2
java -classpath target/activequant-p2-1.3-SNAPSHOT-jar-with-dependencies.jar:target/ -DJMS_HOST=localhost -DJMS_PORT=7676 -DARCHIVE_BASE_FOLDER=/var/www/archive2 org.activequant.util.DataRelay
