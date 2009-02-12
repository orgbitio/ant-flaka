#!/bin/sh

# This file is in bin
## resolve links - $0 may be a link
PRG="$0"
progname=$(basename "$0")

# need this for relative symlinks
while test -h "$PRG"  
do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null
  then
    PRG="$link"
  else
    PRG=$(dirname "$PRG")"/$link"
  fi
done

FLAKA_HOME=$(dirname "$PRG")"/.."
ant=ant

# make it fully qualified
FLAKA_HOME=$(cd "$FLAKA_HOME" > /dev/null && pwd)

L=$(find "$FLAKA_HOME/test" -name test-\*.xml)
G=""
F=""
for x in ${L} ; do
  /bin/echo -n testing $x ..
  if $ant --noconfig -lib $FLAKA_HOME/build/dist -f $x 1>/dev/null 2>&1 ; then
    G="$G $x"
    /bin/echo "ok"
  else
    F="$F $x"
    /bin/echo "no"
  fi
done

test -z "$F" && { 
  exit 0; 
}
cat <<EOF

===============================================================
FAILED
===============================================================
EOF
for x in $F ; do
  echo $x
done
