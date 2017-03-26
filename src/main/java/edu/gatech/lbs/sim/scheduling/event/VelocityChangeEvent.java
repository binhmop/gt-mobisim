// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.vector.IVectorFactory;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;

public class VelocityChangeEvent extends SimEvent implements IMobilityChangeEvent {
  public static final byte typeCode = 'v';

  protected SimAgent agent;
  protected IVector location;
  protected IVector velocity;

  public VelocityChangeEvent(Simulation sim, long timestamp, SimAgent agent, IVector location, IVector velocity) {
    super(sim, timestamp);
    this.agent = agent;
    this.location = location;
    this.velocity = velocity;
  }

  public VelocityChangeEvent(Simulation sim, DataInputStream in) throws IOException {
    super(sim, in);
    int simAgentId = in.readInt();
    agent = sim.getAgent(simAgentId);
    location = IVectorFactory.load(in, sim.getWorld());
    velocity = IVectorFactory.load(in, sim.getWorld());
  }

  public void saveTo(DataOutputStream out) throws IOException {
    out.writeByte(getTypeCode());
    out.writeLong(timestamp);
    out.writeInt(agent.getSimAgentId());
    location.saveTo(out);
    velocity.saveTo(out);
  }

  public int getPriority() {
    return Simulation.priorityVelocityChangeEvent;
  }

  public void execute() {
    sim.updateAgentIndex(agent, location);
    agent.getSimPhysicalAttributes().setEvent(this);
  }

  public IVector getLocation() {
    return location;
  }

  public IVector getVelocity() {
    return velocity;
  }

  public IVector getAcceleration() {
    return null;
  }

  public byte getTypeCode() {
    return typeCode;
  }

  public String toString() {
    return "V";
    // return "[" + (char) getTypeCode() + "@" + timestamp + ", l=" + location + ",v=" + velocity + "]";
  }
}
