.. meta::
    :author: Cask Data, Inc.
    :copyright: Copyright © 2015 Cask Data, Inc.

.. _apptemplates-third-party:

======================
Using Third-party Jars 
======================

.. highlight:: json  

**Prebuilt JARs:** In a case where you'd like to use prebuilt third-party JARs (such as a
JDBC Driver) as a plugin, you will need to create a JSON file to describe the JAR.

For information on the format of the JSON, please refer to the section on the
:ref:`Configuration File Format <apptemplates-etl-configuration-file-format>` on the page
on :ref:`Creating an ETL Adapter <apptemplates-etl-creating>`.

Copy the JAR and the JSON file to the :ref:`Plugin directory
<advanced-custom-app-template-installation-directory>` and then update the Template by
using the :ref:`HTTP RESTful API Application Template Update
<http-restful-api-apptemplates-update>` endpoint.

A sample JDBC Driver Plugin configuration::

  [
    {
      "type" : "JDBC",
      "name" : "MySQL JDBC",
      "description" : "Plugin for MySQL JDBC driver",
      "className" : "com.mysql.jdbc.Driver",
    },
    {
      "type" : "JDBC",
      "name" : "PostgreSQL JDBC",
      "description" : "Plugin for PostgreSQL JDBC driver",
      "className" : "org.postgresql.Driver",
    }
  ]
