#!/bin/sh

usage ()
{
  echo "$0 repository revision" 1>&2
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

PRG="$0"
PROG_HOME=`dirname "$PRG"`
PROG_HOME=`cd "$PROG_HOME" && pwd`

GET_PROJ_DESC=$PROG_HOME/svn_project_description.sh
PROJ_DESC=`$GET_PROJ_DESC $REPOS $REVIS`
if_failure "Can't get project description for $REPOS $REVIS"

# Get everything after the $REVIS (Note the trailing ' '!)
PARTIAL_DESC=${PROJ_DESC#*$REVIS }
# Get everything before the ' '
PROJ_DIR=${PARTIAL_DESC%* *}
# Get everything after the ' '
PROJ_NAME=${PARTIAL_DESC#* *}

if [ -z "$BUILD_DIR" ] ; then
    BUILD_DIR=/tmp/svn-build/$$/
fi

BUILD_DIR=$BUILD_DIR/$PROJ_NAME-$REVIS
mkdir -p $BUILD_DIR

# This will start logging to a unique log file, but once we know
#  our build location we want it to log there. The following
#  command makes these two files the same, and the 'tmp' log
#  file location is removed at the end of the script.
ln $LOG_FILE $BUILD_DIR/build_log.txt

GET_BUILDER=$PROG_HOME/svn_get_builder.sh
BUILD_TYPE=`$GET_BUILDER $REPOS $PROJ_DIR $REVIS`
if_failure "Can't get build type for $REPOS $REVIS $PROJ_DIR"

BUILDER=$PROG_HOME/build_$BUILD_TYPE.sh

SRC_DIR=$BUILD_DIR/src
OBJ_DIR=$BUILD_DIR/obj
SYM_DIR=$BUILD_DIR/sym
DST_DIR=$BUILD_DIR/dst

EXPORT=$PROG_HOME/svn_export.sh
`$EXPORT $REPOS $PROJ_DIR $REVIS $SRC_DIR`
if_failure "Can't export $REPOS $REVIS $PROJ_DIR into $SRC_DIR"

`$BUILDER $SRC_DIR $OBJ_DIR $SYM_DIR $DST_DIR $REVIS`
if_failure "Can't build $BUILDER $SRC_DIR $OBJ_DIR $SYM_DIR $DST_DIR $REVIS"

PACKAGE_TAR=$PROG_HOME/package_tar.sh
`$PACKAGE_TAR $DST_DIR $BUILD_DIR $PROJ_NAME $REVIS`
if_failure "Can't package tar file $PACKAGE_TAR $DST_DIR $BUILD_DIR $PROJ_NAME $REVIS"

echo "Build $BUILD_DIR Succeeded!"
) | tee -a $LOG_FILE

rm $LOG_FILE

exit 0
