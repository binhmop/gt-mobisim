// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.event;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;

public class PeriodicTraceSaveEvent extends SimEvent {
  protected DataOutputStream out;
  protected int period; // [ms]

  public PeriodicTraceSaveEvent(Simulation sim, long timestamp, int period, DataOutputStream out) {
    super(sim, timestamp);
    this.out = out;
    this.period = period;
  }

  public void saveTo(DataOutputStream out) throws IOException {
    // non-persistent
  }

  public int getPriority() {
    return Simulation.priorityPeriodicTraceSaveEvent;
  }

  public void execute() {
    try {
      // save locations of all agents:
      Collection<SimAgent> agents = sim.getAgents();
      for (SimAgent agent : agents) {
        // create a non-roadnetwork location record:
        SimEvent e = new LocationChangeEvent(sim, timestamp, agent, agent.getLocation().toCartesianVector());
        e.saveTo(out);
      }

      // schedule next snapshot:
      sim.addEvent(new PeriodicTraceSaveEvent(sim, timestamp + period, period, out));
    } catch (IOException e) {
      System.out.println("Unable to write trace file.");
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
