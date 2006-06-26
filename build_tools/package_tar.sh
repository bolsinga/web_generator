#!/bin/sh

usage()
{
  echo "$0 src_dir dest_dir project_name version" 1>&2
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

SRC_DIR=$1
if [ -z "$SRC_DIR" ] ; then
  usage "No src_dir."
fi
SRC_DIR=`cd "$SRC_DIR" && pwd`
if [ ! -d "${SRC_DIR}" ] ; then
  usage "No $SRC_DIR"
fi

DEST_DIR=$2
if [ -z "$DEST_DIR" ] ; then
  usage "No dest_dir."
fi
DEST_DIR=`cd "$DEST_DIR" && pwd`
if [ ! -d "${DEST_DIR}" ] ; then
  usage "No $DEST_DIR"
fi

PROJECT=$3
if [ -z "$PROJECT" ] ; then
  usage "No project_name."
fi

VERSION=$4
if [ -z "$VERSION" ] ; then
  usage "No version."
fi

TAR_NAME=$PROJECT-$VERSION

cd $SRC_DIR/.. ; ln -s $SRC_DIR $TAR_NAME
if_failure "symlink failed $SRC_DIR $TAR_NAME"

echo "Creating $DEST_DIR/$TAR_NAME.tar.gz" 1>&2

cd $SRC_DIR/.. ; tar czfhv $DEST_DIR/$TAR_NAME.tar.gz $TAR_NAME 1>&2
if_failure "tar $DEST_DIR/$TAR_NAME.tar.gz failed"

cd $SRC_DIR/.. ; rm $TAR_NAME

exit 0
