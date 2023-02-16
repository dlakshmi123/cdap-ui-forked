#!/usr/bin/env bash

# Copyright © 2014 Cask Data, Inc.
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
  
# Build script for docs

# Builds:
#
# admin-manual
# developers-manual
# reference-manual
# examples-manual

# Builds each of these individually, and then packages them into a single zip file for distribution.
# _common directory holds common files and scripts.

API="cdap-api"
BUILD="build"
BUILD_TEMP="build-temp"
GITHUB="github"
WEB="web"
HTML="html"
PROJECT="cdap"
PROJECT_CAPS="CDAP"
SCRIPT=`basename $0`
SCRIPT_PATH=`pwd`

REDIRECT_DEVELOPER_HTML=`cat <<EOF
<!DOCTYPE HTML>
<html lang="en-US">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="refresh" content="0;url=developers-manual/index.html">
        <script type="text/javascript">
            window.location.href = "developers-manual/index.html"
        </script>
        <title></title>
    </head>
    <body>
    </body>
</html>
EOF`

ARG_1="$1"
ARG_2="$2"
ARG_3="$3"

function set_project_path() {
  if [ "x$ARG_2" == "x" ]; then
    PROJECT_PATH="$SCRIPT_PATH/../"
  else
    PROJECT_PATH="$SCRIPT_PATH/../../$ARG_2"
  fi
}

function usage() {
  cd $PROJECT_PATH
  PROJECT_PATH=`pwd`
  echo "Build script for '$PROJECT_CAPS' docs"
  echo "Usage: $SCRIPT < option > [source test_includes]"
  echo ""
  echo "  Options (select one)"
  echo "    all            Clean build of everything: HTML docs and Javadocs, GitHub and Web versions"
  echo "    docs           Clean build of just the HTML docs"
  echo "    docs-javadocs  Clean build of HTML docs and Javadocs"
  echo "    docs-github    Clean build of HTML docs and Javadocs, zipped for placing on GitHub"
  echo "    docs-web       Clean build of HTML docs and Javadocs, zipped for placing on docs.cask.co webserver"
  echo ""
  echo "    zip            Zips results; options: none, $WEB, or $GITHUB"
  echo "    license-pdfs   Clean build of License Dependency PDFs"
  echo ""
  echo "    sdk            Build SDK"
  echo "    version        Print the version information"
  echo ""
  echo "  with"
  echo "    source         Path to $PROJECT source, if not $PROJECT_PATH"
  echo "    test_includes  local or remote (default: remote); must specify source if used"
  echo ""
}

function run_command() {
  case "$1" in
    all )               build_all; exit 1;;
    docs )              build_docs; exit 1;;
    docs-javadocs )     build_docs_javadocs; exit 1;;
    docs-github )       build_docs_github; exit 1;;
    docs-web )          build_docs_web; exit 1;;
    zip )               build_zip $2; exit 1;;
    license-pdfs )      build_license_pdfs; exit 1;;
    sdk )               build_sdk; exit 1;;
    version )           print_version; exit 1;;
    * )                 usage; exit 1;;
  esac
}

function clean() {
  cd $SCRIPT_PATH
  rm -rf $SCRIPT_PATH/$BUILD
  mkdir -p $SCRIPT_PATH/$BUILD/$HTML
  echo "Cleaned $BUILD directory"
  echo ""
}

function build_all() {
  echo "Building GitHub Docs."
  ./build.sh docs-github $ARG_2 $ARG_3
  echo "Stashing GitHub Docs."
  cd $SCRIPT_PATH
  mkdir -p $SCRIPT_PATH/$BUILD_TEMP
  mv $SCRIPT_PATH/$BUILD/*.zip $SCRIPT_PATH/$BUILD_TEMP
  echo "Building Web Docs."
  ./build.sh docs-web $ARG_2 $ARG_3
  echo "Moving GitHub Docs."
  mv $SCRIPT_PATH/$BUILD_TEMP/*.zip $SCRIPT_PATH/$BUILD
  rm -rf $SCRIPT_PATH/$BUILD_TEMP
}

function build_docs() {
  build "docs"
}

function build_docs_javadocs() {
  build "build"
}

function build_docs_github() {
  build "build-github"
  build_zip $GITHUB
}

function build_docs_web() {
  build "build-web"
  build_zip $WEB
}

function build() {
  clean
  build_specific_doc admin-manual $1
  build_specific_doc developers-manual $1
  build_specific_doc reference-manual $1
  build_specific_doc examples-manual $1
  add_redirect
}

function add_redirect() {
  cd $SCRIPT_PATH/$BUILD/$HTML
  echo "$REDIRECT_DEVELOPER_HTML" > index.html
}

function build_specific_doc() {
  echo "Building $1, target $2..."
  cd $1
  ./build.sh $2 $ARG_2 $ARG_3
  cd $SCRIPT_PATH
  echo "Copying $1 results..."
  cp -r $1/$BUILD/$HTML $BUILD/$HTML/$1
  echo ""
}

function build_zip() {
  cd $SCRIPT_PATH
  source _common/common-build.sh
  set_project_path
  print_version
  if [ "x$1" == "x" ]; then
    make_zip
  else
    make_zip_localized $1
  fi
  echo "Building zip completed."
}

function build_sdk() {
  build_specific_doc developers-manual sdk
}

set_project_path

run_command  $1
