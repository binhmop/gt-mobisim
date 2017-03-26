// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.examples.batch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import edu.gatech.lbs.core.logging.Logz;
import edu.gatech.lbs.core.logging.Varz;

public class SimMonitorHttpServer implements HttpHandler {
  public static final int portNum = 5656;
  public static final String contextStr = "/sim";

  public BatchSimRunner batchRunner;
  public HttpServer server;

  public void handle(HttpExchange t) throws IOException {
    // InputStream is = t.getRequestBody();

    Calendar cal = new GregorianCalendar();
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    cal.setTimeInMillis(batchRunner.getRunTime());
    String response = Varz.getString("configFilename") + "\n";
    response += "Runtime: " + (cal.get(Calendar.DATE) - 1) + " d, " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + "\n\n" + Logz.outLog;

    t.sendResponseHeaders(200, response.length());
    OutputStream out = t.getResponseBody();
    out.write(response.getBytes());
    out.close();
  }
}
