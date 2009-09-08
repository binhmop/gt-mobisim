package edu.gatech.lbs.sim.scheduling.activity;

import java.io.IOException;
import java.io.InputStream;

import edu.gatech.lbs.core.FileHelper;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.tracegenerator.ITraceGenerator;

public class TraceGenerationActivity implements ISimActivity {
  protected String traceFilename;
  protected ITraceGenerator traceGenerator;

  public TraceGenerationActivity(String traceFilename, ITraceGenerator traceGenerator) {
    this.traceFilename = traceFilename;
    this.traceGenerator = traceGenerator;
  }

  public void scheduleOn(Simulation sim) {
    try {
      InputStream in = FileHelper.openFileOrUrl(traceFilename);
      int b = in.read();
      in.close();
      if (b != -1) {
        return;
      }
    } catch (IOException e) {
      // read failure indicates we need to go forward with the trace generation
    }
    /*
    File f = new File(traceFilename);
    if (f.exists() && f.length() > 0) {
      return;
    }*/

    System.out.println(" Generating trace '" + traceFilename + "'... ");
    try {
      traceGenerator.generateTrace(traceFilename);
    } catch (IOException e) {
      System.out.println(" failed.");
      System.exit(-1);
    }
    System.out.println(" done.");
  }

  public void cleanup() {
    // do nothing
  }
}
