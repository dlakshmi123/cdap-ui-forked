#
# Copyright 2012-2014 Continuuity, Inc.
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
#

# Set environment variables here.

# Main class to be invoked.
MAIN_CLASS=com.continuuity.data.runtime.main.ReactorServiceMain

# Arguments for main class.
MAIN_CLASS_ARGS="start"

# Add Hadoop HDFS classpath
# Assuming update-alternatives convention
EXTRA_CLASSPATH="/etc/hbase/conf/"

JAVA_HEAPMAX=-Xmx1024m

