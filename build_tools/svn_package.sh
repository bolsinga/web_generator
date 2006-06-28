#!/bin/sh

usage ()
{
  echo "$0 repository revision project_directory project_name" 1>&2
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

LOG_FILE=/tmp/build-$USER-$$.log
(
exec 2>&1

REPOS=$1
if [ -z "$REPOS" ] ; then
  usage "No repository."
fi
REPOS=`cd "$REPOS" && pwd`
if [ ! -d "${REPOS}" ] ; then
  usage "No $REPOS"
fi

REVIS=$2
if [ -z "$REVIS" ] ; then
  usage "No revision."
fi

PROJ_DIR=$3
if [ -z "$PROJ_DIR" ] ; then
  usage "No project_directory."
fi

PROJ_NAME=$4
if [ -z "$PROJ_NAME" ] ; then
  usage "No project_name."
fi

PRG="$0"
PROG_HOME=`dirname "$PRG"`
PROG_HOME=`cd "$PROG_HOME" && pwd`

if [ -z "$BUILD_DIR" ] ; then
    BUILD_DIR=/tmp/svn-package/$$/
fi

BUILD_DIR=$BUILD_DIR/$PROJ_NAME-$REVIS
mkdir -p $BUILD_DIR

# This will start logging to a unique log file, but once we know
#  our build location we want it to log there. The following
#  command makes these two files the same, and the 'tmp' log
#  file location is removed at the end of the script.
ln $LOG_FILE $BUILD_DIR/build_log.txt
if_failure "Can't ln $LOG_FILE $BUILD_DIR/build_log.txt"

SRC_DIR=$BUILD_DIR/src

EXPORT=$PROG_HOME/svn_export.sh
`$EXPORT $REPOS $PROJ_DIR $REVIS $SRC_DIR`
if_failure "Can't export $REPOS $REVIS $PROJ_DIR into $SRC_DIR"

PACKAGE=$PROG_HOME/package_tar.sh
`$PACKAGE $SRC_DIR $BUILD_DIR $PROJ_NAME-$REVIS`
if_failure "Can't package $SRC_DIR $BUILD_DIR $PROJ_NAME-$REVIS"

echo "Package $BUILD_DIR Succeeded!"
) | tee -a $LOG_FILE

rm $LOG_FILE

exit 0
