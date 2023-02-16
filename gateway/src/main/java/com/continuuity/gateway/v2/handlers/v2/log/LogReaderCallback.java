package com.continuuity.gateway.v2.handlers.v2.log;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import com.continuuity.common.http.core.HttpResponder;
import com.continuuity.logging.read.Callback;
import com.continuuity.logging.read.LogEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback to handle log events from LogReader.
 */
class LogReaderCallback implements Callback {
  private final JsonArray logResults;
  private final HttpResponder responder;
  private final PatternLayout patternLayout;

  LogReaderCallback(HttpResponder responder, String logPattern) {
    this.logResults = new JsonArray();
    this.responder = responder;

    ch.qos.logback.classic.Logger rootLogger =
      (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    LoggerContext loggerContext = rootLogger.getLoggerContext();

    this.patternLayout = new PatternLayout();
    this.patternLayout.setContext(loggerContext);
    this.patternLayout.setPattern(logPattern);
  }

  @Override
  public void init() {
    patternLayout.start();
  }

  @Override
  public void handle(LogEvent event) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("log", patternLayout.doLayout(event.getLoggingEvent()));
    jsonObject.addProperty("offset", event.getOffset());
    logResults.add(jsonObject);
  }

  @Override
  public void close() {
    patternLayout.stop();
    responder.sendJson(HttpResponseStatus.OK, logResults);
  }
}
