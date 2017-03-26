// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet.parser;

import java.util.HashMap;
import java.util.List;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.world.roadnet.RoadJunction;
import edu.gatech.lbs.core.world.roadnet.RoadMap;

public abstract class MapParser {
  protected HashMap<CartesianVector, RoadJunction> junctionMap;

  public MapParser() {
    junctionMap = new HashMap<CartesianVector, RoadJunction>();
  }

  protected RoadJunction[] getJunctions(List<CartesianVector> points) {
    RoadJunction[] junctions = new RoadJunction[2];
    for (int i = 0; i < 2; i++) {
      // determine location of junction:
      CartesianVector junctionLocation;
      if (i == 0) {
        junctionLocation = points.get(0);
      } else {
        junctionLocation = points.get(points.size() - 1);
      }

      // retrieve junction from map, or create anew:
      if (junctionMap.containsKey(junctionLocation)) {
        junctions[i] = junctionMap.get(junctionLocation);
      } else {
        junctions[i] = new RoadJunction(junctionMap.size());
        junctionMap.put(junctionLocation, junctions[i]);
      }
    }

    return junctions;
  }

  public abstract void load(String roadmapFilename, RoadMap roadmap);
}
