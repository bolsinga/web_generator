#!/bin/sh

usage ()
{
  echo "$0 src_dir obj_dir dst_dir" 1>&2
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
DST_DIR=$3
if [ -z "$DST_DIR" ] ; then
  usage "No destination directory."
fi

cd $SRC_DIR ; make SRC_DIR=$SRC_DIR OBJ_DIR==$OBJ_DIR DST_DIR=$DST_DIR 1>&2
if_failure "Make Build Failure!"

exit 0
