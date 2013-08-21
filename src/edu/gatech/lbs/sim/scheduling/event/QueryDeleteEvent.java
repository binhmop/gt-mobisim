// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.gatech.lbs.core.query.QueryKey;
import edu.gatech.lbs.sim.Simulation;

public class QueryDeleteEvent extends SimEvent {
  public static final byte typeCode = 'd';

  protected QueryKey simKey;

  public QueryDeleteEvent(Simulation sim, long timestamp, QueryKey simKey) {
    super(sim, timestamp);
    this.simKey = simKey;
  }

  public QueryDeleteEvent(Simulation sim, DataInputStream in) throws IOException {
    super(sim, in);
    simKey = new QueryKey(in);
  }

  public void saveTo(DataOutputStream out) throws IOException {
    out.writeByte(getTypeCode());
    out.writeLong(timestamp);
    simKey.saveTo(out);
  }

  public int getPriority() {
    return Simulation.priorityQueryRemoveEvent;
  }

  public void execute() {
    sim.simulateRemoveQuery(simKey);
  }

  public int getSimAgentId() {
    return simKey.uid;
  }

  public byte getTypeCode() {
    return typeCode;
  }

  public String toString() {
    return "[" + (char) getTypeCode() + "@" + timestamp + ", simKey=" + simKey + "]";
  }
}
