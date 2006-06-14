#!/bin/sh

usage ()
{
  echo "$0 src_dir"
  echo "Error: $1"
  exit 1
}

SRC_DIR=$1
if [ -z "$SRC_DIR" ] ; then
  usage "No repository."
fi
SRC_DIR=`cd "$SRC_DIR" && pwd`
if [ ! -d "${SRC_DIR}" ] ; then
  usage "No $SRC_DIR"
fi

# The preferred order is *.xcodeproj, build.xml, Makefile

BUILD_FILE=`ls -1d $SRC_DIR/*.xcodeproj &> /dev/null`
if [ "$?" -eq 0 ] ; then
    COUNT=`echo $BUILD_FILE | wc -l`
    if [ "$COUNT" -eq 1 ] ; then
        echo xcode
        exit 0
    fi
    exit 1
fi

BUILD_FILE=`ls -1d $SRC_DIR/build.xml &> /dev/null`
if [ "$?" -eq 0 ] ; then
    echo ant
    exit 0
fi

BUILD_FILE=`ls -1d $SRC_DIR/Makefile &> /dev/null`
if [ "$?" -eq 0 ] ; then
    echo make
    exit 0
fi

exit 1
