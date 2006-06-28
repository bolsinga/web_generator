This directory contains tools used (and related to) automatic builds after svn post-commit hooks.

----------
dir_get_builder.sh
svn_get_builder.sh
get_builder_type.sh

- These scripts will return, in order of precedence, 'xcode', 'ant', or 'make' as what should
  be used to build. dir_get_builder.sh takes a directory name. svn_get_builder.sh takes a
  repository, project_directory and revision. get_builder_type.sh is used by the other scripts
  and simply takes a list of filenames. It could be extended to recognize new build tools.
  Please see the next section for more about new build tools.

----------
build_ant.sh
build_makefile.sh
build_xcode.sh

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
svn_export.sh

- This script will export a svn directory revision to a given directory.
 repository - The name of the repository (also passed to svn post-commit) to export from.
 project_dir - The project directory in the repository to export (obtained from
  svn_project_description.sh).
 revision - The revision of repository (also passed to svn post-commit) to export.
 export_dir - The directory to export into.

----------
svn_project_description.sh

- This script will give a description of a give svn repository and revision. This can be
  used to learn the sub-directory of the entire repository responsible for the given
  revision number. It will print the following out to stdout:
  "Repository Revision Project-Directory Project-Name"
  NOTE: This script is where some assumptions are made. It assumes that the svn repository is
   organized in the classic 'trunk', 'branches', and 'tags' style. It assumes that a given
   revision number will apply to a single one of these directories. It has only been tested
   on revisions that are under 'trunk'. It obtains the Project-Name by extracting the style
   from the Project-Directory, which may fail for 'branches'.
 repository - The name of the repository (also passed to svn post-commit) to export from.
 revision - The revision of repository (also passed to svn post-commit) to export.

----------
package_tar.sh

- This script will tar up a given directory in a given location with a versioned name.
 src_dir - The directory that will be tar'ed up.
 dest_dir - The directory where "<project_name>-<version>.tar.gz" will be created.
 tar_name - The name used to name the tar file.

----------
svn_build.sh

- This script is intended to be called via the svn post-commit hook. It uses a selection of
  the above tools to determine if the given revision can be automatically built with one
  of the supported tools, exporting a fresh directory to build, building the directory with
  the proper tool, and creating a compressed tar file of the result. All of the work it does
  is logged into a text file. It will create all of this into the environment variable
  BUILD_DIR. If it is not set, it will set BUILD_DIR to "/tmp/svn-build/<process_id>".
 repository - The name of the repository (also passed to svn post-commit) to export from.
 revision - The revision of repository (also passed to svn post-commit) to export.
 project_dir - The project directory in the repository to export (obtained from
  svn_project_description.sh).
 project_name - The project name (obtained from svn_project_description.sh).

----------
dir_build.sh

- This script builds the given directory as similarly to svn_build.sh as possible. Namely 
  it uses get_builder_type.sh and build_<tool>.sh. It logs its output. It doesn't use
  package_tar.sh.
 <src_dir> - An optional directory to build. If nothing is supplied, the current directory
  is used.

----------
svn_get_tool.sh

- This script will return the name of the program to run for the given repository and revision
  as a part of running from a svn post-commit hook. It reads the "auto-build" svn property
  for the project directory as the full path of the tools used to automatically build the
  given directory. If there is no svn property, this svn project is assumed to not be 
  automatically built.
 repository - The name of the repository (also passed to svn post-commit) to export from.
 revision - The revision of repository (also passed to svn post-commit) to export.
 project_dir - The project directory in the repository to export (obtained from
  svn_project_description.sh).
 project_name - The project name (obtained from svn_project_description.sh).

----------
svn_post_commit.sh

- This script is the root svn post-commit hook tool. It uses svn_get_tool.sh to determine
  if this repository revision pair supports auto-build, and if it does fires off the
  auto-build tool with the results of the svn_project_description.sh tool.
 repository - The name of the repository (also passed to svn post-commit) to export from.
 revision - The revision of repository (also passed to svn post-commit) to export.
