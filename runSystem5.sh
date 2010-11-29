#!/bin/sh
# Runs the data relay  
java  -Dlog4j.configuration=file:///home/ustaudinger/work/p2-trunk/p2/src/main/resources/log4jconfigs/system5/log4j.xml  -classpath target/activequant-p2-1.3-SNAPSHOT-jar-with-dependencies.jar:target/ org.activequant.production.InMemoryAlgoEnvConfigRunner org.activequant.tradesystems.system5.AlgoEnvConfigSystem5a
