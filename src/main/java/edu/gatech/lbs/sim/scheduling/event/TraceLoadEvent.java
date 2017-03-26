// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.gatech.lbs.sim.Simulation;

public class TraceLoadEvent extends SimEvent {
  protected DataInputStream in;

  public TraceLoadEvent(Simulation sim, long timestamp, DataInputStream in) {
    super(sim, timestamp);
    this.in = in;
  }

  public void saveTo(DataOutputStream out) throws IOException {
    // non-persistent
  }

  public int getPriority() {
    return Simulation.priorityTraceLoadEvent;
  }

  public void execute() {
    try {
      sim.getQueue().loadSome(sim, in);
    } catch (IOException e) {
      System.out.println("Unable to read trace file.");
      System.exit(-1);
    }
  }

  public byte getTypeCode() {
    return '\0'; // non-persistent
  }

  public String toString() {
    return null;
  }
}
