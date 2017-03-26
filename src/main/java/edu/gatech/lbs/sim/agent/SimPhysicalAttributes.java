// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.agent;

import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.scheduling.event.IMobilityChangeEvent;

public class SimPhysicalAttributes implements IPhysicalAttributes {
  protected IMobilityChangeEvent event;
  protected Simulation sim;

  public SimPhysicalAttributes(Simulation sim) {
    this.sim = sim;
  }

  public void setEvent(IMobilityChangeEvent event) {
    this.event = event;
  }

  public IVector getLocation() {
    if (event == null) {
      return null;
    }

    double dt = (sim.getTime() - event.getTimestamp()) / 1000.0;

    // s(t)= s0 + v0*t + a0/2*(t0^2)
    IVector s0 = event.getLocation();
    IVector v0 = event.getVelocity();
    IVector a0 = event.getAcceleration();
    IVector s = null;
    if (s0 != null) {
      s = s0.clone();
      if (v0 != null) {
        s.add(v0.clone().times(dt));
        if (a0 != null) {
          s.add(a0.clone().times(dt * dt / 2));
        }
      }
    }
    return s;
  }

  public IVector getVelocity() {
    if (event == null) {
      return null;
    }

    double dt = (sim.getTime() - event.getTimestamp()) / 1000.0;

    // v(t)= v0 + a0*t
    IVector v0 = event.getVelocity();
    IVector a0 = event.getAcceleration();
    IVector v = null;
    if (v0 != null) {
      v = v0.clone();
      if (a0 != null) {
        v.add(a0.clone().times(dt));
      }
    }
    return v;
  }

  public IVector getAcceleration() {
    if (event == null) {
      return null;
    }

    IVector a0 = event.getAcceleration();
    IVector a = null;
    if (a0 != null) {
      a = a0.clone();
    }
    return a;
  }
}
