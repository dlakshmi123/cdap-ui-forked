.. meta::
    :author: Cask Data, Inc.
    :copyright: Copyright © 2015 Cask Data, Inc.

.. _workers:

=======
Workers
=======

Workers can be used to run background processes. Workers provide the ability to write data processing logic
that doesn't fit into the other paradigms such as Flows and MapReduce or realtime and batch.

You can add workers to your application by calling the ``addWorker`` method in the Application's
``configure`` method::

  public class AnalyticsApp extends AbstractApplication {
    @Override
    public void configure() {
      setName("AnalyticsApp");
      ...
      addWorker(new ProcessWorker());
      ...
    }
  }

Workers have semantics similar to a Java thread and are run in a thread when CDAP is run in either In-Memory
or Standalone Modes. In distributed mode, each instance of a worker runs in its own YARN container.
Their instances may be updated via the :ref:`Command Line Interface <cli-available-commands>` or a :ref:`RESTful API <http-restful-api-lifecycle>`::

  public class ProcessWorker extends AbstractWorker {

    @Override
    public void initialize(WorkerContext context) {
      super.initialize(context);
      ...
    }

    @Override
    public void run() {
      ...
    }

    @Override
    public void stop() {
      ...
    }

    @Override
    public void destroy() {
      ...
    }
  }

Workers can access and use ``Dataset``\s via a ``DatasetContext`` inside the ``run`` method of a ``TxRunnable``.
A ``TxRunnable`` can be executed using the ``execute`` method of a ``WorkerContext``. Note that ``WorkerContext``
is thread-safe and that each thread will receive its own instance of the ``Dataset`` being accessed. Though it is
necessary to use the ``getDataset`` method of ``DatasetContext`` to access a ``Dataset`` instance inside a
``TxRunnable``, this operation is performance-optimized with the help of internal caching logic. In this
example, the Dataset *tableName* is accessed from within a ``TxRunnable``::

  @Override
  public void run() {
    getContext().execute(new TxRunnable() {
      @Override
      public void run(DatasetContext context) {
        Dataset dataset = context.getDataset("tableName");
        ...
      }
    });
  }

Operations executed on ``Dataset``\s within a ``run`` are committed as part of a single transaction.
The transaction is started before ``run`` is invoked and is committed upon successful execution. Exceptions
thrown while committing the transaction or thrown by user-code result in a rollback of the transaction.
It is recommended that ``TransactionConflictException`` be caught and handled appropriately; for example,
you can retry a ``Dataset`` operation.

Services can be discovered from inside of Workers. Services can either belong to the same application or to another
application in the same namespace. WorkerContext can be used to discover the URL of the Service of interest::

  @Override
  public void run() {
    URL url = getContext().getServiceURL("myService");

    // To discover a Service in another application in the same namespace, use:
    url = getContext().getServiceURL("anotherAppName", "anotherServiceId");
  }

Writing to Streams (Beta)
=========================

Workers have the ability to write to ``Streams`` through the ``WorkerContext``. The implementation internally
issues an HTTP request to the Stream Service to persist the data. Because of that, a write to a Stream
cannot be rolled back, and thus the write differs in semantics compared to writing to ``Datasets`` from inside the
``run`` method of a ``TxRunnable``.

The write operation throws an ``IOException`` if it could not write to a ``Stream``. Writing to streams can be
performed as either single event writes or in batch.

When uploading events in batch, there are two options: either uploading a ``File`` or writing multiple events
through a ``StreamBatchWriter``. In batch mode, the content type of the data must be specified. Refer
to the :ref:`Stream RESTful API <http-restful-api-stream>` for information on the content type specification.

With a ``StreamBatchWriter``, the ``close`` method` needs to be called after all the writes have been performed::

  @Override
  public void run() {
    try {
      // Writing a single string event to stream myStream
      getContext().write("myStream", "data1");

      Map<String, String> header = Maps.newHashMap();
      header.put("k1", "v1");

      // Writing a single string event with header to stream myStream
      getContext().write("myStream", "data2", header);

      // Writing a set of events as one batch operation to stream myStream (with content type as text)
      StreamBatchWriter batchWriter = getContext().createBatchWriter("myStream", "text/string");
      batchWriter.write(ByteBuffer.wrap(Bytes.toBytes("data1\n")));
      batchWriter.write(ByteBuffer.wrap(Bytes.toBytes("data2\n")));
      batchWriter.write(ByteBuffer.wrap(Bytes.toBytes("data3")));
      batchWriter.close();
    } catch (IOException e) {
      // Handle exception
    }
  }

