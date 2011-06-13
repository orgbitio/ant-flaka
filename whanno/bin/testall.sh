#!/bin/sh

# This file is in bin
## resolve links - $0 may be a link
PRG="$0"
progname=$(basename "$0")

##
## programs
##
echon=/bin/echo

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


cat <<EOF
======================================================================
Warning: using [1] for testing. Make sure that this file is up-to-date
before testing. You can do this by running

 ant package

[1] $FLAKA_HOME/ant-flaka.jar
======================================================================
EOF

L=$(find "$FLAKA_HOME/test" -name test-\*.xml)
G=""
F=""
for x in ${L} ; do
  cmd="$ant --noconfig -lib $FLAKA_HOME/ant-flaka.jar -f $x"
  $echon $cmd
  if $cmd 1>/dev/null 2>&1 
  then
    G="$G $x"
  else
    F="$F:$cmd"
    $echon '# >> no << '
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
_ifs=$IFS
IFS=:
for x in $F ; do
  echo $x
done
IFS=${_ifs}