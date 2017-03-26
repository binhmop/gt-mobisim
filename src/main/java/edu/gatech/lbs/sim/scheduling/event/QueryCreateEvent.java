// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.gatech.lbs.core.query.LocationBasedQuery;
import edu.gatech.lbs.core.query.LocationBasedQueryFactory;
import edu.gatech.lbs.core.query.QueryKey;
import edu.gatech.lbs.sim.Simulation;

public class QueryCreateEvent extends SimEvent {
  public static final byte typeCode = 'c';

  protected QueryKey simKey;
  protected LocationBasedQuery query;

  public QueryCreateEvent(Simulation sim, long timestamp, QueryKey simKey, LocationBasedQuery query) {
    super(sim, timestamp);
    this.simKey = simKey;
    this.query = query;
    this.sim = sim;
  }

  public QueryCreateEvent(Simulation sim, DataInputStream in) throws IOException {
    super(sim, in);
    simKey = new QueryKey(in);
    query = LocationBasedQueryFactory.load(in);
  }

  public void saveTo(DataOutputStream out) throws IOException {
    out.writeByte(getTypeCode());
    out.writeLong(timestamp);
    simKey.saveTo(out);
    query.saveTo(out);
  }

  public int getPriority() {
    return Simulation.priorityQueryCreateEvent;
  }

  public void execute() {
    sim.simulateAddQuery(simKey, query);
  }

  public byte getTypeCode() {
    return typeCode;
  }

  public String toString() {
    return "[" + (char) getTypeCode() + "@" + timestamp + ", simKey=" + simKey + "]";
  }
}
