#!/bin/bash

#FICH=$(basename $1)
listing="out.txt"

nice -5 castem20 $* > $listing &

PID_CAST3M=$!
echo $PID_CAST3M >> PID
wait $PID_CAST3M
rm -f PID
# Get the exit code (4 lines before end of file)
EXIT_CODE=`tail -n 4 $listing | head -n 1 | sed 's/\*//g' | cut -f2 -d ":" | sed 's/ //g'`
echo "Exit code: $EXIT_CODE"
exit $EXIT_CODE