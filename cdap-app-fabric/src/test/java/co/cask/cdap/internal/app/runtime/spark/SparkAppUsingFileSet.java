/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.internal.app.runtime.spark;

import co.cask.cdap.api.app.AbstractApplication;
import co.cask.cdap.api.data.batch.DatasetOutputCommitter;
import co.cask.cdap.api.data.batch.InputFormatProvider;
import co.cask.cdap.api.data.batch.OutputFormatProvider;
import co.cask.cdap.api.dataset.DataSetException;
import co.cask.cdap.api.dataset.DatasetSpecification;
import co.cask.cdap.api.dataset.lib.AbstractDataset;
import co.cask.cdap.api.dataset.lib.FileSet;
import co.cask.cdap.api.dataset.lib.FileSetProperties;
import co.cask.cdap.api.dataset.lib.PartitionedFileSet;
import co.cask.cdap.api.dataset.lib.PartitionedFileSetProperties;
import co.cask.cdap.api.dataset.lib.Partitioning;
import co.cask.cdap.api.dataset.lib.TimePartitionedFileSet;
import co.cask.cdap.api.dataset.lib.TimePartitionedFileSetArguments;
import co.cask.cdap.api.dataset.module.EmbeddedDataset;
import co.cask.cdap.api.spark.AbstractSpark;
import co.cask.cdap.api.spark.JavaSparkProgram;
import co.cask.cdap.api.spark.SparkContext;
import com.google.common.base.Throwables;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.twill.filesystem.Location;
import scala.Tuple2;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A dummy app with spark program which counts the characters in a string
 */
public class SparkAppUsingFileSet extends AbstractApplication {

  @Override
  public void configure() {
    try {
      createDataset("fs", FileSet.class, FileSetProperties.builder()
        .setInputFormat(MyTextInputFormat.class)
        .setOutputFormat(TextOutputFormat.class)
        .setOutputProperty(TextOutputFormat.SEPERATOR, ":").build());
      createDataset("pfs", PartitionedFileSet.class, PartitionedFileSetProperties.builder()
        .setPartitioning(Partitioning.builder().addStringField("x").build())
        .setInputFormat(MyTextInputFormat.class)
        .setOutputFormat(TextOutputFormat.class)
        .setOutputProperty(TextOutputFormat.SEPERATOR, ":").build());
      createDataset("tpfs", TimePartitionedFileSet.class, FileSetProperties.builder()
        .setInputFormat(MyTextInputFormat.class)
        .setOutputFormat(TextOutputFormat.class)
        .setOutputProperty(TextOutputFormat.SEPERATOR, ":").build());
      createDataset("myfs", MyFileSet.class, FileSetProperties.builder()
        .setInputFormat(MyTextInputFormat.class)
        .setOutputFormat(TextOutputFormat.class)
        .setOutputProperty(TextOutputFormat.SEPERATOR, ":").build());
      addSpark(new JavaCharCount());
      addSpark(new ScalaCharCount());
    } catch (Throwable t) {
      throw Throwables.propagate(t);
    }
  }

  public static final class JavaCharCount extends AbstractSpark {
    @Override
    public void configure() {
      setMainClass(CharCountProgram.class);
    }
  }

  public static final class ScalaCharCount extends AbstractSpark {
    @Override
    public void configure() {
      setMainClass(ScalaFileCountProgram.class);
    }
  }

  public static class CharCountProgram implements JavaSparkProgram {
    @Override
    public void run(SparkContext context) {
      String input = context.getRuntimeArguments().get("input");
      String output = context.getRuntimeArguments().get("output");

      // read the dataset
      JavaPairRDD<Long, String> inputData = context.readFromDataset(input, Long.class, String.class);

      JavaPairRDD<String, Integer> stringLengths = transformRDD(inputData);

      // write the character count to dataset
      context.writeToDataset(stringLengths, output, String.class, Integer.class);

      String inputPartitionTime = context.getRuntimeArguments().get("inputKey");
      String outputPartitionTime = context.getRuntimeArguments().get("outputKey");

      // read and write datasets with dataset arguments
      if (inputPartitionTime != null && outputPartitionTime != null) {
        Map<String, String> inputArgs = new HashMap<>();
        TimePartitionedFileSetArguments.setInputStartTime(inputArgs, Long.parseLong(inputPartitionTime) - 100);
        TimePartitionedFileSetArguments.setInputEndTime(inputArgs, Long.parseLong(inputPartitionTime) + 100);

        // read the dataset with user custom dataset args
        JavaPairRDD<Long, String> customPartitionData = context.readFromDataset(input, Long.class, String.class,
                                                                                inputArgs);

        // create a new RDD with the same key but with a new value which is the length of the string
        JavaPairRDD<String, Integer> customPartitionStringLengths = transformRDD(customPartitionData);

        // write the character count to dataset with user custom dataset args
        Map<String, String> outputArgs = new HashMap<>();
        TimePartitionedFileSetArguments.setOutputPartitionTime(outputArgs, Long.parseLong(outputPartitionTime));
        context.writeToDataset(customPartitionStringLengths, output, String.class, Integer.class, outputArgs);
      }
    }

    private JavaPairRDD<String, Integer> transformRDD(JavaPairRDD<Long, String> inputData) {
      // create a new RDD with the same key but with a new value which is the length of the string
      return inputData.mapToPair(
        new PairFunction<Tuple2<Long, String>, String, Integer>() {
          @Override
          public Tuple2<String, Integer> call(Tuple2<Long, String> pair) throws Exception {
            return new Tuple2<>(pair._2(), pair._2().length());
          }
        });
    }
  }

  /**
   * A custom dataset embedding a fileset. It delegates all operations to the embedded fileset, but it
   * overrides the DatasetOutputCommitter to create a file names "success" or "failure", which can be
   * validated by the test case.
   */
  public static class MyFileSet extends AbstractDataset
    implements InputFormatProvider, OutputFormatProvider, DatasetOutputCommitter {

    private final FileSet delegate;

    public MyFileSet(DatasetSpecification spec, @EmbeddedDataset("files") FileSet embedded) {
      super(spec.getName(), embedded);
      this.delegate = embedded;
    }

    public FileSet getEmbeddedFileSet() {
      return delegate;
    }

    public Location getSuccessLocation() throws IOException {
      return delegate.getBaseLocation().append("success");
    }

    public Location getFailureLocation() throws IOException {
      return delegate.getBaseLocation().append("failure");
    }

    @Override
    public String getInputFormatClassName() {
      return delegate.getInputFormatClassName();
    }

    @Override
    public Map<String, String> getInputFormatConfiguration() {
      return delegate.getInputFormatConfiguration();
    }

    @Override
    public String getOutputFormatClassName() {
      return delegate.getOutputFormatClassName();
    }

    @Override
    public Map<String, String> getOutputFormatConfiguration() {
      return delegate.getOutputFormatConfiguration();
    }

    @Override
    public void onSuccess() throws DataSetException {
      try {
        getSuccessLocation().createNew();
      } catch (Throwable t) {
        throw Throwables.propagate(t);
      }
    }

    @Override
    public void onFailure() throws DataSetException {
      try {
        getFailureLocation().createNew();
      } catch (Throwable t) {
        throw Throwables.propagate(t);
      }
    }
  }

  /**
   * An input format that delegates to TextInputFormat. It is defined in the application and requires
   * the Spark runtime to use the program class loader to load the class.
   */
  public static final class MyTextInputFormat extends InputFormat<Long, String> {

    TextInputFormat delegate = new TextInputFormat();

    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
      return delegate.getSplits(context);
    }

    @Override
    public RecordReader<Long, String> createRecordReader(InputSplit split, TaskAttemptContext context)
      throws IOException, InterruptedException {
      return new MyRecordReader(delegate.createRecordReader(split, context));
    }
  }

  public static class MyRecordReader extends RecordReader<Long, String> {

    private RecordReader<LongWritable, Text> delegate;

    public MyRecordReader(RecordReader<LongWritable, Text> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context)
      throws IOException, InterruptedException {
      delegate.initialize(split, context);
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
      return delegate.nextKeyValue();
    }

    @Override
    public Long getCurrentKey() throws IOException, InterruptedException {
      LongWritable writable = delegate.getCurrentKey();
      return writable == null ? null : writable.get();
    }

    @Override
    public String getCurrentValue() throws IOException, InterruptedException {
      Text text = delegate.getCurrentValue();
      return text == null ? null : text.toString();
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
      return delegate.getProgress();
    }

    @Override
    public void close() throws IOException {
      delegate.close();
    }
  }
}
