#!/bin/sh

usage ()
{
  echo "get_project_dir.sh repository revision"
  echo "Error: $1"
  exit 1
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

# Test each preferred top level directory name
for style in trunk branches tags ; do
  for dir in ${CHANGED_DIRS} ; do
    test_style=`echo $dir | grep $style`
    if [ $test_style ] ; then
      proj_dir=`echo $dir | sed -e"s|$style.*|$style|"`
      test_contains=`echo $PROJ_DIRS | grep $proj_dir`
      if [ -z $test_contains ] ; then
        if [ -z $PROJ_DIRS ] ; then
          PROJ_DIRS=$proj_dir
        else
          PROJ_DIRS="$PROJ_DIRS $proj_dir"
        fi
      fi
    fi
  done
done

proj_count=`echo $PROJ_DIRS | wc -l`
if [ $proj_count -ne 1 ] ; then
  echo "Error: In Repository $REPOS Revision $REVIS:"
  echo "There is more than one project changed. Projects:"
  echo "$PROJ_DIRS"
  exit 1
fi
echo "$PROJ_DIRS"
exit 0
