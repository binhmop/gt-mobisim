// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.activity;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.scheduling.event.PeriodicTraceSaveEvent;

public class PeriodicLocationTraceSavingActivity implements ISimActivity {
  protected String traceFilename;
  protected DataOutputStream traceOutStream;
  protected int period; // [ms]

  public PeriodicLocationTraceSavingActivity(String traceFilename, int period) {
    this.traceFilename = traceFilename;
    this.period = period;
  }

  public void scheduleOn(Simulation sim) {
    if (traceFilename != null) {
      System.out.print(" Opening '" + traceFilename + "' for trace writing... ");
      try {
        traceOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(traceFilename)));
        sim.addEvent(new PeriodicTraceSaveEvent(sim, sim.getSimStartTime(), period, traceOutStream));
        System.out.println("done.");
      } catch (IOException e) {
        System.out.println("failed.");
        System.exit(-1);
      }
    }
  }

  public void cleanup() {
    try {
      if (traceOutStream != null) {
        traceOutStream.close();
      }
    } catch (IOException e) {
      System.out.println("Failed to close file '" + traceFilename + "'.");
    }
  }
}
