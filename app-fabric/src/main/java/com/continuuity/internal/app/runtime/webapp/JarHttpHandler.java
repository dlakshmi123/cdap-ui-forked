package com.continuuity.internal.app.runtime.webapp;

import com.continuuity.common.http.core.HttpHandler;

/**
 * Interface to serve files from a jar.
 */
public interface JarHttpHandler extends HttpHandler {
  String getServePath(String hostHeader, String uri);
}
