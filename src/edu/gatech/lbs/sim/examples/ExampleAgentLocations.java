// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.examples;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.config.paramparser.TimeParser;

/**
 * An example program to demonstrate how to get the locations of agents at a given time.
 *
 */
public class ExampleAgentLocations {

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage example:");
      System.out.println("  java " + ExampleAgentLocations.class + " jnlp-demo.xml \"2.5 min\"");
      return;
    }

    String configFilename = args[0];
    long t = (long) (new TimeParser().parse(args[1]) * 1000);

    Simulation sim = new Simulation();
    sim.loadConfiguration(configFilename);
    sim.initSimulation();

    sim.runSimulationTo(t);
    for (SimAgent agent : sim.getAgents()) {
      IVector vector = agent.getLocation();
      CartesianVector vec1 = vector.toCartesianVector();
      RoadnetVector vec2 = vector.toRoadnetVector();

      System.out.println("Agent #" + agent.getSimAgentId() + ":");
      System.out.println("  X= " + vec1.getX() + " m, Y= " + vec1.getY() + " m");
      System.out.println("  segment #" + vec2.getRoadSegment().getId() + ", progress= " + vec2.getProgress() + " m");
    }

    sim.endSimulation();
  }
}
