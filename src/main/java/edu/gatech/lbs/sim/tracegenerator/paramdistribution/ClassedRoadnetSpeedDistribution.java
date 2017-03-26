// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.paramdistribution;

import java.util.Random;

import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.roadnet.ClassedRoadSegment;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;

public class ClassedRoadnetSpeedDistribution implements IParamDistribution {
  public static final String xmlName = "roadnet";

  private IParamDistribution[] speedDistributions;
  private Random rnd;

  public ClassedRoadnetSpeedDistribution(IParamDistribution[] speedDistributions) {
    this.speedDistributions = speedDistributions;

    rnd = new Random();
  }

  public int getNextValue(IVector location) {
    RoadnetVector roadnetLocation = (RoadnetVector) location;
    ClassedRoadSegment roadsegment = (ClassedRoadSegment) roadnetLocation.getRoadSegment();

    int roadClass = roadsegment.getRoadClassIndex();

    int speed;
    do {
      speed = speedDistributions[roadClass].getNextValue(location);
    } while (speed <= 0 || speed > roadsegment.getSpeedLimit());

    // if segment is not directed, randomly reverse direction when inside a segment, but force movement towards the middle when at segment ends:
    if (!roadnetLocation.getRoadSegment().isDirected() && ((roadnetLocation.getProgress() != 0 && rnd.nextBoolean()) || roadnetLocation.getProgress() == roadsegment.getLength())) {
      speed *= -1;
    }

    return speed;
  }

  public double getMaxValue(RoadSegment roadsegment) {
    int roadClass = ((ClassedRoadSegment) roadsegment).getRoadClassIndex();

    return ((UniformParamDistribution) speedDistributions[roadClass]).getMaxValue();
  }
}
