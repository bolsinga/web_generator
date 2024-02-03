This directory contains tools used (and related to) automatic builds.

----------
dir_get_builder.sh
get_builder_type.sh

- These scripts will return, in order of precedence, 'ant', or 'make' as what should
  be used to build. dir_get_builder.sh takes a directory name. get_builder_type.sh is used by
  the other scripts and simply takes a list of filenames. It could be extended to recognize
  new build tools. Please see the next section for more about new build tools.

----------
build_ant.sh
build_makefile.sh

- These scripts will build the type of project given in their name. If new build tools are
  introduced, they would be supported by adding a "build_<newtool>.sh" script. Their
  parameters are:
 src_dir - The directory where the project is located
 obj_dir - The directory to build 'disposable' items; these may be safely deleted in the future
 sym_dir - The directory to build the symboled items; these are not shipped, but useful for
  debugging.
 dst_dir - The directory to build the shipped items
 build_id - The identifier for the build (e.g., my_project-123)

----------
package_tar.sh

- This script will tar up a given directory in a given location with a versioned name.
 src_dir - The directory that will be tar'ed up.
 dest_dir - The directory where "<project_name>-<version>.tar.gz" will be created.
 tar_name - The name used to name the tar file.

----------
dir_build.sh

- This script builds the given directory. Namely it uses get_builder_type.sh and
  build_<tool>.sh. It logs its output. It doesn't use package_tar.sh.
 <src_dir> - An optional directory to build. If nothing is supplied, the current directory
  is used.

----------
git_build.sh

- Use this script to build from a git directory. The project_name is the src_dir. The
  revision is obtained from the git log long commit hash. It uses a selection of
  the above tools to determine if the given revision can be automatically built with one
  of the supported tools, exporting a fresh directory to build, building the directory with
  the proper tool, and creating a compressed tar file of the result. All of the work it does
  is logged into a text file. It will create all of this into the environment variable
  BUILD_DIR. If it is not set, it will set BUILD_DIR to "/tmp/git-build/<process_id>".
 <src_dir> - An optional directory to build. If nothing is supplied, the current directory
  is used.
