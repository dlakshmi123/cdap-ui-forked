.. meta::
    :author: Cask Data, Inc.
    :description: Cask Data Application Platform Wikipedia Pipeline Application
    :copyright: Copyright © 2015 Cask Data, Inc.

.. _examples-wikipedia-data-pipeline:

==================
Wikipedia Pipeline
==================

A Cask Data Application Platform (CDAP) example demonstrating a typical batch data
processing pipeline using CDAP Workflows.


Overview
========
This example demonstrates a CDAP application performing analysis on Wikipedia data using MapReduce and Spark programs
running within a CDAP Workflow: *WikipediaPipelineWorkflow*.

This example can be run in both online and offline modes.

- In the **online mode**, the MapReduceProgram *WikipediaDataDownloader* reads the stream
  *pageTitleStream*, each event of which is an element from the output of the `Facebook
  "Likes" API
  <https://developers.facebook.com/docs/graph-api/reference/v2.4/object/likes>`__. For
  each event, it tries to download Wikipedia data for the page using the `MediaWiki
  Wikipedia API <https://www.mediawiki.org/wiki/API:Main_page>`__. It stores the
  downloaded data in the ``KeyValueTable`` dataset *wikiData*.

..

- In the **offline mode**, it expects Wikipedia data formatted following the output of the
  MediaWiki API in the stream *wikiStream*. The MapReduce program *wikiDataToDataset*
  consumes this stream and stores it in the same ``KeyValueTable`` dataset *wikiData*.
  Data can be uploaded to the *wikiStream* using the CDAP CLI.

Once raw Wikipedia data is available from using either the online or offline modes, the
*WikipediaPipelineWorkflow* runs a MapReduce program *WikiContentValidatorAndNormalizer* that filters bad records from
the raw data, as well as normalizes it by converting the wikitext-formatted data to plain text. It then stores the
output in another ``KeyValueTable`` dataset called *normalized*.

The *WikipediaPipelineWorkflow* then contains a fork, with two branches:

- One branch runs the Apache Spark program *SparkWikipediaAnalyzer*. This program consumes
  normalized data and runs topic modeling on it using the 
  `Latent Dirichlet Allocation (LDA) <https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation>`__
  algorithm. It stores its output in the CDAP Table dataset *lda*, with one row for each
  iteration, and a column per topic containing the score for that topic.

..

- The other branch contains a MapReduce program *TopNMapReduce* that consumes the
  normalized data and produces the top "N" words in the dataset *topn*.

Let's look at some of these components, and then run the application and see the results.

The WikipediaPipeline Application
---------------------------------
As in the other `examples <index.html>`__, the components
of the application are tied together by the class ``WikipediaPipelineApp``:

.. literalinclude:: /../../../cdap-examples/WikipediaPipeline/src/main/java/co/cask/cdap/examples/wikipedia/WikipediaPipelineApp.java
   :language: java
   :lines: 24-57

This application demonstrates:

- The use of assigning unique names, as the same MapReduce (*StreamToDataset*) is used twice in the workflow
  (*WikipediaPipelineWorkflow*) under two different names.
  
- The use of Workflow Tokens in:

  - Condition Predicates
  - Setting MapReduce program configuration (setting it based on values in the token)
  - ``map()`` and ``reduce()`` functions (read-only, no updates)
  - Spark Programs (reading from |---| and writing to |---| the workflow token; adding
    Spark Accumulators to the workflow token)
  - Assertions in application unit tests


.. Building and Starting
.. =====================
.. |example| replace:: WikipediaPipeline
.. |example-italic| replace:: *WikipediaPipeline*
.. |application-overview-page| replace:: :cdap-ui-apps-programs:`application overview page, programs tab <WikipediaPipeline>`

.. include:: _includes/_building-starting-running.txt


Running the Example
===================

.. highlight:: console

Injecting data
--------------
The *pageTitleStream* consumes events in the format returned by the Facebook "Likes" Graph API.

- Inject a file of Facebook "Likes" data to the stream *pageTitleStream* by running this command from the Standalone
  CDAP SDK directory, using the CDAP Command Line Interface::

    $ cdap-cli.sh load stream pageTitleStream examples/WikipediaPipeline/resources/fb-likes-data.txt
    Successfully sent stream event to stream 'pageTitleStream'

The *wikiStream* consumes events in the format returned by the MediaWiki Wikipedia API.

- Inject a file of "Wikipedia" data to the stream *wikiStream* by running this command from the Standalone
  CDAP SDK directory, using the Command Line Interface::

    $ cdap-cli.sh load stream wikiStream examples/WikipediaPipeline/resources/wikipedia-data.txt
    Successfully sent stream event to stream 'wikiStream'

.. Start the Workflow
.. ------------------
.. |example-workflow| replace:: WikipediaPipelineWorkflow
.. |example-workflow-italic| replace:: *WikipediaPipelineWorkflow*
.. include:: _includes/_starting-workflow.txt

These runtime arguments can be set for the *WikipediaPipelineWorkflow*:

- *min.pages.threshold*: Threshold for the number of pages to exist in the *pageTitleStream* for the workflow to proceed.
  Defaults to 10.
- *mode*: Set this to 'online' when you wish to download Wikipedia data over the Internet.
  Defaults to 'offline', in which case the workflow expects Wikipedia data to be in the *wikiStream*.
- *stopwords.file*: The path to the file containing stopwords to filter in the *SparkWikipediaAnalyzer* program.
  If unspecified, no words are considered as stopwords.
- *vocab.size*: The size of the vocabulary for the *SparkWikipediaAnalyzer* program. Defaults to 1000.
- *topn.rank*: The number of top words to produce in the *TopNMapReduce* program. Defaults to 10.
- *num.reduce.tasks*: The number of reduce tasks to set for the *TopNMapReduce* program. Defaults to 1.

If you run with the default arguments, you will find that the pipeline starts but then
stops after the first node, as the number of pages is less than the *min.pages.threshold*:

.. image:: _images/wikipedia-data-pipeline-1.png
   :width: 8in

Reduce the number of minimum number of pages to zero, and change the mode to *online*, by
setting either the runtime arguments (which changes the *next* run) or the preferences
(which changes *all* subsequent runs):

.. image:: _images/wikipedia-data-pipeline-2.png
   :width: 8in

Once the pipeline has run through to the end (below), you can start the service and query the results.

.. image:: _images/wikipedia-data-pipeline-3.png
   :width: 8in

.. Start the Service
.. -----------------
.. |example-service| replace:: WikipediaService
.. |example-service-italic| replace:: *WikipediaService*
.. include:: _includes/_starting-service.txt


Retrieving the Results
----------------------
The *WikipediaService* can retrieve results from the analysis performed by the *WikipediaPipelineWorkflow*.
The service exposes these REST APIs, which can be accessed either with the CDAP-CLI or ``curl``.

- Retrieve the list of topics generated by the *SparkWikipediaAnalyzer* program:

  .. container:: highlight

    .. parsed-literal::
      |$| cdap-cli.sh call service |example|.\ |example-service| GET /v1/functions/lda/topics
      
      |$| curl -w'\\n' -v 'localhost:10000/v3/namespaces/default/apps/|example|/services/|example-service|/methods/v1/functions/lda/topics'
      
      [0,1,2,3,4,5,6,7,8,9]

- Retrieve the details (terms and term weights) for a given (integer) topic:

  .. container:: highlight

    .. parsed-literal::
    
      |$| cdap-cli.sh call service |example|.\ |example-service| GET /v1/functions/lda/topics/{topic}

      |$| cdap-cli.sh call service |example|.\ |example-service| GET /v1/functions/lda/topics/0
      
      |$| curl -w'\\n' -v 'localhost:10000/v3/namespaces/default/apps/|example|/services/|example-service|/methods/v1/functions/lda/topics/0'
      
      [{"name":"and","weight":0.038682279584092004},{"name":"company","weight":0.011716155714206075},
      {"name":"facebook","weight":0.03279816812913312},{"name":"for","weight":0.0236260327332555},
      {"name":"google","weight":0.03240608486488011},{"name":"its","weight":0.01541806996121385},
      {"name":"that","weight":0.032277216101403945},{"name":"the","weight":0.08955250785732792},
      {"name":"users","weight":0.013512787321319556},{"name":"was","weight":0.014201825107197289}]      

- Retrieve the output of the *TopNMapReduce* program:

  .. container:: highlight

    .. parsed-literal::
      |$| cdap-cli.sh call service |example|.\ |example-service| GET /v1/functions/topn/words
      
      |$| curl -w'\\n' -v 'localhost:10000/v3/namespaces/default/apps/|example|/services/|example-service|/methods/v1/functions/topn/words'

      [{"The":627},{"a":1466},{"and":1844},{"in":1415},{"of":2076},{"on":604},{"that":644},{"the":3857},{"to":1620},{"was":740}]
      

.. Stopping and Removing the Application
.. =====================================
.. include:: _includes/_stopping-workflow-service-removing-application.txt
