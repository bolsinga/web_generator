#!/bin/sh

usage()
{
  echo "$0 repository project_dir revision export_dir" 1>&2
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

EXPORT_DIR=$4
if [ -z "$EXPORT_DIR" ] ; then
    usage "No export_dir"
fi
NAMED_DIR=`basename $EXPORT_DIR`
EXPORT_DIR=`dirname $EXPORT_DIR`
if [ ! -d "$EXPORT_DIR" ] ; then
  mkdir -p $EXPORT_DIR
fi
EXPORT_DIR=`cd "$EXPORT_DIR" && pwd`

echo "SVN Export $REPOS/$PROJ_DIR -r $REVIS to $EXPORT_DIR/$NAMED_DIR" 1>&2

cd $EXPORT_DIR ; svn export -r $REVIS file://$REPOS/$PROJ_DIR $NAMED_DIR 1>&2
if_failure "Can't export $PROJ_DIR"
