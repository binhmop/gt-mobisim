// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.examples;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.config.paramparser.TimeParser;

/**
 * An example program to demonstrate how to get the location & velocity of an agent
 * at periodic time intervals.
 */
public class ExamplePeriodicLocations {

  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage example:");
      System.out.println("  java " + ExamplePeriodicLocations.class.getName() + " web-demo.xml \"30 sec\" 0");
      return;
    }

    String configFilename = args[0];
    long period = new TimeParser().parse(args[1]); // [ms]
    int simAgentId = Integer.parseInt(args[2]);

    Simulation sim = new Simulation();
    sim.loadConfiguration(configFilename);
    sim.initSimulation();

    for (long t = sim.getSimStartTime(); t < sim.getSimEndTime(); t += period) {
      sim.runSimulationTo(t);
      SimAgent agent = sim.getAgent(simAgentId);

      System.out.println("Agent #" + agent.getSimAgentId() + " at t= " + t + " ms:");
      System.out.println("  location vector: " + agent.getLocation().toCartesianVector());
      System.out.println("  velocity vector: "
          + agent.getLocation().toRoadnetVector().toTangentVector()
              .times(1e-6 * agent.getVelocity().getLength()).toCartesianVector());

    }

    sim.endSimulation();
  }
}