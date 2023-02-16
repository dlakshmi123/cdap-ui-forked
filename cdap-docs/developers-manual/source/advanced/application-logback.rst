.. meta::
    :author: Cask Data, Inc.
    :copyright: Copyright © 2015 Cask Data, Inc.

.. _application-logback:

===================
Application Logback
===================

.. highlight:: xml

YARN containers launched by a CDAP application use a default container logback file
|---| ``logback-container.xml`` |---| packaged with CDAP and installed in 
the CDAP :ref:`configuration directory <admin-manual-cdap-components>`. This logback does
log rotation once every day at midnight and deletes logs older than 14 days. Depending on
the use case, the default configuration may be sufficient.

However, you can specify a custom ``logback.xml`` for a CDAP application by packaging
it with the application in the application's ``src/main/resources`` directory.
The packaged ``logback.xml`` is then used for each container launched by the application.

To write a custom logback, refer to `Logback <http://logback.qos.ch/>`__ for information.

**Note:** When a custom ``logback.xml`` is specified for an application, the custom
``logback.xml`` will be used in place of the ``logback-container.xml``. A custom
``logback.xml`` needs to be configured for log rotation (``rollingPolicy``) and log
clean-up (``maxHistory``) to ensure that long-running containers don't fill up the disk.
