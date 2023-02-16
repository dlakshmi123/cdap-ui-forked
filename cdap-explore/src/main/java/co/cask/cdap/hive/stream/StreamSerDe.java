/*
 * Copyright © 2014-2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.hive.stream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde2.SerDe;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeStats;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * SerDe to deserialize Stream Events. It MUST implement the deprecated SerDe interface instead of extending the
 * abstract SerDe class, otherwise we get ClassNotFound exceptions on cdh4.x.
 */
@SuppressWarnings("deprecation")
public class StreamSerDe implements SerDe {
  private static final Logger LOG = LoggerFactory.getLogger(StreamSerDe.class);


  // initialize gets called multiple times by Hive. It may seem like a good idea to put additional settings into
  // the conf, but be very careful when doing so. If there are multiple hive tables involved in a query, initialize
  // for each table is called before input splits are fetched for any table. It is therefore not safe to put anything
  // the input format may need into conf in this method. Rather, use StorageHandler's method to place needed config
  // into the properties map there, which will get passed here and also copied into the job conf for the input
  // format to consume.
  @Override
  public void initialize(Configuration conf, Properties properties) throws SerDeException {
  }

  private URL[] getURLs() {
    try {
      List<String> fileNames = new ArrayList<>();
      fileNames.add("/opt/cdap/master/lib/co.cask.cdap.cdap-proto-4.3.3-SNAPSHOT.jar");
      fileNames.add("/opt/cdap/master/lib/cdap-master-4.3.1.jar");
      fileNames.add("/opt/cdap/master/lib/cdap-explore-4.3.1.jar");
      fileNames.add("/opt/cdap/master/lib/co.cask.cdap.cdap-common-4.3.3-SNAPSHOT.jar");

      List<URL> fileURLs = new ArrayList<>();
      for (String fileName : fileNames) {
        File file = new File(fileName);
        if (!file.exists()) {
          throw new IllegalArgumentException(fileName);
        }
        fileURLs.add(file.toURI().toURL());
      }


      return fileURLs.toArray(new URL[fileURLs.size()]);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Class<? extends Writable> getSerializedClass() {
    return Text.class;
  }

  @Override
  public Writable serialize(Object o, ObjectInspector objectInspector) throws SerDeException {
    // should not be writing to streams through this
    throw new SerDeException("Stream serialization through Hive is not supported.");
  }

  @Override
  public SerDeStats getSerDeStats() {
    return new SerDeStats();
  }

  @Override
  public Object deserialize(Writable writable) throws SerDeException {
    return null;
  }

  @Override
  public ObjectInspector getObjectInspector() throws SerDeException {
    return null;
  }
}
