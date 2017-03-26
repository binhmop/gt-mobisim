// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import edu.gatech.lbs.core.logging.Logz;
import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.BoundingBox;
import edu.gatech.lbs.core.world.IWorld;
import edu.gatech.lbs.core.world.roadnet.partition.Partition;
import edu.gatech.lbs.core.world.roadnet.route.Route;

public class RoadMap implements IWorld {
  protected boolean isDirected; // is traffic-directedness meaningful for this map?
  protected HashMap<Integer, RoadSegment> segments; // segmentId --> segment
  protected HashMap<Integer, RoadJunction> junctions; // junctionId --> junction

  private int highestSegmentId;

  private Collection<Partition> partitions;

  private BoundingBox bounds;

  public RoadMap(boolean isDirected) {
    segments = new HashMap<Integer, RoadSegment>();
    junctions = new HashMap<Integer, RoadJunction>();

    this.isDirected = isDirected;

    highestSegmentId = -1;

    bounds = new BoundingBox();
  }

  public int getRoadSegmentCount() {
    return segments.size();
  }

  public Collection<RoadSegment> getRoadSegments() {
    return segments.values();
  }

  /**
   * Add a road segment, ensuring that the end-junctions are also correctly stored & connected.
   * 
   * @param segment
   * @return
   */
  public boolean addRoadSegment(RoadSegment segment) {
    if (segments.containsKey(segment.getId())) {
      return false;
    }

    // store segment:
    segments.put(segment.getId(), segment);
    highestSegmentId = Math.max(highestSegmentId, segment.getId());

    // store junctions:
    for (int i = 0; i < 2; i++) {
      RoadJunction junction = segment.getEndJunction(i);
      if (!junctions.containsKey(junction.getId())) {
        junctions.put(junction.getId(), junction);
      }
    }

    // connect road to junctions:
    segment.getSourceJunction().addOriginatingRoad(segment);
    segment.getTargetJunction().addTerminatingRoad(segment);

    return true;
  }

  /**
   * Remove a road segment, ensuring that the end-junctions are also correctly removed.
   * 
   * @param segment
   */
  public void removeRoadSegment(RoadSegment segment) {
    // remove segment:
    segments.remove(segment.getId());

    // remove segment from junctions, and also remove unconnected junctions:
    for (int i = 0; i < 2; i++) {
      RoadJunction junction = segment.getEndJunction(i);
      junction.removeRoad(segment);
      if (junction.getDegree() == 0) {
        junctions.remove(junction);
      }
    }
  }

  public RoadSegment getRoadSegment(int segmentId) {
    return segments.get(segmentId);
  }

  public RoadJunction getRoadJunction(int junctionId) {
    return junctions.get(junctionId);
  }

  public Collection<RoadJunction> getRoadJunctions() {
    return junctions.values();
  }

  public boolean isDirected() {
    return isDirected;
  }

  public int getNextSegmentId() {
    return highestSegmentId + 1;
  }

  public long getLengthTotal() {
    long lengthTotal = 0;
    for (RoadSegment segment : segments.values()) {
      lengthTotal += segment.getLength();
    }
    return lengthTotal;
  }

  public void showStats() {
    System.out.println("World bounds: " + bounds.toString());
    showSegmentStats(segments.values());
    showJunctionStats();
  }

  public static void showSegmentStats(Collection<RoadSegment> segs) {
    double lengthTotal = 0, lengthMin = Double.MAX_VALUE, lengthMax = Double.MIN_VALUE;
    int pointsTotal = 0, pointsMin = Integer.MAX_VALUE, pointsMax = Integer.MIN_VALUE;
    double travelTimeTotal = 0;

    for (RoadSegment segment : segs) {
      double length = segment.getLength() / 1000.0;
      lengthTotal += length;
      lengthMin = Math.min(lengthMin, length);
      lengthMax = Math.max(lengthMax, length);

      int points = segment.getGeometry().getPoints().length;
      pointsTotal += points;
      pointsMin = Math.min(pointsMin, points);
      pointsMax = Math.max(pointsMax, points);

      travelTimeTotal += segment.getLength() / (double) segment.getSpeedLimit();
    }
    int segmentCount = segs.size();
    double lengthAvg = lengthTotal / (double) segmentCount;
    double pointsAvg = pointsTotal / (double) segmentCount;
    double travelTimeAvg = travelTimeTotal / (double) segmentCount;
    System.out.println(" Segment totals: count= " + segmentCount + ", length= " + String.format("%.1f", lengthTotal / 1000) + " km (" + String.format("%.1f", travelTimeTotal / 3600) + " h), points= " + pointsTotal);
    System.out.println("  length per segment: avg= " + String.format("%.1f", lengthAvg) + " m (" + String.format("%.1f", travelTimeAvg) + " sec), min= " + String.format("%.1f", lengthMin) + " m, max= " + String.format("%.1f", lengthMax) + " m");
    System.out.println("  points per segment: avg= " + String.format("%.1f", pointsAvg) + ", min= " + pointsMin + ", max= " + pointsMax + " ");
  }

  public void showJunctionStats() {
    int deg1 = 0, deg2 = 0, degMax = Integer.MIN_VALUE;
    double degAvg = 0;

    for (RoadJunction junction : junctions.values()) {
      int degree = junction.getDegree();
      deg1 += degree == 1 ? 1 : 0;
      deg2 += degree == 2 ? 1 : 0;
      degAvg += degree;
      degMax = Math.max(degMax, degree);
    }
    int junctionCount = junctions.size();
    degAvg /= (double) junctionCount;

    System.out.println(" Junctions: count= " + junctionCount);
    System.out.println("  degree: 1-degree= " + deg1 + ", 2-degree= " + deg2 + ", avg= " + String.format("%.1f", degAvg) + ", max= " + degMax);
  }

  protected Collection<RoadJunction> getRoadJunctionsInOrder(int orderingMode) {
    TreeSet<RoadJunctionDistance> juncSet = new TreeSet<RoadJunctionDistance>();

    for (RoadJunction junction : junctions.values()) {
      int score = 0;
      switch (orderingMode) {
      case 1:
        // order by speed-thru:
        for (RoadSegment seg : junction.getAllRoads(true)) {
          score -= seg.getSpeedLimit();
        }
        break;
      default:
        // random order:
        score = (int) (1e6 * Math.random());
      }

      juncSet.add(new RoadJunctionDistance(junction, score));
    }

    List<RoadJunction> juncs = new LinkedList<RoadJunction>();
    for (RoadJunctionDistance junc : juncSet) {
      juncs.add(junc.junction);
    }
    return juncs;
  }

  /**
   * Partition the roadnet.
   */
  public Collection<Partition> makePartitions(int partitionRadius, int distanceMode, int seedPriorityMode) {
    partitions = new ArrayList<Partition>();

    HashMap<Integer, Integer> segmentStatus = new HashMap<Integer, Integer>(); // segmentID --> partitionID
    HashMap<Integer, Integer> junctionStatus = new HashMap<Integer, Integer>(); // junctionID --> code (1: in partition, 2: border point)

    LinkedList<RoadJunction> juncs = new LinkedList<RoadJunction>(getRoadJunctionsInOrder(seedPriorityMode));

    // while there are uncovered segments:
    while (segmentStatus.size() < segments.size()) {
      // if there are no completely uncovered junctions, only 1-segment partition(s) remain:
      if (junctionStatus.size() == junctions.size()) {
        // find all uncovered segments, and make a partition out of each:
        for (Integer segmentId : segments.keySet()) {
          if (segmentStatus.containsKey(segmentId)) {
            continue;
          }
          Partition p = new Partition(partitions.size());
          RoadSegment seg = segments.get(segmentId);
          p.addSegment(seg);
          Collection<RoadJunction> B = new LinkedList<RoadJunction>();
          B.add(seg.startJunction);
          B.add(seg.endJunction);
          p.setBorderPoints(B);
          partitions.add(p);

          segmentStatus.put(segmentId, p.getId());
        }
      } else {
        Partition p = new Partition(partitions.size());
        TreeSet<RoadJunctionDistance> junctionQueue = new TreeSet<RoadJunctionDistance>();
        HashMap<Integer, RoadJunctionDistance> junctionDist = new HashMap<Integer, RoadJunctionDistance>(); // junctionID --> minDist
        List<RoadJunction> borderPoints = new LinkedList<RoadJunction>(); // partition border points

        RoadJunction seedJun = juncs.poll();
        while (junctionStatus.containsKey(seedJun.getId())) {
          seedJun = juncs.poll();
        }
        int d = 0;
        RoadJunctionDistance seedDist = new RoadJunctionDistance(seedJun, d);
        junctionQueue.add(seedDist);
        junctionDist.put(seedJun.getId(), seedDist);

        while (!junctionQueue.isEmpty()) {
          RoadJunction jun = junctionQueue.pollFirst().junction;
          d = junctionDist.get(jun.getId()).distance;
          if (d <= partitionRadius) {
            junctionStatus.put(jun.getId(), 1);
            List<RoadSegment> reachableSegments = jun.getReachableRoads();
            for (RoadSegment seg : reachableSegments) {
              // if segment is uncovered:
              if (!segmentStatus.containsKey(seg.getId())) {
                segmentStatus.put(seg.getId(), p.getId());
                p.addSegment(seg);

                int d2 = 0;
                switch (distanceMode) {
                case 1:
                  d2 = d + 1; // hop; [count]
                  break;
                case 2:
                  d2 = d + seg.getLength(); // road-distance; [mm]
                  break;
                case 3:
                  d2 = d + 1000 * (int) ((double) seg.getLength() / seg.getSpeedLimit()); // travel-distance; [ms]
                  break;
                default:
                  Logz.println("Partitioning failed on invalid mode.");
                  System.exit(-1);
                }

                RoadJunction otherEnd = seg.getOtherJunction(jun);

                // if other end is already a border in another partition, it must be a border in this one too:
                if (junctionStatus.containsKey(otherEnd.getId()) && junctionStatus.get(otherEnd.getId()) == 2) {
                  borderPoints.add(otherEnd);
                } else {
                  // set the shortest available distance for the other end-junction:
                  RoadJunctionDistance d3 = junctionDist.get(otherEnd.getId());
                  if (d3 == null || d2 < d3.distance) {
                    if (d3 != null) {
                      junctionQueue.remove(d3);
                    }
                    RoadJunctionDistance otherDist = new RoadJunctionDistance(otherEnd, d2);
                    junctionQueue.add(otherDist);
                    junctionDist.put(otherEnd.getId(), otherDist);

                    // mark as internal point:
                    junctionStatus.put(otherEnd.getId(), 1);
                  }
                }
              }
            }
          } else {
            // push back over-the-range junction:
            junctionQueue.add(new RoadJunctionDistance(jun, d));
            break;
          }
        }

        // all junctions remaining in the queue are borders (too-far other-ends of in-partition segments),
        // except those that only have outlets into the current precinct:
        for (RoadJunctionDistance roadJunctionDist : junctionQueue) {
          RoadJunction roadJunction = roadJunctionDist.junction;
          List<RoadSegment> reachableSegments = roadJunction.getReachableRoads();
          for (RoadSegment roadSegment : reachableSegments) {
            if (!segmentStatus.containsKey(roadSegment.getId()) || segmentStatus.get(roadSegment.getId()) != p.getId()) {
              borderPoints.add(roadJunction);
              break;
            }
          }
        }
        // mark as border:
        for (RoadJunction point : borderPoints) {
          junctionStatus.put(point.getId(), 2);
        }

        // don't bother with border-points (& distance pre-calculation), when looking for connected components:
        if (partitionRadius < Integer.MAX_VALUE) {
          p.setBorderPoints(borderPoints);
        }
        partitions.add(p);
      }

    }

    return partitions;
  }

  public void setPartitions(Collection<Partition> partitions) {
    this.partitions = partitions;
  }

  public Collection<Partition> getPartitions() {
    return partitions;
  }

  public Collection<Partition> getConnectedComponents() {
    // for undirected road networks (the result might not be correct for directed road networks)
    return makePartitions(Integer.MAX_VALUE, 1, 0);
  }

  /**
   * Calculate path lengths between all node pairs.
   */
  public static int[][] doFloydWarshall(int path[][], List<Integer>[][] minPath, List<Boolean>[][] direction) {
    int n = path.length;

    for (int k = 0; k < n; k++) {
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          int newValue = path[i][k] + path[k][j];
          if (newValue >= 0 && newValue < path[i][j]) {
            path[i][j] = newValue;

            minPath[i][j].clear();
            minPath[i][j].addAll(minPath[i][k]);
            minPath[i][j].addAll(minPath[k][j]);

            direction[i][j].clear();
            direction[i][j].addAll(direction[i][k]);
            direction[i][j].addAll(direction[k][j]);
          }
        }
      }
    }

    return path;
  }

  /**
   * Shortest route with Dijkstra's algorithm.
   * 
   * @param source
   * @param target
   * @return
   */
  public Route getShortestRoute(RoadnetVector source, RoadnetVector target) {
    return getSpanningTree(source, target, null, null);
  }

  public void getSpanningTree(RoadnetVector source, HashMap<Integer, RoadJunctionDistance> junctionDist, HashMap<Integer, RoadSegment> previous) {
    getSpanningTree(source, null, junctionDist, previous);
  }

  /**
   * Spanning tree & shortest route with Dijkstra's algorithm.
   * Data for visited nodes is dynamically maintained with a hash-structure (expecting that
   * the number of nodes visited will be much less than the total number of nodes in the graph).
   * 
   * @param source
   * @param target
   * @return
   */
  private Route getSpanningTree(RoadnetVector source, RoadnetVector target, HashMap<Integer, RoadJunctionDistance> junctionDist, HashMap<Integer, RoadSegment> previous) {
    Route route = (target != null) ? new Route(source, target) : null;

    TreeSet<RoadJunctionDistance> junctionQueue = new TreeSet<RoadJunctionDistance>();
    junctionDist = (junctionDist != null) ? junctionDist : new HashMap<Integer, RoadJunctionDistance>(); // junctionID --> minDist
    previous = (previous != null) ? previous : new HashMap<Integer, RoadSegment>(); // junctionID --> previous-road

    RoadSegment sourceSeg = source.getRoadSegment();
    RoadSegment targetSeg = (target != null) ? target.getRoadSegment() : null;

    if (target != null) {
      // if source & target are on same segment, and target is reachable from source on the segment:
      if (sourceSeg.getId() == targetSeg.getId() && (!sourceSeg.isDirected || source.getProgress() <= target.getProgress())) {
        // route has only one segment:
        route.addFirstSegment(sourceSeg, source.getProgress() <= target.getProgress());
        return route;
      }
    }

    // expand to two ends of current segment:
    for (int j = 0; j == 0 || (j == 1 && !sourceSeg.isDirected()); j++) {
      RoadJunction jun = sourceSeg.getEndJunction(j);
      int d = (j == 0 ? source.getProgress() : sourceSeg.getLength() - source.getProgress());

      RoadJunctionDistance otherDist = new RoadJunctionDistance(jun, d);
      junctionQueue.add(otherDist);
      junctionDist.put(jun.getId(), otherDist);
    }

    int dMin = Integer.MAX_VALUE;
    RoadJunction lastJunction = null;
    while (!junctionQueue.isEmpty() && junctionQueue.first().distance < dMin) {
      RoadJunction jun = junctionQueue.pollFirst().junction;
      List<RoadSegment> reachableSegments = jun.getReachableRoads();
      for (RoadSegment segment : reachableSegments) {
        int d2 = junctionDist.get(jun.getId()).distance + segment.getLength(); // road-distance
        RoadJunction otherEnd = segment.getOtherJunction(jun);

        // set the shortest available distance for the other end-junction:
        RoadJunctionDistance d3 = junctionDist.get(otherEnd.getId());
        if (d3 == null || d2 < d3.distance) {
          if (d3 != null) {
            junctionQueue.remove(d3);
          }
          RoadJunctionDistance otherDist = new RoadJunctionDistance(otherEnd, d2);
          junctionQueue.add(otherDist);
          junctionDist.put(otherEnd.getId(), otherDist);

          previous.put(otherEnd.getId(), segment);
        }

        // check for possible shortest route, if arrived to a junction next to target:
        if (target != null) {
          int idx = targetSeg.getJunctionIndex(otherEnd);
          if (idx != -1) {
            int dMin2 = d2 + (idx == 0 ? target.getProgress() : targetSeg.getLength() - target.getProgress());
            if (dMin2 < dMin) {
              dMin = dMin2;
              lastJunction = otherEnd;
            }
          }
        }

      }
    }

    if (target != null) {
      // last segment of route:
      route.addLastSegment(targetSeg, targetSeg.getJunctionIndex(lastJunction) == 0);

      // collect segments of route:
      RoadJunction junction = lastJunction;
      RoadSegment seg;
      do {
        seg = previous.get(junction.getId());
        if (seg != null) {
          route.addFirstSegment(seg, seg.getJunctionIndex(junction) == 1);
          junction = seg.getOtherJunction(junction);
        }
      } while (seg != null);

      // first segment of route:
      route.addFirstSegment(sourceSeg, sourceSeg.getJunctionIndex(junction) == 1);
    }

    return route;
  }

  public BoundingBox getBounds() {
    return bounds;
  }

  // TODO: use an index
  public RoadnetVector getRoadnetLocation(CartesianVector v) {
    long minDist = -1;
    RoadnetVector minVector = null;
    for (RoadSegment segment : segments.values()) {
      RoadnetVector vv = segment.getRoadnetLocation(v);
      long dist = vv.toCartesianVector().vectorTo(v).getLength();
      if (minDist < 0 || dist < minDist) {
        minDist = dist;
        minVector = vv;
      }
    }

    return minVector;
  }
}
