#!/bin/sh

usage()
{
  echo "export.sh repository revision build_root_dir"
  echo "Error: $1"
  exit 1
}

if_failure()
{
  if [ "$?" -ne 0 ] ; then
    echo "Error: $1"
    exit 1
  fi
}

REPOS=$1
if [ -z "$REPOS" ] ; then
    usage "No repository"
fi

REVIS=$2
if [ -z "$REVIS" ] ; then
    usage "No revision"
fi

BUILD_ROOT_DIR=$3
if [ -z "$BUILD_ROOT_DIR" ] ; then
    usage "No build_root_dir"
fi
if [ ! -d "$BUILD_ROOT_DIR" ] ; then
  mkdir -p $BUILD_ROOT_DIR
fi
BUILD_ROOT_DIR=`cd "$BUILD_ROOT_DIR" && pwd`

PRG="$0"
PROG_HOME=`dirname "$PRG"`
PROG_HOME=`cd "$PROG_HOME" && pwd`

GET_PROJ_DIR=$PROG_HOME/get_project_dir.sh
GET_PROJ=$PROG_HOME/get_project.sh

REPOS_PROJ=`$GET_PROJ_DIR $REPOS $REVIS`
if_failure "Can't get project dir for $REPOS $REVIS"
PROJ_NAME=`$GET_PROJ $REPOS_PROJ`
if_failure "Can't get projecdt name for $REPOS_PROJ"
BUILD_DIR=$PROJ_NAME-$REVIS

cd $BUILD_ROOT_DIR

svn export -q -r $REVIS file://$REPOS/$REPOS_PROJ $BUILD_DIR
if_failure "Can't export $REPOS_PROJ"

echo $BUILD_ROOT_DIR/$BUILD_DIR
