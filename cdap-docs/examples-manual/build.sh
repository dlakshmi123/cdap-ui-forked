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
  
# Build script for guide docs
#
# Copies the original README from the mounted GitHub repo in the local filesystem
# running it through sed to modify all image links to be relative to the build/_includes directory.
# This modified README is included in the source RST files, and is used by Sphinx to build the HTML.

source ../_common/common-build.sh

CHECK_INCLUDES=$TRUE

function guide_rewrite() {
  echo "Re-writing $1 $2"
  # Re-writes the links in the RST file to point to a local copy of any image links.
  INCLUDES_DIR=$1
  GUIDE=$2
  local redirect_s1="https://raw.githubusercontent.com/cdap-guides"
  local redirect_s2="release/cdap-2.6.0-compatible/README.md"
  REDIRECT_T="\.\./\.\./\.\./\.\./\.\." # Target, 5 redirects, escaped
  
  mkdir $INCLUDES_DIR/$GUIDE
  pandoc -t rst -r markdown $redirect_s1/$GUIDE/$redirect_s2  -o $INCLUDES_DIR/$GUIDE/README_SOURCE.rst
  sed -e "s|figure:: docs/images|figure:: $REDIRECT_T/$GUIDE/docs/images|g" -e "s|.. code:: |.. code-block:: |g" $INCLUDES_DIR/$GUIDE/README_SOURCE.rst > $INCLUDES_DIR/$GUIDE/README.rst
}

function pandoc_includes() {
  # Uses pandoc to translate the README markdown files to rst in the target directory
  # and then re-writes all the image links...
  guide_rewrite $1 cdap-bi-guide
  guide_rewrite $1 cdap-flow-guide
  guide_rewrite $1 cdap-flume-guide
  guide_rewrite $1 cdap-kafka-ingest-guide
  guide_rewrite $1 cdap-mapreduce-guide
  guide_rewrite $1 cdap-spark-guide
  guide_rewrite $1 cdap-timeseries-guide
  guide_rewrite $1 cdap-twitter-ingest-guide
}

run_command $1
