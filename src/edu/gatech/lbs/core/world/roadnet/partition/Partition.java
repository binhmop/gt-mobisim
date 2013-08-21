// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet.partition;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.roadnet.RoadJunction;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.core.world.roadnet.route.Route;

public class Partition {
  protected int partitionId;

  protected List<RoadSegment> segments;
  protected HashMap<Integer, Integer> segmentMap; // segmentID --> segment-in-partition-ID mapping

  protected List<RoadJunction> junctions;
  protected HashMap<Integer, Integer> junctionMap; // junctionID --> junction-in-partition-ID mapping
  protected boolean[] isBorderPoint; // border-points
  protected int[][] d; // node-to-node distance table
  protected List<Integer>[][] minPath; // node-to-node shortest paths
  protected List<Boolean>[][] direction; // node-to-node shortest paths' directionality

  protected List<RoadnetVector> borderLocations; // derived & cached locations of all border points

  public Partition(int partitionId) {
    this.partitionId = partitionId;
    segments = new ArrayList<RoadSegment>();
    segmentMap = new HashMap<Integer, Integer>();
    junctions = new ArrayList<RoadJunction>();
    junctionMap = new HashMap<Integer, Integer>();
  }

  public Partition(DataInputStream in, RoadMap map) throws IOException {
    this(in.readInt());

    int segmentCount = in.readInt();
    for (int i = 0; i < segmentCount; i++) {
      int id = in.readInt();
      addSegment(map.getRoadSegment(id));
    }

    int junctionCnt = in.readInt();
    assert (junctionCnt == junctions.size());
    isBorderPoint = new boolean[junctions.size()];
    for (int i = 0; i < junctions.size(); i++) {
      isBorderPoint[i] = in.readBoolean();
    }

    int n = junctions.size();
    d = new int[n][n];
    minPath = new ArrayList[n][n];
    direction = new ArrayList[n][n];
    for (int i = 0; i < d.length; i++) {
      for (int j = 0; j < d.length; j++) {
        d[i][j] = in.readInt();
        assert (d[i][j] >= 0);

        int pathLength = in.readInt();
        minPath[i][j] = new ArrayList<Integer>();
        direction[i][j] = new ArrayList<Boolean>();
        for (int k = 0; k < pathLength; k++) {
          int theInt = in.readInt();
          minPath[i][j].add(Math.abs(theInt));
          direction[i][j].add((theInt > 0));
        }
      }
    }

    // derive inside-partition border point locations:
    borderLocations = getBorderLocations();
  }

  public void saveTo(DataOutputStream out) throws IOException {
    out.writeInt(partitionId);
    out.writeInt(segments.size());
    for (Iterator<RoadSegment> it = segments.iterator(); it.hasNext();) {
      out.writeInt(it.next().getId());
    }
    out.writeInt(isBorderPoint.length);
    for (int i = 0; i < isBorderPoint.length; i++) {
      out.writeBoolean(isBorderPoint[i]);
    }
    for (int i = 0; i < d.length; i++) {
      for (int j = 0; j < d.length; j++) {
        out.writeInt(d[i][j]);

        out.writeInt(minPath[i][j].size());
        for (int k = 0; k < minPath[i][j].size(); k++) {
          out.writeInt(minPath[i][j].get(k) * (direction[i][j].get(k) ? 1 : -1));
        }
      }
    }
  }

  public int getId() {
    return partitionId;
  }

  public void addSegment(RoadSegment segment) {
    segmentMap.put(segment.getId(), segments.size());
    segments.add(segment);
    for (int i = 0; i < 2; i++) {
      RoadJunction junction = segment.getEndJunction(i);
      if (!junctionMap.containsKey(junction.getId())) {
        junctionMap.put(junction.getId(), junctions.size());
        junctions.add(junction);
      }
    }

    // set link to partition in segment:
    segment.setPartition(this);
  }

  public Collection<RoadSegment> getSegments() {
    return segments;
  }

  public void setBorderPoints(Collection<RoadJunction> B) {
    // mark borderpoints:
    isBorderPoint = new boolean[junctions.size()];
    for (int i = 0; i < isBorderPoint.length; i++) {
      isBorderPoint[i] = false;
    }

    // B might be empty, if the precinct is a complete (sub)graph
    for (RoadJunction roadJunction : B) {
      isBorderPoint[junctionMap.get(roadJunction.getId())] = true;
    }

    // precompute node-to-node distances:
    // initialize distances for Floyd-Warshall:
    int n = junctions.size();
    d = new int[n][n];
    minPath = new ArrayList[n][n];
    direction = new ArrayList[n][n];
    // set 0-length path for self-access:
    for (int i = 0; i < junctions.size(); i++) {
      for (int j = 0; j < junctions.size(); j++) {
        d[i][j] = Integer.MAX_VALUE;
        minPath[i][j] = new ArrayList<Integer>();
        direction[i][j] = new ArrayList<Boolean>();
      }
      d[i][i] = 0;
    }
    // set segment-length path for nodes connected by a segment:
    for (RoadSegment segment : segments) {
      int idx0 = junctionMap.get(segment.getEndJunction(0).getId());
      int idx1 = junctionMap.get(segment.getEndJunction(1).getId());
      d[idx0][idx1] = segment.getLength();
      minPath[idx0][idx1].add(segmentMap.get(segment.getId()));
      direction[idx0][idx1].add(true); // by definition, the segment's directionality is junction[0]->junction[1]
      if (!segment.isDirected()) {
        d[idx1][idx0] = segment.getLength();
        minPath[idx1][idx0].add(segmentMap.get(segment.getId()));
        direction[idx1][idx0].add(false);
      }
    }
    d = RoadMap.doFloydWarshall(d, minPath, direction);

    // derive inside-partition border point locations:
    borderLocations = getBorderLocations();
  }

  /**
   * Get the locations of all border points, in coordinates where all segmentIDs are within the partition.
   */
  private List<RoadnetVector> getBorderLocations() {
    List<RoadnetVector> junctionLocations = new ArrayList<RoadnetVector>();
    for (int i = 0; i < isBorderPoint.length; i++) {
      if (isBorderPoint[i]) {
        RoadJunction junction = junctions.get(i);
        Collection<RoadSegment> inRoads = junction.getAllRoads(false);
        for (RoadSegment roadSegment : inRoads) {
          if (segmentMap.containsKey(roadSegment.getId())) {
            junctionLocations.add(roadSegment.getJunctionLocation(junction));
            break;
          }
        }
      }
    }
    return junctionLocations;
  }

  public int size() {
    return segments.size();
  }

  public int getJunctionCount() {
    return junctions.size();
  }

  public Collection<RoadJunction> getBorderJunctions() {
    Collection<RoadJunction> borderJunctions = new ArrayList<RoadJunction>();
    for (int i = 0; i < isBorderPoint.length; i++) {
      if (isBorderPoint[i]) {
        borderJunctions.add(junctions.get(i));
      }
    }
    return borderJunctions;
  }

  /**
   * Get the shortest route between the two given locations, using the pre-computed distances.
   * 
   * TODO: add specialized code for end-of-segment (at-junction) locations
   * 
   */
  public Route getRoute(IVector source, IVector target) {
    RoadnetVector loc0 = source.toRoadnetVector();
    RoadSegment seg0 = loc0.getRoadSegment();
    RoadnetVector loc1 = target.toRoadnetVector();
    RoadSegment seg1 = loc1.getRoadSegment();

    // source & target on same segment:
    if (seg0 == seg1) {
      // compose single-segment route:
      Route route = new Route(loc0, loc1);
      route.addFirstSegment(seg0, loc0.getProgress() <= loc1.getProgress());
      route.setLength(Math.abs(loc0.getProgress() - loc1.getProgress()));
      return route;
    }

    int minDist = Integer.MAX_VALUE;
    int nodeIdx0_best = -1;
    int nodeIdx1_best = -1;
    int j0_best = -1;
    int j1_best = -1;
    // examine routes going forward & backward from source:
    for (int j0 = 0; j0 == 0 || (j0 == 1 && !seg0.isDirected()); j0++) {
      int nodeIdx0 = junctionMap.get(seg0.getEndJunction(j0).getId());
      // distance from source to first node on route:
      int dist0 = (j0 == 0 ? loc0.getProgress() : seg0.getLength() - loc0.getProgress());

      // examine routes going forward & backward to target:
      for (int j1 = 0; j1 == 0 || (j1 == 1 && !seg1.isDirected()); j1++) {
        int nodeIdx1 = junctionMap.get(seg1.getEndJunction(j1).getId());
        // distance from target to last node on route:
        int dist1 = (j1 == 0 ? loc1.getProgress() : seg1.getLength() - loc1.getProgress());

        // total distance is node-to-node distance + fractions on the source & target segments:
        int newMinDist = d[nodeIdx0][nodeIdx1] + dist0 + dist1;
        if (newMinDist < minDist) {
          minDist = newMinDist;
          nodeIdx0_best = nodeIdx0;
          nodeIdx1_best = nodeIdx1;
          j0_best = j0;
          j1_best = j1;
        }
      }
    }

    // compose the best route found:
    Route route = new Route(loc0, loc1);
    // set the pre-computed distance:
    route.setLength(minDist);
    route.addFirstSegment(seg0, j0_best == 1); // fwd-traversed if exiting source's segment at junction[1]
    for (int idx = 0; idx < minPath[nodeIdx0_best][nodeIdx1_best].size(); idx++) {
      int segId = minPath[nodeIdx0_best][nodeIdx1_best].get(idx);
      route.addLastSegment(segments.get(segId), direction[nodeIdx0_best][nodeIdx1_best].get(idx));
    }
    route.addLastSegment(seg1, j1_best == 0); // fwd-traversed if entering target's segment at junction[0]

    return route;
  }

  /**
   * Get route to the nearest trigger point (border point or given point of interest).
   */
  public Route getRouteToNearestTriggerPoint(List<RoadnetVector> points, RoadnetVector location) {
    List<RoadnetVector> triggers = new LinkedList<RoadnetVector>(borderLocations);
    triggers.addAll(points);

    Route minRoute = null;
    for (RoadnetVector triggerPoint : triggers) {
      Route newMinRoute = getRoute(location, triggerPoint);
      if (newMinRoute != null && (minRoute == null || newMinRoute.getLength() < minRoute.getLength())) {
        minRoute = newMinRoute;
      }
    }

    return minRoute;
  }

  public int getJunctionDistance(RoadJunction jun0, RoadJunction jun1) {
    int junId0 = junctionMap.get(jun0.getId());
    int junId1 = junctionMap.get(jun1.getId());
    return d[junId0][junId1];
  }

  public int getMaxJunctionDistance(RoadJunction jun0) {
    int junId = junctionMap.get(jun0.getId());
    int maxDist = 0;
    for (int i = 0; i < d.length; ++i) {
      maxDist = Math.max(maxDist, d[junId][i]);
    }
    return maxDist;
  }
}
