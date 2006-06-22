#!/bin/sh

usage ()
{
  echo "$0 src_dir (pwd used if not set)" 1>&2
  echo "$0: Usage Error: $1" 1>&2
  exit 1
}

if_failure ()
{
  if [ "$?" -ne 0 ] ; then
    echo "$0: Failure Error: $1" 1>&2
    exit 1
  fi
}

SRC_DIR=$1
if [ -z "$SRC_DIR" ] ; then
  SRC_DIR=`pwd`
fi
SRC_DIR=`cd "$SRC_DIR" && pwd`
if [ ! -d "${SRC_DIR}" ] ; then
  usage "No $SRC_DIR"
fi

PRG="$0"
PROG_HOME=`dirname "$PRG"`
PROG_HOME=`cd "$PROG_HOME" && pwd`

REVIS=$USER-internal-$$

BUILD_DIR=$SRC_DIR/build-$REVIS/

mkdir -p $BUILD_DIR

GET_BUILDER=$PROG_HOME/dir_get_builder.sh
BUILD_TYPE=`$GET_BUILDER $SRC_DIR`
if_failure "Can't get build type for $REPOS $REVIS $PROJ_DIR"

BUILDER=$PROG_HOME/build_$BUILD_TYPE.sh

OBJ_DIR=$BUILD_DIR/obj
DST_DIR=$BUILD_DIR/dst

`$BUILDER $SRC_DIR $OBJ_DIR $DST_DIR $REVIS`
if_failure "Can't build $BUILDER $SRC_DIR $OBJ_DIR $DST_DIR $REVIS"

echo "Build $BUILD_DIR Succeeded!"

exit 0
