// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet;

import java.util.ArrayList;
import java.util.Collection;

public class ClassedRoadMap extends RoadMap {

  protected String[] roadClassNames = null; // name of each road class
  protected int[] speedLimits; // speed limit for each road class [mm/s]

  public ClassedRoadMap(String[] roadClassNames, int[] speedLimits) {
    super(false);

    this.roadClassNames = roadClassNames;
    this.speedLimits = speedLimits;
  }

  public Collection<RoadSegment> getClassedRoadSegments(int classIdx) {
    Collection<RoadSegment> ret = new ArrayList<RoadSegment>();

    for (RoadSegment seg : segments.values()) {
      ClassedRoadSegment segment = (ClassedRoadSegment) seg;
      if (segment.getRoadClassIndex() == classIdx) {
        ret.add(segment);
      }
    }
    return ret;
  }

  public int getNumberOfRoadClasses() {
    return roadClassNames.length;
  }

  public String getRoadClassName(int index) {
    return roadClassNames[index];
  }

  public int getSpeedLimit(int index) {
    return speedLimits[index];
  }

  public int getRoadClassIndex(String str) {
    for (int i = 0; i < roadClassNames.length; i++) {
      if (str.indexOf(roadClassNames[i]) != -1) {
        return i;
      }
    }
    return -1;
  }

  public void showStats() {
    super.showStats();
    for (int i = 0; i < getNumberOfRoadClasses(); i++) {
      System.out.println(" " + roadClassNames[i]);
      showSegmentStats(getClassedRoadSegments(i));
    }
  }
}
