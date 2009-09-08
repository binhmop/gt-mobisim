// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.event;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.gui.SimPanel;

import java.io.DataOutputStream;
import java.io.IOException;

public class DrawGUIEvent extends SimEvent {
  private SimPanel panel;
  private double period; // [sec]

  public DrawGUIEvent(Simulation sim, long timestamp, SimPanel panel, double period) {
    super(sim, timestamp);
    this.panel = panel;
    this.period = period;
  }

  public void saveTo(DataOutputStream out) throws IOException {
    // non-persistent
  }

  public int getPriority() {
    return Simulation.priorityDrawGUIEvent;
  }

  public void execute() {
    panel.redrawSim();
    panel.repaint();

    // schedule next snapshot:
    sim.addEvent(new DrawGUIEvent(sim, timestamp + (long) (1000 * period), panel, period));

  }

  public byte getTypeCode() {
    return '\0'; // non-persistent
  }

  public String toString() {
    return null;
  }
}
