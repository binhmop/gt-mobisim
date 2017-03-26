// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.event;

import edu.gatech.lbs.sim.Simulation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class SimEvent {
  protected Simulation sim;
  protected long timestamp; // [ms]

  public SimEvent(Simulation sim, long timestamp) {
    this.sim = sim;
    this.timestamp = timestamp;
  }

  public SimEvent(Simulation sim, DataInputStream in) throws IOException {
    this.sim = sim;
    timestamp = in.readLong();
  }

  public boolean isBefore(long now) {
    return timestamp < now;
  }

  public boolean isAfter(long now) {
    return timestamp > now;
  }

  public long getTimestamp() {
    return timestamp;
  }

  /*
   * Priority number for ordering among events with the same timestamp.
   * Lower priority number means higher priority.
   */
  public abstract int getPriority();

  public abstract void execute();

  public abstract void saveTo(DataOutputStream out) throws IOException;

  public abstract byte getTypeCode();

  public abstract String toString();

}
