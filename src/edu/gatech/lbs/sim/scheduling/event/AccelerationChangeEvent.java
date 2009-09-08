// Copyright (c) 2009, Georgia Tech Research Corporation
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

public class AccelerationChangeEvent extends SimEvent implements IMobilityChangeEvent {
  public static final byte typeCode = 'a';

  private Simulation sim;
  private SimAgent agent;
  private IVector location;
  private IVector velocity;
  private IVector acceleration;

  public AccelerationChangeEvent(Simulation sim, long timestamp, SimAgent agent, IVector location, IVector velocity, IVector acceleration) {
    super(sim, timestamp);
    this.agent = agent;
    this.location = location;
    this.velocity = velocity;
    this.acceleration = acceleration;
  }

  public AccelerationChangeEvent(Simulation sim, DataInputStream in) throws IOException {
    super(sim, in);
    int simAgentId = in.readInt();
    agent = sim.getAgent(simAgentId);
    location = IVectorFactory.load(in, sim.getWorld());
    velocity = IVectorFactory.load(in, sim.getWorld());
    acceleration = IVectorFactory.load(in, sim.getWorld());
  }

  public void saveTo(DataOutputStream out) throws IOException {
    out.writeByte(getTypeCode());
    out.writeLong(timestamp);
    out.writeInt(agent.getSimAgentId());
    location.saveTo(out);
    velocity.saveTo(out);
    acceleration.saveTo(out);
  }

  public int getPriority() {
    return Simulation.priorityAccelerationChangeEvent;
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
    return acceleration;
  }

  public byte getTypeCode() {
    return typeCode;
  }

  public String toString() {
    return "A";
    // return "[" + (char) getTypeCode() + "@" + timestamp + ", l=" + location + ",v=" + velocity + ",a=" + acceleration + "]";
  }
}
