#!/bin/sh

usage ()
{
  echo "get_project.sh project_dir"
  echo "Error: $1"
  exit 1
}

PROJ_DIR=$1
if [ -z "$PROJ_DIR" ] ; then
  usage "No project_dir."
fi

for style in trunk branches tags ; do
  test_style=`echo $PROJ_DIR | grep $style`
  if [ $test_style ] ; then
    project=`echo $PROJ_DIR | sed -e"s|/$style||"`
    echo $project
    exit 0
  fi
done

echo "Error: $PROJ_DIR doesn't match the standard 'trunk', 'branches', or 'tags' suffix."
exit 1
