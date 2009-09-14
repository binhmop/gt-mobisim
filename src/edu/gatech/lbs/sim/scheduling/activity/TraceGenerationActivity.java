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
    // only generate trace, if it doesn't exist:
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

    System.out.println(" Generating trace '" + traceFilename + "'... ");
    try {
      traceGenerator.generateTrace(traceFilename);
      // allow garbage collection of generation setup:
      traceGenerator = null;
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
