// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.examples;

import java.util.Collection;

import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.config.paramparser.TimeParser;

/**
 * An example program to demonstrate how to get the list of agents on a segment at a given time.
 *
 */
public class ExampleAgentsOnSegment {

  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage example:");
      System.out.println("  java " + ExampleAgentsOnSegment.class + " jnlp-demo.xml \"150 sec\" 4618");
      return;
    }

    String configFilename = args[0];
    long t = (long) (new TimeParser().parse(args[1]) * 1000); // [ms]
    int segmentId = Integer.parseInt(args[2]);

    Simulation sim = new Simulation();
    sim.loadConfiguration(configFilename);
    sim.initSimulation();

    sim.runSimulationTo(t);
    RoadSegment segment = ((RoadMap) sim.getWorld()).getRoadSegment(segmentId);
    Collection<SimAgent> agents = sim.getAgentsOnSegment(segment.getId());
    if (agents != null) {
      for (SimAgent agent : agents) {
        RoadnetVector l = agent.getLocation().toRoadnetVector();
        System.out.println("Agent #" + agent.getSimAgentId() + " on segment " + l.getRoadSegment().getId() + " at progress= " + l.getProgress());
      }
    }

    sim.endSimulation();
  }
}
