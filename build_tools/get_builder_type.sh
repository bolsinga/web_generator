#!/bin/sh

usage ()
{
  echo "$0 <file list>" 1>&2
  echo "$0: Usage Error: $1" 1>&2
  exit 1
}

# The preferred order is build.xml

TEST_FILES="$@"

for word in $TEST_FILES ; do
  if [ $word = build.xml ] ; then
    echo ant
    exit 0
  fi
done

exit 1
