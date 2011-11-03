#!/bin/sh
# Runs the data relay  

java  -Dlog4j.configuration=file:///home/media/work/p2/log4j.xml   -classpath target/activequant-p2-1.3-SNAPSHOT-jar-with-dependencies.jar:target/ org.activequant.tradesystems.s15.S15




