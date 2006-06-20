#!/bin/sh

usage ()
{
  echo "$0 repository project_dir revision" 1>&2
  echo "$0: Usage Error: $1" 1>&2
  exit 1
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

PRG="$0"
PROG_HOME=`dirname "$PRG"`
PROG_HOME=`cd "$PROG_HOME" && pwd`

GET_BUILDER_TYPE=$PROG_HOME/get_builder_type.sh

SVNLOOK=/usr/local/bin/svnlook

SVNREVIS="-r $REVIS"

TEST_FILES=`$SVNLOOK tree $SVNREVIS $REPOS $PROJ_DIR | grep "^ \w"`
if [ "$?" -eq 0 ] ; then
    TYPE=`$GET_BUILDER_TYPE $TEST_FILES`
    if [ "$?" -eq 0 ] ; then
      echo $TYPE
      exit 0
    fi
    exit 1
fi

exit 1
