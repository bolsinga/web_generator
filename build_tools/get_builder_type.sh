#!/bin/sh

usage ()
{
  echo "$0 <file list>" 1>&2
  echo "$0: Usage Error: $1" 1>&2
  exit 1
}

# The preferred order is *.xcodeproj, build.xml, Makefile

TEST_FILES="$@"

for word in $TEST_FILES ; do
  # Get extension
  XCODE=${word#*.*}
  # Strip trailing '/'
  XCODE=${XCODE%/}
  if [ $XCODE = "xcodeproj" ] ; then
    echo xcode
    exit 0
  fi
  if [ $word = build.xml ] ; then
    echo ant
    exit 0
  fi
  if [ $word = Makefile ] ; then
    echo make
    exit 0
  fi
done

exit 1
