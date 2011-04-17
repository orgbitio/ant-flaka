#!/bin/sh

rm -rf tmp && mkdir tmp || {
  echo "error"
  exit 1
}
ant-last -f legacy.xml && cp build/dist/flaka-S*.jar tmp 
ant -lib tmp/flaka-SNAPSHOT.jar && cp build/dist/flaka-S*.jar tmp/bad.jar
ant -lib tmp/bad.jar
