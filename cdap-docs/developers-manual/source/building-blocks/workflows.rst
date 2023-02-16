.. meta::
    :author: Cask Data, Inc.
    :copyright: Copyright © 2014-2015 Cask Data, Inc.

.. _workflows:

=========
Workflows
=========

*Workflows* are used to automate the execution of a series of :ref:`MapReduce
<mapreduce>`, :ref:`Spark <spark>` or :ref:`custom actions <workflow-custom-actions>`. It
allows for both sequential and :ref:`parallel execution <workflow_parallel>` of programs.

The workflow system allows specifying, executing, scheduling, and monitoring complex
series of jobs and tasks in CDAP. The system can manage thousands of workflows and
maintain millions of historic workflow logs. 

Overview
========

A *Workflow* is given a sequence of programs that follow each other, with an optional
schedule to run the workflow periodically. Upon successful execution of a program, the
control is transferred to the next program in the sequence until the last program in the
sequence is executed. Upon failure, the execution is stopped at the failed program and no
subsequent programs in the sequence are executed.

The control flow of a workflow can be described as a directed, acyclic graph (DAG) of actions.
To be more precise, we require that it be a series-parallel graph. This is a graph with a
single start node and a single finish node. As described below, execution can be either
sequential (the default) or :ref:`concurrent <workflow-concurrent>`, and the graph can be 
either a simple series of nodes or a more complicated :ref:`parallel workflow <workflow_parallel>`.

Executing MapReduce or Spark Programs
-------------------------------------
To execute MapReduce or Spark programs in a workflow, you will need to add them in your
application along with the workflow. You can optionally add a :ref:`schedule <schedules>` 
(such as a `crontab schedule 
<../../reference-manual/javadocs/co/cask/cdap/api/app/AbstractApplication.html#scheduleWorkflow(java.lang.String,%20java.lang.String,%20java.lang.String)>`__)
to the workflow::

  public void configure() {
    ...
    addMapReduce(new MyMapReduce());
    addMapReduce(new AnotherMapReduce());
    addSpark(new MySpark());
    addWorkflow(new MyWorkflow());
    scheduleWorkflow(Schedules.createTimeSchedule("FiveHourSchedule", 
                                                  "Schedule running every 5 hours", 
                                                  "0 */5 * * *"),
                     "MyWorkflow");
    ...
  }

You'll then extend the ``AbstractWorkflow`` class and implement the ``configure()``
method. Inside ``configure``, you can add multiple MapReduce, Spark programs or custom
actions to the workflow. The programs will be executed in the order they are specified in
the ``configure`` method::

  public static class MyWorkflow extends AbstractWorkflow {

    @Override
    public void configure() {
        setName("MyWorkflow");
        setDescription("MyWorkflow description");
        addMapReduce("MyMapReduce");
        addSpark("MySpark");
        addMapReduce("AnotherMapReduce");
        addAction(new MyAction());
    }
  }

In this example, the ``MyWorkflow`` will be executed every 5 hours. During each execution
of the workflow, the ``MyMapReduce``, ``MySpark``, and ``AnotherMapReduce`` programs and
the ``MyAction`` custom action will be executed in order.

.. _workflow-custom-actions:

Workflow Custom Action
----------------------
In addition to MapReduce and Spark programs, workflow can also execute custom actions.
Custom actions are implemented in Java and can perform tasks such as sending an email. To
define a custom action, you will need to extend the ``AbstractWorkflowAction`` and
implement the ``run()`` method::

  public static class MyAction extends AbstractWorkflowAction {

    @Override
    public void run() {
      // your code goes here
    }
  }

The custom action then can be added to the workflow using the ``addAction()`` method as
shown above.

.. _workflow-unique-names:

Assigning Unique Names
----------------------
It's important to assign unique names to each component of the workflow, especially when
you use  multiple instances of the same program in the same workflow.

These unique names can be set when the Workflow is first configured, passed to the
instance of the program, and then be used when the program performs its own configuration.

An example of this is the :ref:`Wikipedia Pipeline <examples-wikipedia-data-pipeline>` example, and
its use of the *StreamToDataset* MapReduce program multiple times::

  public class StreamToDataset extends AbstractMapReduce {
    ...
    private final String name;
  
    public StreamToDataset(String name) {
      this.name = name;
    }
  
    @Override
    public void configure() {
      setName(name);
      ...
    }

In its declaration of the application, the example specifies the unique names for each instance::

  public class WikipediaPipelineApp extends AbstractApplication {
    ...
    static final String LIKES_TO_DATASET_MR_NAME = "likesToDataset";
    static final String WIKIPEDIA_TO_DATASET_MR_NAME = "wikiDataToDataset";

    @Override
    public void configure() {
      ...
      addMapReduce(new StreamToDataset(LIKES_TO_DATASET_MR_NAME));
      addMapReduce(new StreamToDataset(WIKIPEDIA_TO_DATASET_MR_NAME));
      ...
      addWorkflow(new WikipediaPipelineWorkflow());
    }
  }

The workflow itself uses the same names in its configuration::

  public class WikipediaPipelineWorkflow extends AbstractWorkflow {

    public static final String NAME = WikipediaPipelineWorkflow.class.getSimpleName();

    @Override
    protected void configure() {
      setName(NAME);
      setDescription("A workflow that demonstrates a typical data pipeline to process Wikipedia data.");
      addMapReduce(WikipediaPipelineApp.LIKES_TO_DATASET_MR_NAME);
      condition(new EnoughDataToProceed())
        .condition(new IsWikipediaSourceOnline())
          .addAction(new DownloadWikiDataAction())
        .otherwise()
          .addMapReduce(WikipediaPipelineApp.WIKIPEDIA_TO_DATASET_MR_NAME)
        .end()
        .addMapReduce(WikiContentValidatorAndNormalizer.NAME)
        .fork()
          .addSpark(SparkWikipediaAnalyzer.NAME)
        .also()
          .addMapReduce(TopNMapReduce.NAME)
        .join()
      .otherwise()
      .end();
    }

.. _workflow-concurrent:

Concurrent Workflows
--------------------
By default, a workflow runs sequentially. Multiple instances of a workflow can be run
concurrently. To enable concurrent runs for a workflow, set its runtime argument
``concurrent.runs.enabled`` to ``true``.


.. _workflow_token:

Workflow Tokens
===============

In addition to passing the control flow from one node to the next, a **workflow token** is
passed, available to each of the programs in the workflow. This allows programs to:

- pass custom data (such as a counter, a status, or an error code) from one program in the 
  workflow to subsequent programs;
- query and set the data in the token;
- fetch the data from the token which was set by a specific node; and
- alter the job configuration based on a key in the token; for example, set a different
  mapper/reducer class or a different input/output dataset for a Spark or MapReduce program.
  
The API is intended to allow appropriate action to be taken in response to the token, including
logging and modifying the conditional execution of the workflow based on the token.

Once a run is completed, you can query the tokens from past workflow runs for analyses that
determine which node was executed more frequently and when. You can retrieve the token values
that were added by a specific node in the workflow to debug the flow of execution.

Scope
-----
Two scopes |---| *System* and *User* |---| are provided for workflow keys. CDAP adds keys
(such as MapReduce counters) under the *System* scope. Keys added by user programs are
stored under the *User* scope.

Putting and Getting Token Values
--------------------------------
When a value is put into a token, it is stored under a specific key. Both keys and their
corresponding values must be non-null. The token stores additional information about the 
context in which the key is being set, such as the unique name of the workflow node. 
To put a value into a token, first obtain access to the token from the workflow context,
and then set a value for a specific key. 

In the case of a MapReduce program, the program's Mapper and Reducer classes need to
implement ``ProgramLifecycle<MapReduceContext>``. After doing so, they can access the
workflow token in either the ``initialize`` or ``destroy`` methods. To access it in the
``map`` or ``reduce`` methods, you would need to cache a reference to the workflow token
object as a class member in the ``initialize()`` method. This is because the context
object passed to those methods is a Hadoop class that is unaware of CDAP and its workflow
tokens.

Here is an example, taken from the
:ref:`Wikipedia Pipeline <examples-wikipedia-data-pipeline>` example's ``TopNMapReduce.java``:

.. literalinclude:: /../../../cdap-examples/WikipediaPipeline/src/main/java/co/cask/cdap/examples/wikipedia/TopNMapReduce.java
   :language: java
   :lines: 109-123

**Note:** The test of ``workflowToken != null`` is only required because this Reducer could
be used outside of a workflow. When run from within a workflow, the token is guaranteed to
be non-null.

The `WorkflowToken Java API 
<../../reference-manual/javadocs/co/cask/cdap/api/workflow/WorkflowToken.html)>`__
includes methods for getting values for different keys, scopes, and nodes. The same
key can be added to the workflow by different nodes, and there are methods to return a map of those
key-value pairs. Convenience methods allow the putting and getting of non-string values
through the use of the class Value.

Spark Accumulators and Workflow Tokens
--------------------------------------
`Spark Accumulators <https://spark.apache.org/docs/latest/programming-guide.html#accumulators-a-nameaccumlinka>`__ 
can be accessed through the SparkContext, and used with workflow tokens. This allows the 
values in the accumulators to be accessed through workflow tokens. An example of this is in
the :ref:`Wikipedia Pipeline <examples-wikipedia-data-pipeline>` example's ``ScalaSparkLDA.scala``:

.. literalinclude:: /../../../cdap-examples/WikipediaPipeline/src/main/scala/co/cask/cdap/examples/wikipedia/ScalaSparkLDA.scala
   :language: scala
   :lines: 81-87


Persisting the WorkflowToken
----------------------------
The ``WorkflowToken`` is persisted after each action in the workflow has completed.

Examples
--------

In this code sample, we show how to update the WorkflowToken in a MapReduce program::

  @Override
  public void beforeSubmit(MapReduceContext context) throws Exception {
    ...
    WorkflowToken workflowToken = context.getWorkflowToken();
    if (workflowToken != null) {
      // Put the action type in the WorkflowToken
      workflowToken.put("action.type", "MAPREDUCE");
      // Put the start time for the action
      workflowToken.put("start.time", String.valueOf(System.currentTimeMillis()));
    }
    ...
  }
 
  @Override
  public void onFinish(boolean succeeded, MapReduceContext context) throws Exception {
    ...
    WorkflowToken workflowToken = context.getWorkflowToken();
    if (workflowToken != null) {
      // Put the end time for the action
      workflowToken.put("end.time", String.valueOf(System.currentTimeMillis()));
    }
    ...
  }

**A token can only be updated** in:

- ``beforeSubmit`` and ``onFinish`` methods of a MapReduce program;
- Driver of a Spark program;
- ``initialize`` and ``destroy`` methods of a custom action; and 
- predicates of condition nodes.

**You will get an exception** if you try to update the workflow token in:

- map or reduce methods;
- Executors in Spark programs; or
- ``run`` method in custom actions.

You can always read the workflow token in any of the above situations. The :ref:`Wikipedia
Pipeline example <examples-wikipedia-data-pipeline>` demonstrates some of these techniques.


.. _workflow_parallel:

Parallelizing Workflow Execution
================================

The control flow of a workflow can be described as a directed, acyclic graph (DAG) of actions.
To be more precise, we require that it be a series-parallel graph. This is a graph with a
single start node and a single finish node. In between, execution can fork into concurrent
branches, but the graph may not have cycles. Every action can be a batch job or a custom
action (implemented in Java; for example, making a RESTful call to an external system).

For example, a simple control flow could be computing user and product profiles from
purchase events. After the start, a batch job could start that joins the events with the
product catalog. After that, execution could continue with a fork, and with two batch jobs
running in parallel: one computing product profiles; while the other computes user
profiles. When they are both done, execution is joined and continues with a custom action
to upload the computed profiles to a serving system, after which the control flow
terminates:

.. image:: /_images/parallelized-workflow.png
   :width: 8in
   :align: center

Forks and Joins
---------------

To create such a workflow, you provide a series of *forks* and *joins* in your workflow
specification, following these rules:

- Where your control flow initially splits, you place a ``fork`` method. 
- Every time your control flow splits, you add additional ``fork`` methods. 
- Every point where you have either a program or an action, you add a ``addMapReduce``,
  ``addSpark``, or ``addAction`` method. 
- To show each fork, use a ``also`` method to separate the different branches of the
  control flow. 
- Where your control flow reconnects, you add a ``join`` method to indicate. 
- The control flow always concludes with a ``join`` method.

The application shown above could be coded (assuming the other classes referred to exist) as::

  public class ParallelizedWorkflow extends AbstractWorkflow {

    @Override
    public void configure() {
      setName("ParallelizedWorkflow");
      setDescription("Demonstration of parallelizing execution of a workflow");
      
      addMapReduce("JoinWithCatalogMR");
    
      fork()
        .addMapReduce("BuildProductProfileMR")
      .also()
        .addMapReduce("BuildUserProfileMR")
      .join();
      
      addAction(new UploadProfilesCA());
    }
  }

Provided that the control flow does not have cycles or the joining of any branches that do
not originate from the same fork, flows of different complexity can be created using these
rules and methods.

More complicated structures can be created using ``fork``. To add another MapReduce
that runs in parallel to the entire process described above, you could use code such as::

  public class ComplexParallelizedWorkflow extends AbstractWorkflow {

    @Override
    public void configure() {
      setName("ComplexParallelizedWorkflow");
      setDescription("Demonstration of parallelized execution using a complex fork in a workflow");

      fork()
        .addMapReduce("JoinWithCatalogMR")
        .fork()
          .addMapReduce("BuildProductProfileMR")
        .also()
          .addMapReduce("BuildUserProfileMR")
        .join()
          .addAction(new UploadProfilesCA())
      .also()
        .addMapReduce("LogMonitoringMR")
      .join();
    }
  }

The diagram for this code would be:

.. image:: /_images/complex-parallelized-workflow.png
   :width: 8in
   :align: center

Conditional Node
----------------

You can provide a *conditional* node in your structure that allows for branching based on 
a boolean predicate.

Taking our first example and modifying it, you could use code such as::

  public class ConditionalWorkflow extends AbstractWorkflow {

    @Override
    public void configure() {
      setName("ConditionalWorkflow");
      setDescription("Demonstration of conditional execution of a workflow");
      
      addMapReduce("JoinWithCatalogMR");
      
      condition(new MyPredicate())
        .addMapReduce("BuildProductProfileMR")
      .otherwise()
        .addMapReduce("BuildUserProfileMR")
      .end();
      
      addAction(new UploadProfilesCA());
    }
  }

where ``MyPredicate`` is a public class which implements the ``Predicate`` interface as::

  public static class MyPredicate implements Predicate<WorkflowContext> {

    @Override
    public boolean apply(@Nullable WorkflowContext input) {
       if (input == null) {
          return false;
       }
       String flattenCounterName = "org.apache.hadoop.mapreduce.TaskCounter.MyCustomCounters";
       Value tokenValue = input.getToken().get(flattenCounterName, "BuildProductProfile", WorkflowToken.Scope.SYSTEM);
       if (tokenValue == null) {
          return false;
       }
       if (tokenValue.getAsInt() > 0) {
         return true;
       }
       return false;
    }
  }
  
In the ``JoinWithCatalogMR`` MapReduce, it could have in its Mapper class code that 
governs which condition to follow. Note that as the context passed is a standard
Hadoop context, the ``WorkflowContext`` is not available::

  public static final class JoinWithCatalogMR extends AbstractMapReduce {

    @Override
    public void configure() {
      setName("JoinWithCatalogMR");
      setDescription("MapReduce program to demonstrate a Conditional workflow");
    }

    @Override
    public void beforeSubmit(MapReduceContext context) throws Exception {
      Job job = context.getHadoopJob();
      job.setMapperClass(MyVerifier.class);
      String inputPath = context.getRuntimeArguments().get("inputPath");
      String outputPath = context.getRuntimeArguments().get("outputPath");
      FileInputFormat.addInputPath(job, new Path(inputPath));
      FileOutputFormat.setOutputPath(job, new Path(outputPath));
    }
  }

  public static class MyVerifier extends Mapper<LongWritable, Text, Text, NullWritable> {
    public void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {
      if (value != null and value.toString().equals("BuildProductProfile")) {
        context.getCounter("MyCustomCounters", "BuildProductProfile").setValue(1L);
      } else {
        context.getCounter("MyCustomCounters", "BuildProductProfile").setValue(0);
      }
    }
  }

In this case, if the predicate finds that the ``MapReduceCounter`` *BuildProductProfile*
is greater than zero, the logic will follow the path of *BuildProductProfileMR*;
otherwise, the other path will be taken. The diagram for this code would be:

.. image:: /_images/conditional-workflow.png
   :width: 8in
   :align: center

Workflow Token with Forks and Joins
-----------------------------------
For workflows that involve forks and joins, a single instance of the workflow token is
shared by all branches of the fork. Updates to the singleton are made thread-safe through
synchronized updates, guaranteeing that value you obtain from reading the token is the
last value written at runtime. This is a time-based guarantee.

Example
-------

This code sample shows how to obtain values from the token from within a custom action,
and from within a workflow with a predicate, fork and joins::

  @Override
  public void run() {
    ...
    WorkflowToken token = getContext().getToken();
    
    // Set the type of action of the current node:
    token.put("action.type", "CUSTOM_ACTION");
 
    // Assume that we have the following Workflow: 
    //                                              |--> PurchaseByCustomer -->|
    //                                        True  |                          |   
    // Start --> RecordVerifier --> Predicate ----->|                          |----> StatusReporter --> End    
    //                                  |           |                          |  |
    //                                  | False     |--> PurchaseByProduct --->|  |
    //                                  |                                         |
    //                                  |------------> ProblemLogger ------------>|
 
    // Use case 1: Predicate can add the key "branch" in the WorkflowToken with value as
    // "true" if true branch will be executed or "false" otherwise. In "StatusReporter" in
    // order to get which branch in the Workflow was executed, use:
    
    boolean bTrueBranch = token.getAsBoolean("branch");
 
    // Use case 2: You may want to compare the records emitted by "PurchaseByCustomer"
    // and "PurchaseByProduct", in order to find which job is generating more records:
    
    String flattenReduceOutputRecordsCounterName = "org.apache.hadoop.mapreduce.TaskCounter.REDUCE_OUTPUT_RECORDS";
    String purchaseByCustomerCounterValue = token.get(flattenReduceOutputRecordsCounterName, "PurchaseByCustomer", 
                                                      WorkflowToken.Scope.SYSTEM);
    String purchaseByProductCounterValue = token.get(flattenReduceOutputRecordsCounterName, "PurchaseByProduct", 
                                                     WorkflowToken.Scope.SYSTEM);
  
    // Use case 3: Since Workflow can have multiple complex conditions and forks in its
    // structure, in the "StatusReporter", you may want to know how many actions were
    // executed as a part of a run. If the number of nodes executed were below a certain
    // threshold, send an alert. Assuming that every node in the Workflow adds the key
    // "action.type" with the value as action type for that node in the WorkflowToken,
    // you can determine the breakdown by action type in a particular Workflow run:
    
    List<NodeValue> nodeValues = token.getAll("action.type");
    int totalNodeExecuted = nodeValues.size();
    int mapReduceNodes = 0;
    int sparkNodes = 0;
    int customActionNodes = 0;
    int conditions = 0;
    for (NodeValue entry : nodeValues) {
      if (entry.getValue().equals("MAPREDUCE")) {
        mapReduceNodes++;
      }
      if (entry.getValue().equals("SPARK")) {
        sparkNodes++;
      }
      if (entry.getValue().equals("CUSTOM_ACTION")) {
        customActionNodes++;
      }
      if (entry.getValue().equals("CONDITION")) {
        conditions++;
      }
    }
 
    // Use case 4: Retrieve values from the Workflow token.
    
    // To get the name of the last node which set the "ERROR" flag in the WorkflowToken:
    
    List<NodeValue> errorNodeValueList = token.getAll("ERROR");
    String nodeNameWhoSetTheErrorFlagLast = errorNodeValueList.get(errorNodeValueList.size() - 1);
 
    // To get the start time of the MapReduce program with unique name "PurchaseHistoryBuilder":
    
    String startTime = token.get("start.time", "PurchaseHistoryBuilder");
 
    // To get the most recent value of counter with group name
    // 'org.apache.hadoop.mapreduce.TaskCounter' and counter name 'MAP_INPUT_RECORDS':
   
    String flattenCounterKey = "mr.counters.org.apache.hadoop.mapreduce.TaskCounter.MAP_INPUT_RECORDS";
    workflowToken.get(flattenCounterKey, WorkflowToken.Scope.SYSTEM);
 
    // To get the value of counter with group name
    // 'org.apache.hadoop.mapreduce.TaskCounter' and counter name 'MAP_INPUT_RECORDS' as
    // set by a MapReduce program with unique name 'PurchaseHistoryBuilder':
    
    workflowToken.get(flattenCounterKey, "PurchaseHistoryBuilder", WorkflowToken.Scope.SYSTEM);
   ...
  }


Example of Using a Workflow
===========================

- For an example of the use of **a workflow,** see the :ref:`Purchase
  <examples-purchase>` example.
- The :ref:`Wikipedia Pipeline <examples-wikipedia-data-pipeline>` example is another workflow example.
