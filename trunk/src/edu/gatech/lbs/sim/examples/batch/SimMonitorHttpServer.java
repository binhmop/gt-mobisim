// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.examples.batch;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import edu.gatech.lbs.core.logging.Logz;
import edu.gatech.lbs.core.logging.Varz;

public class SimMonitorHttpServer implements HttpHandler {
  public static final int portNum = 5656;
  public static final String contextStr = "/sim";

  public BatchSimRunner batchRunner;

  public void handle(HttpExchange t) throws IOException {
    // InputStream is = t.getRequestBody();

    SimpleDateFormat dateformat = new SimpleDateFormat("D 'days,' H 'hours,' m 'minutes,' s 'seconds'");
    String response = Varz.getString("configFilename") + ", " + dateformat.format(new Date(batchRunner.getRunTime())) + "\n\n" + Logz.outLog;

    t.sendResponseHeaders(200, response.length());
    OutputStream out = t.getResponseBody();
    out.write(response.getBytes());
    out.close();
  }
}
