#!/bin/sh

usage ()
{
  echo "$0 src_dir obj_dir sym_dir dst_dir build_id" 1>&2
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
  usage "No source directory."
fi
if [ ! -d "${SRC_DIR}" ] ; then
  usage "No $SRC_DIR"
fi
SRC_DIR=`cd "$SRC_DIR" && pwd`
OBJ_DIR=$2
if [ -z "$OBJ_DIR" ] ; then
  usage "No objects directory."
fi
SYM_DIR=$3
if [ -z "$SYM_DIR" ] ; then
  usage "No symbols directory."
fi
DST_DIR=$4
if [ -z "$DST_DIR" ] ; then
  usage "No destination directory."
fi
BUILD_ID=$5
if [ -z "$BUILD_ID" ] ; then
  BUILD_ID=$USER-internal
fi

ARGS="SYMROOT=$SYM_DIR OBJROOT=$OBJ_DIR DSTROOT=$DST_DIR BUILD_ID=$BUILD_ID"
echo "xcodebuild $ARGS" 1>&2

cd $SRC_DIR ; xcodebuild $ARGS 1>&2
if_failure "Xcode Build Failure!"

exit 0
