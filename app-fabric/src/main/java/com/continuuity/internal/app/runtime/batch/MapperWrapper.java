package com.continuuity.internal.app.runtime.batch;

import com.continuuity.common.logging.LoggingContextAccessor;
import com.google.common.base.Throwables;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Wraps user-defined implementation of {@link Mapper} class which allows perform extra configuration.
 */
public class MapperWrapper extends Mapper {

  public static final String ATTR_MAPPER_CLASS = "c.mapper.class";

  private static final Logger LOG = LoggerFactory.getLogger(MapperWrapper.class);

  @Override
  public void run(Context context) throws IOException, InterruptedException {
    MapReduceContextProvider mrContextProvider = new MapReduceContextProvider(context);
    BasicMapReduceContext basicMapReduceContext = mrContextProvider.get();

    // now that the context is created, we need to make sure to properly close all datasets of the context
    try {
      String userMapper = context.getConfiguration().get(ATTR_MAPPER_CLASS);
      Mapper delegate = createMapperInstance(context.getConfiguration().getClassLoader(), userMapper);

      // injecting runtime components, like datasets, etc.
      basicMapReduceContext.injectFields(delegate);

      LoggingContextAccessor.setLoggingContext(basicMapReduceContext.getLoggingContext());

      delegate.run(context);

      // transaction is not finished, but we want all operations to be dispatched (some could be buffered in
      // memory by tx agent
      try {
        basicMapReduceContext.flushOperations();
      } catch (Exception e) {
        LOG.error("Failed to flush operations at the end of mapper of " + basicMapReduceContext.toString());
        throw Throwables.propagate(e);
      }
    } finally {
      basicMapReduceContext.close();
    }
  }

  private Mapper createMapperInstance(ClassLoader classLoader, String userMapper) {
    try {
      return (Mapper) classLoader.loadClass(userMapper).newInstance();
    } catch (Exception e) {
      LOG.error("Failed to create instance of the user-defined Mapper class: " + userMapper);
      throw Throwables.propagate(e);
    }
  }
}
