.. meta::
    :author: Cask Data, Inc.
    :description: HTTP RESTful Interface to the Cask Data Application Platform
    :copyright: Copyright © 2015-2016 Cask Data, Inc.

.. _http-restful-api-artifact:

=========================
Artifact HTTP RESTful API 
=========================

.. highlight:: console

Use the CDAP Artifact HTTP RESTful API to deploy artifacts, list available artifacts, and
retrieve information about plugins available to artifacts. Artifacts, their use, and
examples of using them, are described in the
:ref:`Developers' Manual: Artifacts <artifacts>`.


.. Base URL explanation
.. --------------------
.. include:: base-url.txt


.. _http-restful-api-artifact-add:

Add an Artifact
===============
An artifact can be added (loaded) with an HTTP POST method to the URL::

  POST /v3/namespaces/<namespace-id>/artifacts/<artifact-name>

The request body must contain the binary contents of the artifact.

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact to be created

Several optional headers may also be specified:

.. list-table::
   :widths: 20 40 40
   :header-rows: 1

   * - Header
     - Description
     - Example
   * - **Artifact-Version**
     - The version of the artifact to add. If not specified, the ``Bundle-Version`` attribute
       in the JAR file's Manifest will be used.
     - ``1.0.0``
   * - **Artifact-Extends**
     - If the artifact contains plugins, describes which parent artifacts should have access to those plugins.
       Multiple parents can be given by separating them with a ``/`` 
     - ``cdap-data-pipeline[3.2.0,4.0.0)/cdap-etl-realtime[3.2.0,4.0.0)``
   * - **Artifact-Plugins**
     - JSON Array of plugins contained in the artifact that are not annotated as a plugin.
       This should be used for third-party JARs that need to be plugins, such as JDBC drivers. Each element
       in the array is a JSON object containing name, type, and className of the plugin.
     - ``[ { "name": "mysql", "type": "jdbc", "className": "com.mysql.jdbc.Driver" } ]``

.. _http-restful-api-artifact-available:

List Available Artifacts 
========================
To retrieve a list of available artifacts, submit an HTTP GET request::

  GET /v3/namespaces/<namespace-id>/artifacts[?scope=<scope>]

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``scope``
     - Optional scope filter. If not specified, artifacts in the ``user`` and
       ``system`` scopes are returned. Otherwise, only artifacts in the specified scope are returned.

This will return a JSON array that lists each artifact with its name, version, and scope.
Example output (pretty-printed):

.. container:: highlight

  .. parsed-literal::
    |$| GET /v3/namespaces/default/artifacts
    [
      {
        "name": "cdap-data-pipeline",
        "scope": "SYSTEM",
        "version": "|release|"
      },
      {
        "name": "cdap-etl-realtime",
        "scope": "SYSTEM",
        "version": "|release|"
      },
      {
        "name": "Purchase",
        "scope": "USER",
        "version": "|release|"
      }
    ]

.. _http-restful-api-artifact-versions:

List Artifact Versions
======================
To list all versions of a specific artifact, submit an HTTP GET request::

  GET /v3/namespaces/<namespace-id>/artifact/<artifact-name>[?scope=<scope>]
  
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``scope``
     - Optional scope filter. If not specified, defaults to ``user``.

This will return a JSON array that lists each version of the specified artifact with
its name, version, and scope. Example output for the ``cdap-data-pipeline`` artifact (pretty-printed):

.. container:: highlight

  .. parsed-literal::
    |$| GET /v3/namespaces/default/artifacts/cdap-data-pipeline?scope=system
    [
      {
        "name": "cdap-data-pipeline",
        "scope": "SYSTEM",
        "version": "|release|"
      }
    ]

.. _http-restful-api-artifact-detail:

Retrieve Artifact Details
=========================
To retrieve details about a specific version of an artifact, submit an HTTP GET request::

  GET /v3/namespaces/<namespace-id>/artifacts/<artifact-name>/versions/<artifact-version>[?scope=<scope>]
  
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact
   * - ``scope``
     - Optional scope filter. If not specified, defaults to 'user'.

This will return a JSON object that contains information about: classes in the artifact;
the schema of the config object supported by the ``Application`` class; and the artifact name,
version, and scope. Example output for version |literal-release| of the ``WordCount``
artifact (pretty-printed and reformatted to fit):

.. container:: highlight

  .. parsed-literal::
    |$| GET /v3/namespaces/default/artifact/WordCount/versions/|release|?scope=system
    {
      "classes": {
        "apps": [
          {
            "className": "co.cask.cdap.examples.wordcount.WordCount",
            "configSchema": {
              "fields": [
                { "name": "stream", "type": [ "string", "null" ] },
                { "name": "uniqueCountTable", "type": [ "string", "null" ] },
                { "name": "wordAssocTable", "type": [ "string", "null" ] },
                { "name": "wordCountTable", "type": [ "string", "null" ] },
                { "name": "wordStatsTable", "type": [ "string", "null" ] }
              ],
              "name": "co.cask.cdap.examples.wordcount.WordCount$WordCountConfig",
              "type": "record"
            },
            "description": ""
          }
        ],
        "plugins": []
      },
      "name": "WordCount",
      "scope": "USER",
      "version": "|release|"
    }

.. _http-restful-api-artifact-set-properties:

Set Artifact Properties
=======================
To set properties for a specific version of an artifact, submit an HTTP PUT request::

  PUT /v3/namespaces/<namespace-id>/artifacts/<artifact-name>/versions/<artifact-version>/properties

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact

The request body must be a JSON object that contains the properties for the artifact.
The keys and values in the object must be strings. If any properties are already
defined, they will be overwritten.

.. container:: highlight

  .. parsed-literal::
    |$| PUT /v3/namespaces/default/artifact/WordCount/versions/|release|/properties -d 
    {
        "author": "samuel",
        "company": "cask"
    }

.. _http-restful-api-artifact-set-property:

Set an Artifact Property
========================
To set a specific property for a specific version of an artifact, submit an HTTP PUT request::

  PUT /v3/namespaces/<namespace-id>/artifacts/<artifact-name>/versions/<artifact-version>/properties/<property>
  
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact
   * - ``property``
     - Property to set

The request body must contain the value to set for the property. If the property already exists,
the previous value will be overwritten.

.. container:: highlight

  .. parsed-literal::
    |$| PUT /v3/namespaces/default/artifact/WordCount/versions/|release|/properties/author -d
    samuel

.. _http-restful-api-artifact-retrieve-properties:

Retrieve Artifact Properties
============================
To retrieve properties for a specific version of an artifact, submit an HTTP GET request::

  GET /v3/namespaces/<namespace-id>/artifacts/<artifact-name>/versions/<artifact-version>/properties[?scope=<scope>&keys=<keys>]
  
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact
   * - ``scope``
     - Optional scope filter. If not specified, defaults to 'user'.
   * - ``keys``
     - Optional comma-separated list of property keys to return. If not specified, all keys are returned. 

This will return a JSON object that contains the properties of the artifact.

.. container:: highlight

  .. parsed-literal::
    |$| GET /v3/namespaces/default/artifact/WordCount/versions/|release|/properties?keys=author,company
    { "author": "samuel", "company": "cask" }

.. _http-restful-api-artifact-retrieve-property:

Retrieve an Artifact Property
=============================
To retrieve a specific property for a specific version of an artifact, submit an HTTP GET request::

  GET /v3/namespaces/<namespace-id>/artifacts/<artifact-name>/versions/<artifact-version>/properties/<property>
  
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact
   * - ``property``
     - Property to retrieve

.. container:: highlight

  .. parsed-literal::
    |$| GET /v3/namespaces/default/artifact/WordCount/versions/|release|/properties/author
    samuel

.. _http-restful-api-artifact-delete-properties:

Delete Artifact Properties
==========================
To delete all properties for a specific version of an artifact, submit an HTTP DELETE request::

  GET /v3/namespaces/<namespace-id>/artifacts/<artifact-name>/versions/<artifact-version>/properties
  
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact

.. container:: highlight

  .. parsed-literal::
    |$| DELETE /v3/namespaces/default/artifact/WordCount/versions/|release|/properties

.. _http-restful-api-artifact-delete-property:

Delete an Artifact Property
===========================
To delete a specific property for a specific version of an artifact, submit an HTTP DELETE request::

  GET /v3/namespaces/<namespace-id>/artifacts/<artifact-name>/versions/<artifact-version>/properties/<property>
  
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact
   * - ``property``
     - Property key to delete

.. container:: highlight

  .. parsed-literal::
    |$| DELETE /v3/namespaces/default/artifact/WordCount/versions/|release|/properties/author

.. _http-restful-api-artifact-extensions:

List Extensions (Plugin Types) available to an Artifact
=======================================================
To list the extensions (plugin types) available to an artifact, submit
an HTTP GET request::

  GET /v3/namespaces/<namespace-id>/artifacts/<artifact-name>/versions/<artifact-version>/extensions[?scope=<scope>]
  
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact
   * - ``scope``
     - Optional scope filter. If not specified, defaults to 'user'.
  
This will return a JSON array that lists the extensions (plugin types) available to the artifact.
Example output for version |literal-release| of the ``cdap-data-pipeline`` artifact:

.. container:: highlight

  .. parsed-literal::
    |$| GET /v3/namespaces/default/artifacts/cdap-data-pipeline/versions/|release|/extensions?scope=system
    ["sparksink","postaction","transform","batchaggregator","sparkcompute","validator","realtimesource","action","batchsource","realtimesink","batchsink","batchjoiner"]

.. _http-restful-api-artifact-available-plugins:

List Plugins available to an Artifact
=====================================
To list plugins of a specific type available to an artifact, submit
an HTTP GET request::

  GET /v3/namespaces/<namespace-id>/artifacts/<artifact-name>/versions/<artifact-version>/extensions/<plugin-type>[?scope=<scope>]
  
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact
   * - ``plugin-type``
     - Type of plugins to list
   * - ``scope``
     - Optional scope filter. If not specified, defaults to 'user'.

This will return a JSON array that lists the plugins of the specified type
available to the artifact. Each element in the array is a JSON object containing
the artifact that the plugin originated from, and the plugin's class name, description, 
name, and type. Note that the details provided are a summary compared to those provided by
the endpoint :ref:`http-restful-api-artifact-plugin-detail`.

Example output for plugins of type ``transform`` available to version |literal-release|
of the ``cdap-data-pipeline`` artifact (pretty-printed and reformatted to fit):

.. container:: highlight

  .. parsed-literal::
    |$| GET /v3/namespaces/default/artifacts/cdap-data-pipeline/versions/|release|/extensions/transform?scope=system

    [
        {
            "name": "LogParser",
            "type": "transform",
            "description": "Parses logs from any input source for relevant information such as 
                URI, IP, browser, device, HTTP status code, and timestamp.",
            "className": "co.cask.hydrator.plugin.transform.LogParserTransform",
            "artifact": {
                "name": "core-plugins",
                "version": "|cask-hydrator-version|",
                "scope": "SYSTEM"
            }
        },
        {
            "name": "JavaScript",
            "type": "transform",
            ...
        },
        ...
    ]

.. _http-restful-api-artifact-plugin-detail:

Retrieve Plugin Details
=======================
To retrieve details about a specific plugin available to an artifact, submit
an HTTP GET request::

  GET /v3/namespaces/<namespace-id>/artifacts/<artifact-name>/versions/<artifact-version>/extensions/<plugin-type>/plugins/<plugin-name>[?scope=<scope>]
  
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact
   * - ``plugin-type``
     - Type of the plugin
   * - ``plugin-name``
     - Name of the plugin
   * - ``scope``
     - Optional scope filter. If not specified, defaults to 'user'.

This will return a JSON array that lists the plugins of the specified type and name
available to the artifact. As can been seen compared with the endpoint
:ref:`http-restful-api-artifact-available-plugins`, this provides all details
on the specified plugin. Each element in the array is a JSON object containing the
artifact that the plugin originated from, and the plugin's class name, description, name,
type, and properties.

Example output for the ``ScriptFilter`` plugin available to version |literal-release|
of the ``cdap-data-pipeline`` artifact (pretty-printed and reformatted to fit):

.. container:: highlight

  .. parsed-literal::
    |$| GET /v3/namespaces/default/artifacts/cdap-data-pipeline/versions/|release|/extensions/transform/plugins/ScriptFilter?scope=system

    [
        {
            "properties": {
                "lookup": {
                    "name": "lookup",
                    "description": "Lookup tables to use during transform. Currently supports KeyValueTable.",
                    "type": "string",
                    "required": false
                },
                "script": {
                    "name": "script",
                    "description": "JavaScript that must implement a function
                      'shouldFilter' that takes a JSON object representation of the input
                      record and a context object (which encapsulates CDAP metrics and
                      logger) and returns true if the input record should be filtered and
                      false if not. For example:\n'function shouldFilter(input, context)
                      {\nif (input.count < 0) {\ncontext.getLogger().info(\"Got input record
                      with negative
                      count\");\ncontext.getMetrics().count(\"negative.count\",
                      1);\n}\nreturn input.count > 100;\n}\n' will filter out any records
                      whose 'count' field is greater than 100.",
                    "type": "string",
                    "required": true
                }
            },
            "endpoints": [

            ],
            "name": "ScriptFilter",
            "type": "transform",
            "description": "A transform plugin that filters records using a custom
                JavaScript provided in the plugin's config.",
            "className": "co.cask.hydrator.plugin.transform.ScriptFilterTransform",
            "artifact": {
                "name": "core-plugins",
                "version": "|cask-hydrator-version|",
                "scope": "SYSTEM"
            }
        }
    ]


.. _http-restful-api-artifact-delete:

Delete an Artifact
==================
To delete an artifact, submit an HTTP DELETE request::

  DELETE /v3/namespaces/<namespace-id>/artifacts/<artifact-name>/versions/<artifact-version>

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact

Deleting an artifact is an advanced feature. If there are programs that use the artifact, those
programs will not be able to start unless the artifact is added again, or the program application
is updated to use a different artifact. 

.. _http-restful-api-artifact-system-load:

Load System Artifacts
=====================
To load all system artifacts on the CDAP Master node(s), submit an HTTP POST request::

  POST /v3/namespaces/system/artifacts

This call will make the CDAP master scan the artifacts directly and add any new artifacts
that it finds. Any snapshot artifacts will be re-loaded.

.. _http-restful-api-artifact-system-delete:

Delete a System Artifact
========================
To delete a system artifact, submit an HTTP DELETE request::

  DELETE /v3/namespaces/system/artifacts/<artifact-name>/versions/<artifact-version>

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``artifact-name``
     - Name of the artifact
   * - ``artifact-version``
     - Version of the artifact

Deleting an artifact is an advanced feature. If there are programs that use the artifact, those
programs will not be able to start unless the artifact is added again, or the program application
is updated to use a different artifact. 

.. _http-restful-api-artifact-app-classes:

List Application Classes
========================
To list application classes, submit an HTTP GET request::

  GET /v3/namespaces/<namespace-id>/classes/apps[?scope=<scope>]

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``scope``
     - Optional scope filter. If not specified, classes from artifacts in the ``user`` and
       ``system`` scopes are returned. Otherwise, only classes from artifacts in the specified scope are returned.

This will return a JSON array that lists all application classes contained in artifacts.
Each element in the array is a JSON object that describes the artifact the class originates in
as well as the class name. Example output for the ``ScriptFilter`` (pretty-printed and reformatted to fit):

.. container:: highlight

  .. parsed-literal::
    |$| GET /v3/namespaces/default/classes/apps

    [
      {
        "artifact": {
          "name": "cdap-data-pipeline",
          "scope": "SYSTEM",
          "version": "|release|"
        },
        "className": "co.cask.cdap.datapipeline.DataPipelineApp"
      },
      {
        "artifact": {
          "name": "cdap-etl-realtime",
          "scope": "SYSTEM",
          "version": "|release|"
        },
        "className": "co.cask.cdap.etl.realtime.ETLRealtimeApplication"
      },
      {
        "artifact": {
          "name": "Purchase",
          "scope": "USER",
          "version": "|release|"
        },
        "className": "co.cask.cdap.examples.purchase.PurchaseApp"
      },
    ]

.. _http-restful-api-artifact-appclass-detail:

Retrieve Application Class Details
==================================
To retrieve details about a specific application class, submit an HTTP GET request::

  GET /v3/namespaces/<namespace-id>/classes/apps/<class-name>[?scope=<scope>]

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``namespace-id``
     - Namespace ID
   * - ``class-name``
     - Application class name
   * - ``scope``
     - Optional scope filter. If not specified, defaults to ``user``.

This will return a JSON array that lists each application class with that class name.
Each element in the array is a JSON object that contains details about the application
class, including the artifact the class is from, the class name, and the schema of
the config supported by the application class.
Example output for the ``WordCount`` application (pretty-printed and reformatted to fit):

.. container:: highlight

  .. parsed-literal::
    |$| GET /v3/namespaces/default/classes/apps/co.cask.cdap.examples.wordcount.WordCount
    [
      {
        "artifact": {
          "name": "WordCount",
          "scope": "USER",
          "version": "|version|"
        },
        "className": "co.cask.cdap.examples.wordcount.WordCount",
        "configSchema": {
          "fields": [
            { "name": "stream", "type": [ "string", "null" ] },
            { "name": "uniqueCountTable", "type": [ "string", "null" ] },
            { "name": "wordAssocTable", "type": [ "string", "null" ] },
            { "name": "wordCountTable", "type": [ "string", "null" ] },
            { "name": "wordStatsTable", "type": [ "string", "null" ] },
          ],
          "name": "co.cask.cdap.examples.wordcount.WordCount$WordCountConfig",
          "type": "record"
        }
      }
    ]
