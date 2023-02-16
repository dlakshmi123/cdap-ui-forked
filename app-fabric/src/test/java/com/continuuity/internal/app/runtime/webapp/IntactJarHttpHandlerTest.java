package com.continuuity.internal.app.runtime.webapp;

import com.continuuity.common.http.core.BasicHandlerContext;
import com.continuuity.common.http.core.HttpResponder;
import com.continuuity.weave.filesystem.LocalLocationFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.net.URL;

/**
 * Test IntactJarHttpHandler class.
 */
public class IntactJarHttpHandlerTest extends JarHttpHandlerTestBase {
  private static IntactJarHttpHandler jarHttpHandler;

  @BeforeClass
  public static void init() throws Exception {
    URL jarUrl = IntactJarHttpHandlerTest.class.getResource("/CountRandomWebapp-localhost.jar");
    Assert.assertNotNull(jarUrl);

    jarHttpHandler = new IntactJarHttpHandler(new LocalLocationFactory().create(jarUrl.toURI()));
    jarHttpHandler.init(new BasicHandlerContext(null));
  }

  @AfterClass
  public static void destroy() throws Exception {
    jarHttpHandler.destroy(new BasicHandlerContext(null));
  }

  @Override
  protected void serve(HttpRequest request, HttpResponder responder) {
    String servePath = jarHttpHandler.getServePath(request.getHeader("Host"), request.getUri());
    request.setUri(servePath);
    jarHttpHandler.serve(request, responder);
  }
}
