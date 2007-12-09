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

SVNLOOK=/usr/bin/svnlook

AUTO_BUILD_PROP=auto-build

ARGS="propget $REPOS $AUTO_BUILD_PROP $PROJ_DIR -r $REVIS"
echo "$SVNLOOK $ARGS" 1>&2

AUTO_TOOL=`$SVNLOOK $ARGS`
if_failure "$SVNLOOK $ARGS: $AUTO_TOOL"

if [ -z "$AUTO_TOOL" ] ; then
  echo "$REPOS $REVIS $PROJ_DIR $PROJ_NAME does not svn auto-build." 1>&2
  exit 1
fi

echo "SVN auto-build for $REPOS $REVIS $PROJ_DIR $PROJ_NAME is $AUTO_TOOL" 1>&2
echo $AUTO_TOOL

exit 0  
