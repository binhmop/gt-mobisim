// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.roadnet.RoadJunctionDistance;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;

public class HotspotRoadnetLocationDistribution implements ILocationDistribution {
  public static final String xmlName = "hotspotroadnet";

  private RoadSegment[] segments;
  private double[] segmentWeights;
  private double totalWeights;
  private Random rnd;

  public HotspotRoadnetLocationDistribution(RoadMap roadmap, int hotspotCount, double coeff, long seed) {
    Collection<RoadSegment> segments0 = roadmap.getRoadSegments();
    segments = new RoadSegment[roadmap.getRoadSegmentCount()];
    int r = 0;
    for (RoadSegment roadSegment : segments0) {
      segments[r] = roadSegment;
      r++;
    }
    int segmentCount = segments.length;
    segmentWeights = new double[segmentCount];

    // create random-generator with given seed, if given:
    rnd = seed < 0 ? new Random() : new Random(seed);

    // System.out.print("Calculating hotspottyness of segments... ");
    for (int i = 0; i < hotspotCount; i++) {
      // select a hotspot center:
      int centerNum = (int) Math.floor(rnd.nextDouble() * segmentCount);
      RoadSegment centerSeg = segments[centerNum];
      RoadnetVector source = new RoadnetVector(centerSeg, centerSeg.getLength() / 2);

      // calculate hotspottyness of all segments:
      HashMap<Integer, RoadJunctionDistance> junctionDist = new HashMap<Integer, RoadJunctionDistance>();
      roadmap.getSpanningTree(source, junctionDist, null);
      long lengthAvg = roadmap.getLengthTotal() / roadmap.getRoadSegments().size();
      for (int j = 0; j < segmentCount; j++) {
        RoadSegment seg = segments[j];
        int dist = (j == centerNum) ? 0 : Math.min(junctionDist.get(seg.getSourceJunction().getId()).distance, junctionDist.get(seg.getTargetJunction().getId()).distance) + seg.getLength() / 2;
        segmentWeights[j] += Math.pow(coeff, dist / (double) lengthAvg);
      }
    }
    // System.out.println("done.");

    totalWeights = 0;
    for (int i = 0; i < segmentCount; i++) {
      totalWeights += segmentWeights[i];
    }
  }

  public IVector getNextLocation() {
    // find segment interval in total that corresponds to a segment:
    double watermark = rnd.nextDouble() * totalWeights;

    RoadSegment seg = null;
    double water = 0;
    for (int i = 0; water <= watermark && i < segments.length; water += segmentWeights[i], i++) {
      seg = segments[i];
    }

    return new RoadnetVector(seg, (int) (rnd.nextDouble() * seg.getLength()));
  }
}
