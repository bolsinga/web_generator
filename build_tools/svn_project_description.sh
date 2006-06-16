#!/bin/sh

usage ()
{
  echo "$0 repository revision"
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

SVNLOOK=/usr/local/bin/svnlook

SVNREVIS="-r $REVIS"

CHANGED_DIRS=`$SVNLOOK dirs-changed $REPOS $SVNREVIS`
if_failure "Can't $SVNLOOOK dirs-changed $REPOS $SVNREVIS"

KNOWN_STYLES="trunk branches tags"
# Test each preferred top level directory name
for style in ${KNOWN_STYLES} ; do
  for dir in ${CHANGED_DIRS} ; do
    test_style=`echo $dir | grep $style`
    if [ $test_style ] ; then
      proj_dir=`echo $dir | sed -e"s|$style.*|$style|"`
      test_contains=`echo $PROJ_DIR | grep $proj_dir`
      if [ -z $test_contains ] ; then
        if [ -z $PROJ_DIR ] ; then
          PROJ_DIR=$proj_dir
        else
          PROJ_DIR="$PROJ_DIR $proj_dir"
        fi
      fi
    fi
  done
done

proj_count=`echo $PROJ_DIR | wc -l`
if [ $proj_count -ne 1 ] ; then
  echo "Error: In Repository $REPOS Revision $REVIS:"
  echo "There is more than one project changed. Projects:"
  echo "$PROJ_DIR"
  exit 1
fi

for style in ${KNOWN_STYLES} ; do
  test_style=`echo $PROJ_DIR | grep $style`
  if [ $test_style ] ; then
    project=`echo $PROJ_DIR | sed -e"s|/$style||"`
    PROJ_NAME=$project
    break
  fi
done

echo "$REPOS $REVIS $PROJ_DIR $PROJ_NAME"
exit 0
