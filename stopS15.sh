#!/bin/bash
ps -edf | grep "S15" | cut -c 10-15 | sudo xargs kill 

# build the last candles of the day, the script will do sudo internally 
#sh /home/share/work/bin/generateOHLCData.sh

#echo "Compressing archive"
#cd /home/share/archive/
#udo sh /home/share/archive/compressArchive.sh
# doing two minutes safety sleep before restarting recorders. 
