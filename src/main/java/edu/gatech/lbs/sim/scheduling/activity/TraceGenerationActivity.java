// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.activity;

import java.io.IOException;

import edu.gatech.lbs.core.FileHelper;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.tracegenerator.ITraceGenerator;

public class TraceGenerationActivity implements ISimActivity {
  protected String traceFilename;
  protected ITraceGenerator traceGenerator;
  protected boolean isOverwriteAllowed;

  public TraceGenerationActivity(String traceFilename, ITraceGenerator traceGenerator, boolean isOverwriteAllowed) {
    this.traceFilename = traceFilename;
    this.traceGenerator = traceGenerator;
    this.isOverwriteAllowed = isOverwriteAllowed;
  }

  public void scheduleOn(Simulation sim) {
    if (!isOverwriteAllowed && FileHelper.isNonEmptyFileOrUrl(traceFilename)) {
      return;
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
