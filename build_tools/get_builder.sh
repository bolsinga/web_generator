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

PRG="$0"
PROG_HOME=`dirname "$PRG"`
PROG_HOME=`cd "$PROG_HOME" && pwd`

GET_BUILDER_TYPE=$PROG_HOME/get_builder_type.sh

TEST_FILES=`ls -1 $SRC_DIR`
if [ "$?" -eq 0 ] ; then
    TYPE=`$GET_BUILDER_TYPE $TEST_FILES`
    if [ "$?" -eq 0 ] ; then
      echo $TYPE
      exit 0
    fi
    exit 1
fi

exit 1
