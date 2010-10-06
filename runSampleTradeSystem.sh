#!/bin/sh
# Runs the data relay  
java  -classpath target/activequant-p2-1.3-SNAPSHOT-jar-with-dependencies.jar:target/ org.activequant.production.InMemoryAlgoEnvConfigRunner org.activequant.tradesystems.template.AlgoEnvConfigSample
