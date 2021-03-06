#!/bin/bash

tr -d '\r' < $1 > $1.nor
mv $1.nor $1

#FICH=$(basename $1)
listing="castem.out"

export NCPU=""
NCPU_in=`grep "NCPU " $1 | sed 's/.*NCPU //' | tr '\n' ' ' | tr -d '\r' | cut -d' ' -f1`
  echo "parse NCPU "$NCPU_in
if [ ! "$NCPU_in""zz" = "zz" ] ; then
  export NCPU="-NCPU $NCPU_in"
fi
echo "NCPU: "$NCPU

nice -5 castem20 $1 > $listing &

PID_CAST3M=$!
echo $PID_CAST3M >> PID
wait $PID_CAST3M
rm -f PID

if [[ ! -f $listing ]]; then
  echo "No output file!"
  exit 666
fi

# cleanup null chars
#cat $listing | tr -d '\000' > $listing

# Get the exit code (4 lines before end of file)
EXIT_CODE=`tail -n 4 $listing | head -n 1 | sed 's/\*//g' | cut -f2 -d ":" | sed 's/ //g'`
echo "Exit code: $EXIT_CODE"
exit $EXIT_CODE
