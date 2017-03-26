// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.agent;

import edu.gatech.lbs.core.query.LocationBasedQuery;
import edu.gatech.lbs.core.query.QueryKey;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.sim.Simulation;

public class SimAgent implements IPhysicalAttributes {
  protected int simAgentId;

  // Simulated physical attributes (eg. location) of the agent.
  protected SimPhysicalAttributes physicalAttributes;

  public SimAgent(Simulation sim, int simAgentId) {
    this.simAgentId = simAgentId;
    this.physicalAttributes = new SimPhysicalAttributes(sim);
  }

  public int getSimAgentId() {
    return simAgentId;
  }

  public IVector getLocation() {
    return physicalAttributes == null ? null : physicalAttributes.getLocation();
  }

  public IVector getVelocity() {
    return physicalAttributes == null ? null : physicalAttributes.getVelocity();
  }

  public IVector getAcceleration() {
    return physicalAttributes == null ? null : physicalAttributes.getAcceleration();
  }

  public SimPhysicalAttributes getSimPhysicalAttributes() {
    return physicalAttributes;
  }

  public void simulateAddQuery(QueryKey simKey, LocationBasedQuery query) {
    // do nothing
  }

  public void simulateRemoveQuery(QueryKey simKey) {
    // do nothing
  }
}
