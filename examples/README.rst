============================
Continuuity Reactor Examples
============================

This ``/examples`` directory contains example apps for the Continuuity Reactor. 
They are not compiled as part of the master build, and they should only depend 
on the API jars (plus their dependencies). However, they may also be provided 
in their compiled forms as JAR files in this release.

Building
========

Each example comes with Maven ``.pom`` file. To build, install Maven, and from your
``/examples`` directory prompt, enter::

	mvn clean package


List of Example Apps
========================

CountAndFilterWords
-------------------
- A variation of CountTokens that illustrates that a flowlet's output can
  be consumed by multiple downstream flowlets.
- In addition to counting all tokens, also sends all tokens to a filter that
  drops all tokens that are not upper case
- The upper case tokens are then counted by a separate flowlet

CountCounts
-----------
- A very simple flow that counts counts.
- Reads input stream 'text' and tokenizes it. Instead of counting words, it
  counts the number of inputs with the same number of tokens.

CountOddAndEven
---------------
- Consumes generated random numbers and counts odd and even numbers.

CountRandom
-----------
- Generates random numbers between 0 and 9999
- For each number *i*, generates i%10000, i%1000, i%100, i%10
- Increments the counter for each number.
 
CountTokens
-----------
- Reads events (``= byte[] body, Map<String,String>`` headers) from input
  stream 'text'.
- Tokenizes the text in the body and in the header named 'title', ignores
  all other headers.
- Each token is cloned into two tokens:

  #. the upper cased version of the token
  #. the original token with a field prefix ('title', or if the token is from
     the body of the event, 'text')

- All of the cloned tokens are counted using increment operations.

HelloWorld
----------
- This is a simple HelloWorld example that uses one stream, one DataSet, one flow and one procedure.
- A stream to send names to.
- A flow with a single flowlet that reads the stream and stores each name in a KeyValueTable.
- A procedure that reads the name from the KeyValueTable and prints Hello [Name]!

PageViewAnalytics
-----------------
- This example demonstrates use of custom DataSets and batch processing in an Application.
- It takes data from Apache access logs, parses them and save the data in a custom DataSet.
  It then queries the results to find, for a specific URI, pages that are requesting that
  page and the distribution of those requests. 

Purchase
--------
- An app that uses scheduled MapReduce workflows to read from one ObjectStore DataSet
  and write to another.

  - Send sentences of the form "Tom bought 5 apples for $10" to the purchaseStream.
  - The PurchaseFlow reads the purchaseStream and converts every input String into a
    Purchase object and stores the object in the purchases DataSet.
  - When scheduled by the PurchaseHistoryWorkflow, the PurchaseHistoryBuilder MapReduce job
    reads the purchases DataSet, creates a purchase history,
    and stores the purchase history in the history DataSet every morning at 4:00 A.M.
    You can manually (in the Process screen in the Reactor Dashboard) or programmatically execute 
    the PurchaseHistoryBuilder MapReduce job to store customers' purchase history in the history
    DataSet.
  - Execute the PurchaseQuery procedure to query the history DataSet to discover
    the purchase history of each user.

- Note: Because by default the PurchaseHistoryWorkflow process doesn't run until 4:00 A.M.,
  you'll have to wait until the next day (or manually or programmatically execute the
  PurcaseHistoryBuilder) after entering the first customers' purchases or the PurchaseQuery will
  return a "not found" error.

ResourceSpammer
---------------
- An example designed to stress test CPU resources.

ResponseCodeAnalytics
---------------------
- A simple application for real-time streaming log analysis—computing 
  the number of occurrences of each HTTP status code by processing Apache access log data. 

SentimentAnalysis
-----------------
- An application that analyzes sentiment of sentences as positive, negative or neutral.

SimpleWriteAndRead
------------------
- A simple example to illustrate how to read and write key/values in a flow.

TrafficAnalytics
----------------
- This application pulls in stock market activity data and stores it in 
  DataSets that allow querying for that data with various filters.

WordCount
---------
- A simple application that counts words 
  and tracks word associations and unique words seen on the stream. It 
  demonstrates the power of using DataSets and how they can be used to simplify 
  storing complex data.

.. include:: includes/footer.rst