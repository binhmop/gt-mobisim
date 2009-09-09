// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import edu.gatech.lbs.core.logging.Logz;
import edu.gatech.lbs.core.logging.Stat;
import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.BoundingBox;
import edu.gatech.lbs.core.world.IWorld;
import edu.gatech.lbs.core.world.roadnet.partition.Partition;
import edu.gatech.lbs.core.world.roadnet.route.Route;

public class RoadMap implements IWorld {
  protected boolean isDirected; // is traffic-directedness meaningful for this map?
  protected HashMap<Integer, RoadSegment> segments; // segmentID --> segment
  protected List<RoadJunction> junctions;

  private int highestSegmentId;

  private Collection<Partition> partitions;

  private BoundingBox bounds;

  public RoadMap(boolean isDirected) {
    segments = new HashMap<Integer, RoadSegment>();
    junctions = new ArrayList<RoadJunction>();

    this.isDirected = isDirected;

    highestSegmentId = -1;

    bounds = new BoundingBox();
  }

  public int getNumberOfRoadSegments() {
    return segments.size();
  }

  public Collection<RoadSegment> getRoadSegments() {
    return segments.values();
  }

  /**
   * Add a road segment, ensuring that the end-junctions are also correctly stored.
   * 
   * @param segment
   * @return
   */
  public boolean addRoadSegment(RoadSegment segment) {
    Integer ID = new Integer(segment.getId());
    if (segments.containsKey(ID)) {
      return false;
    }

    // store segment:
    segments.put(ID, segment);
    highestSegmentId = Math.max(highestSegmentId, ID);

    // store junctions:
    for (int i = 0; i < 2; i++) {
      RoadJunction junction = segment.getEndJunction(i);
      if (junction.getId() >= junctions.size()) {
        junctions.add(junction);
      }
    }
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

  public RoadSegment getRoadSegment(int id) {
    return segments.get(id);
  }

  public RoadJunction getRoadJunction(int id) {
    return junctions.get(id);
  }

  public boolean isDirected() {
    return isDirected;
  }

  public int getNextValidId() {
    return highestSegmentId + 1;
  }

  public double getLengthTotal() {
    double lengthTotal = 0;
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

    for (RoadSegment segment : segs) {
      double length = segment.getLength();
      lengthTotal += length;
      lengthMin = Math.min(lengthMin, length);
      lengthMax = Math.max(lengthMax, length);

      int points = segment.getGeometry().getPoints().length;
      pointsTotal += points;
      pointsMin = Math.min(pointsMin, points);
      pointsMax = Math.max(pointsMax, points);
    }
    int segmentCount = segs.size();
    double lengthAvg = lengthTotal / (double) segmentCount;
    double pointsAvg = pointsTotal / (double) segmentCount;
    System.out.println(" Segment totals: count= " + segmentCount + ", length= " + Stat.round(lengthTotal / 1000, 1) + " km, points= " + pointsTotal);
    System.out.println("  length per segment: min= " + Stat.round(lengthMin, 1) + " m, avg= " + Stat.round(lengthAvg, 1) + " m, max= " + Stat.round(lengthMax, 1) + " m");
    System.out.println("  points per segment: min= " + pointsMin + ", avg= " + Stat.round(pointsAvg, 1) + ", max= " + pointsMax + " ");
  }

  public void showJunctionStats() {
    int deg1 = 0, deg2 = 0, degMax = Integer.MIN_VALUE;
    double degAvg = 0;

    for (Iterator<RoadJunction> it = junctions.iterator(); it.hasNext();) {
      RoadJunction junction = it.next();

      int degree = junction.getDegree();
      deg1 += degree == 1 ? 1 : 0;
      deg2 += degree == 2 ? 1 : 0;
      degAvg += degree;
      degMax = Math.max(degMax, degree);
    }
    int junctionCount = junctions.size();
    degAvg /= (double) junctionCount;

    System.out.println(" Junctions: count= " + junctionCount);
    System.out.println("  degree: 1-degree= " + deg1 + ", 2-degree= " + deg2 + ", avg= " + Stat.round(degAvg, 1) + ", max= " + degMax);
  }

  /**
   * Partition the roadnet.
   * 
   * @param partitionRadius
   * @param mode
   * @return
   */
  public Collection<Partition> makePartitions(double partitionRadius, int mode) {
    partitions = new ArrayList<Partition>();

    HashMap<Integer, Integer> segmentStatus = new HashMap<Integer, Integer>(); // segmentID --> partitionID
    HashMap<Integer, Integer> junctionStatus = new HashMap<Integer, Integer>(); // junctionID --> partitionID

    // while there are uncovered segments:
    while (segmentStatus.size() < segments.size()) {
      // find an uncovered junction:
      int junctionIdx;
      for (junctionIdx = (int) (Math.random() * junctions.size()); junctionStatus.size() < junctions.size() && junctionStatus.containsKey(junctions.get(junctionIdx).getId()); junctionIdx = (junctionIdx == 0) ? junctions.size() - 1 : junctionIdx - 1) {
      }

      // if there are no completely uncovered junctions, only 1-segment partition(s) remain:
      if (junctionStatus.size() == junctions.size()) {
        // find all uncovered segments, and make a partition out of each:
        for (int segmentId = 0; segmentStatus.size() < segments.size(); segmentId++) {
          // if this is a real segment's ID, and it is uncovered:
          if (segments.containsKey(segmentId) && !segmentStatus.containsKey(segmentId)) {
            Partition p = new Partition(partitions.size());
            RoadSegment seg = segments.get(segmentId);
            p.addSegment(seg);
            ArrayList<RoadJunction> B = new ArrayList<RoadJunction>();
            B.add(seg.startJunction);
            B.add(seg.endJunction);
            p.setBorderPoints(B);
            partitions.add(p);

            segmentStatus.put(segmentId, p.getId());
          }
        }
      } else {
        Partition p = new Partition(partitions.size());
        LinkedList<RoadJunction> junctionQueue = new LinkedList<RoadJunction>();
        HashMap<Integer, Double> junctionDist = new HashMap<Integer, Double>(); // junctionID --> minDist
        LinkedList<RoadJunction> borderPoints = new LinkedList<RoadJunction>(); // partition border points

        RoadJunction j = junctions.get(junctionIdx);
        double d = 0;
        junctionDist.put(j.getId(), d);

        while (j != null && (d = junctionDist.get(j.getId())) <= partitionRadius) {
          junctionStatus.put(j.getId(), 1);
          List<RoadSegment> reachableSegments = j.getReachableRoads();
          for (RoadSegment seg : reachableSegments) {
            // if segment is uncovered:
            if (!segmentStatus.containsKey(seg.getId())) {
              segmentStatus.put(seg.getId(), p.getId());
              p.addSegment(seg);

              double d2 = 0;
              switch (mode) {
              case 1:
                d2 = d + 1; // hop; [count]
                break;
              case 2:
                d2 = d + seg.getLength(); // road-distance; [m]
                break;
              case 3:
                d2 = d + seg.getLength() / seg.getSpeedLimit(); // travel-distance; [sec]
                break;
              default:
                Logz.println("Partitioning failed on invalid mode.");
                System.exit(-1);
              }

              RoadJunction otherEnd = seg.getOtherJunction(j);

              // if other end is already a border in another partition, it must be a border in this one too:
              if (junctionStatus.containsKey(otherEnd.getId()) && junctionStatus.get(otherEnd.getId()) == 2) {
                borderPoints.add(otherEnd);
              } else {
                // if other end is not enqueued with a shorter distance:
                Double d3 = junctionDist.get(otherEnd.getId());
                if (d3 == null || d2 < d3) {
                  // add other end-junction to ordered queue:
                  ListIterator<RoadJunction> it2 = junctionQueue.listIterator();
                  if (!junctionQueue.isEmpty()) {
                    while (it2.hasNext() && junctionDist.get(it2.next().getId()) < d2) {
                    }
                    it2.previous();
                  }
                  it2.add(otherEnd);
                  // set the shortest available distance for the other end-junction:
                  junctionDist.put(otherEnd.getId(), d2);
                  // mark as internal point:
                  junctionStatus.put(otherEnd.getId(), 1);
                }
              }

            }
          }

          // get next closest junction:
          j = junctionQueue.poll();
        }
        // push back over-the-range junction:
        if (j != null) {
          junctionQueue.addFirst(j);
        }

        // all junctions remaining in the queue are borders (too-far other-ends of in-partition segments),
        // except those that only have outlets into the current precinct:
        for (RoadJunction roadJunction : junctionQueue) {
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
        if (partitionRadius < Double.MAX_VALUE) {
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
    return makePartitions(Double.MAX_VALUE, 1);
  }

  /**
   * Calculate path lengths between all node pairs.
   */
  public static double[][] doFloydWarshall(double path[][], List<Integer>[][] minPath, List<Boolean>[][] direction) {
    int n = path.length;

    for (int k = 0; k < n; k++) {
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          double newValue = path[i][k] + path[k][j];
          if (newValue < path[i][j]) {
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

  public void getSpanningTree(RoadnetVector source, HashMap<Integer, Double> junctionDist, HashMap<Integer, RoadSegment> previous) {
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
  private Route getSpanningTree(RoadnetVector source, RoadnetVector target, HashMap<Integer, Double> junctionDist, HashMap<Integer, RoadSegment> previous) {
    Route route = (target != null) ? new Route(source, target) : null;

    LinkedList<RoadJunction> junctionQueue = new LinkedList<RoadJunction>();
    junctionDist = (junctionDist != null) ? junctionDist : new HashMap<Integer, Double>(); // junctionID --> minDist
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
      double d = (j == 0 ? source.getProgress() : sourceSeg.getLength() - source.getProgress());

      junctionDist.put(jun.getId(), d);
      if (junctionQueue.isEmpty() || junctionDist.get(junctionQueue.peek().getId()) <= d) {
        junctionQueue.addFirst(jun);
      } else {
        junctionQueue.addLast(jun);
      }
    }

    // indicator for reaching the two target nodes (at both ends of target segment)
    boolean[] reachedTargetNode = new boolean[2];
    for (int i = 0; i < 2; i++) {
      reachedTargetNode[i] = false;
    }

    RoadJunction jun = junctionQueue.poll();
    while (jun != null && !(reachedTargetNode[0] && reachedTargetNode[1])) {
      List<RoadSegment> reachableSegments = jun.getReachableRoads();
      for (RoadSegment segment : reachableSegments) {
        double d2 = junctionDist.get(jun.getId()) + segment.getLength(); // road-distance
        RoadJunction otherEnd = segment.getOtherJunction(jun);

        // if other end is not enqueued with a shorter distance:
        Double d3 = junctionDist.get(otherEnd.getId());
        if (d3 == null || d2 < d3) {
          // add other end-junction to ordered queue:
          ListIterator<RoadJunction> it2 = junctionQueue.listIterator();
          if (!junctionQueue.isEmpty()) {
            while (it2.hasNext() && junctionDist.get(it2.next().getId()) < d2) {
            }
            it2.previous();
          }
          it2.add(otherEnd);
          // set the shortest available distance for the other end-junction:
          junctionDist.put(otherEnd.getId(), d2);
          previous.put(otherEnd.getId(), segment);
        }

        // check if arrived to junctions next to target:
        if (target != null) {
          for (int i = 0; i < 2; i++) {
            if (otherEnd.getId() == targetSeg.getEndJunction(i).getId()) {
              reachedTargetNode[i] = true;
            }
          }
        }

      }

      // get next closest junction:
      jun = junctionQueue.poll();
    }

    if (target != null) {
      // decide if target is reached via junction0 or junction1 of target-segment:
      double dist0 = reachedTargetNode[0] ? junctionDist.get(targetSeg.getEndJunction(0).getId()) + target.getProgress() : Double.MAX_VALUE;
      double dist1 = reachedTargetNode[1] ? junctionDist.get(targetSeg.getEndJunction(1).getId()) + targetSeg.getLength() - target.getProgress() : Double.MAX_VALUE;

      RoadJunction junction;
      if (dist0 <= dist1) {
        junction = targetSeg.getEndJunction(0);
        route.addLastSegment(targetSeg, true);
      } else {
        junction = targetSeg.getEndJunction(1);
        route.addLastSegment(targetSeg, false);
      }

      // collect segments of route:
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
    double minDist = -1;
    RoadnetVector minVector = null;
    for (RoadSegment segment : segments.values()) {
      RoadnetVector vv = segment.getRoadnetLocation(v);
      double dist = vv.toCartesianVector().vectorTo(v).getLength();
      if (minDist < 0 || dist < minDist) {
        minDist = dist;
        minVector = vv;
      }
    }

    return minVector;
  }
}
