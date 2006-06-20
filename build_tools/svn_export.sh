#!/bin/sh

usage()
{
  echo "$0 repository project_dir revision src_dir" 1>&2
  echo "$0: Usage Error: $1" 1>&2
  exit 1
}

if_failure()
{
  if [ "$?" -ne 0 ] ; then
    echo "$0: Failure Error: $1" 1>&2
    exit 1
  fi
}

REPOS=$1
if [ -z "$REPOS" ] ; then
    usage "No repository"
fi

PROJ_DIR=$2
if [ -z "$PROJ_DIR" ] ; then
    usage "No project_dir"
fi

REVIS=$3
if [ -z "$REVIS" ] ; then
    usage "No revision"
fi

SRC_DIR=$4
if [ -z "$SRC_DIR" ] ; then
    usage "No src_dir"
fi
NAMED_DIR=`basename $SRC_DIR`
SRC_DIR=`dirname $SRC_DIR`
if [ ! -d "$SRC_DIR" ] ; then
  mkdir -p $SRC_DIR
fi
SRC_DIR=`cd "$SRC_DIR" && pwd`

cd $SRC_DIR ; svn export -q -r $REVIS file://$REPOS/$PROJ_DIR $NAMED_DIR
if_failure "Can't export $PROJ_DIR"
