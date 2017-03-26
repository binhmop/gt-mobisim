// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.activity;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import edu.gatech.lbs.core.FileHelper;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.scheduling.event.TraceLoadEvent;

public class TraceLoadingActivity implements ISimActivity {
  protected String traceFilename;
  protected DataInputStream traceInStream;

  public TraceLoadingActivity(String traceFilename) {
    this.traceFilename = traceFilename;
  }

  public void scheduleOn(Simulation sim) {
    if (traceFilename != null) {
      System.out.print(" Opening '" + traceFilename + "' for trace loading... ");
      try {
        traceInStream = new DataInputStream(new BufferedInputStream(FileHelper.openFileOrUrl(traceFilename)));
        sim.addEvent(new TraceLoadEvent(sim, sim.getSimStartTime(), traceInStream));
        System.out.println("done.");
      } catch (IOException e) {
        System.out.println("failed.");
        System.exit(-1);
      }
    }
  }

  public void cleanup() {
    try {
      if (traceInStream != null) {
        traceInStream.close();
      }
    } catch (IOException e) {
      System.out.println("Failed to close file '" + traceFilename + "'.");
    }
  }
}
