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

SVN_GET_TOOL=$PROG_HOME/svn_get_tool.sh
BUILD_TOOL=`$SVN_GET_TOOL $PROJ_DESC`
if_failure "$PROJ_DESC is not an auto-build svn project."

BUILD_IT=`$BUILD_TOOL $PROJ_DESC`
if_failure "$BUILD_TOOL could not build $PROJ_DESC."

echo "$PROJ_DESC Build Succeeded!"

exit 0

