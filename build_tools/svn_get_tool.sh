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

# Get everything after the $REVIS (Note the trailing ' '!)
PARTIAL_DESC=${PROJ_DESC#*$REVIS }
# Get everything before the ' '
PROJ_DIR=${PARTIAL_DESC%* *}
# Get everything after the ' '
PROJ_NAME=${PARTIAL_DESC#* *}

SVNLOOK=/usr/local/bin/svnlook

AUTO_BUILD_PROP=auto-build

ARGS="propget $REPOS $AUTO_BUILD_PROP $PROJ_DIR -r $REVIS"
echo "$SVNLOOK $ARGS" 1>&2

AUTO_TOOL=`$SVNLOOK $ARGS`
if_failure "$SVNLOOK $ARGS: $AUTO_TOOL"

if [ -z $AUTO_TOOL ] ; then
  echo "$REPOS $REVIS $PROJ_DIR $PROJ_NAME doesn't svn auto-build." 1>&2
  exit 1
fi

echo "SVN auto-build for $REPOS $REVIS $PROJ_DIR $PROJ_NAME is $AUTO_TOOL" 1>&2
echo $AUTO_TOOL

exit 0  


