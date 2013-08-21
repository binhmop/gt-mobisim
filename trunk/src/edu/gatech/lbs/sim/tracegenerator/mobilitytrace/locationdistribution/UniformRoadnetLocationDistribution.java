// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.roadnet.ClassedRoadSegment;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;

/**
 * Generate locations that are uniformly distributed over every km of road in each road class
 * (if doLengthWeighting=true; otherwise shorter segments will have more clients/km),
 * but the objects/km distribution across road classes is different
 * (if classWeight!=null; otherwise there is no road class).
 * 
 * @author Peter Pesti
 *
 */
public class UniformRoadnetLocationDistribution implements ILocationDistribution {
  public static final String xmlName = "uniformroadnet";

  private Collection<RoadSegment> segments;
  private double[] classWeight; // only the relative sizes of the weights matter; they needn't add up to 1
  private double totalWeights;
  private boolean doLengthWeighting;
  private Random rnd;

  public UniformRoadnetLocationDistribution(RoadMap roadmap, double[] classWeight, boolean doLengthWeighting) {
    segments = roadmap.getRoadSegments();

    this.classWeight = classWeight;
    this.doLengthWeighting = doLengthWeighting;

    totalWeights = 0;
    for (RoadSegment seg : segments) {
      totalWeights += getSegmentWeight(seg);
    }

    rnd = new Random();
  }

  private double getSegmentWeight(RoadSegment seg) {
    return (doLengthWeighting ? seg.getLength() : 1) * (classWeight == null ? 1 : classWeight[((ClassedRoadSegment) seg).getRoadClassIndex()]);
  }

  public IVector getNextLocation() {
    double watermark = rnd.nextDouble() * totalWeights;

    RoadSegment seg = null;
    double water = 0;
    for (Iterator<RoadSegment> it = segments.iterator(); water <= watermark && it.hasNext(); water += getSegmentWeight(seg)) {
      seg = it.next();
    }

    return new RoadnetVector(seg, (int) (rnd.nextDouble() * seg.getLength()));
  }
}
