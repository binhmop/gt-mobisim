// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.gatech.lbs.core.FileHelper;
import edu.gatech.lbs.core.logging.Varz;
import edu.gatech.lbs.core.world.IWorld;
import edu.gatech.lbs.core.world.roadnet.ClassedRoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.core.world.roadnet.parser.MapParser;
import edu.gatech.lbs.core.world.roadnet.parser.ShpMapParser;
import edu.gatech.lbs.core.world.roadnet.parser.SvgMapParser;
import edu.gatech.lbs.core.world.roadnet.partition.Partition;
import edu.gatech.lbs.core.world.roadnet.writer.KMLMapWriter;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.config.paramparser.DistanceParser;
import edu.gatech.lbs.sim.config.paramparser.IParamParser;
import edu.gatech.lbs.sim.config.paramparser.SpeedParser;
import edu.gatech.lbs.sim.config.paramparser.TimeParser;

public class XmlWorldConfigInterpreter implements IXmlConfigInterpreter {

  public static boolean doKMLmap = false;

  protected RoadMap makeRoadMap(String[] roadClassNames, int[] speedLimits) {
    boolean isClassed = roadClassNames != null && speedLimits != null;
    RoadMap roadmap;
    if (isClassed) {
      roadmap = new ClassedRoadMap(roadClassNames, speedLimits);
    } else {
      roadmap = new RoadMap(false);
    }
    return roadmap;
  }

  public void initFromXmlElement(Element rootNode, Simulation sim) throws IOException {
    Element worldNode = (Element) rootNode.getElementsByTagName("world").item(0);

    IWorld world = null;
    String worldType = worldNode.getAttribute("type");
    if (worldType.equalsIgnoreCase("roadnet")) {
      Element fileNode = (Element) worldNode.getElementsByTagName("file").item(0);
      String roadmapFilename = fileNode.getAttribute("name");

      NodeList classNodes = fileNode.getElementsByTagName("class");
      int classCount = classNodes.getLength();
      String[] roadClassNames = null;
      int[] speedLimits = null;
      if (classCount > 0) {
        roadClassNames = new String[classCount];
        speedLimits = new int[classCount];
        IParamParser pparser = new SpeedParser();
        for (int roadclass = 0; roadclass < classCount; roadclass++) {
          Element classNode = (Element) classNodes.item(roadclass);
          roadClassNames[roadclass] = classNode.getAttribute("name");
          String vmax_str = classNode.getAttribute("v_max");
          speedLimits[roadclass] = vmax_str.length() > 0 ? pparser.parse(vmax_str) : Integer.MAX_VALUE;
        }
      }

      System.out.print("Loading roadmap from '" + roadmapFilename + "'... ");
      RoadMap roadmap = makeRoadMap(roadClassNames, speedLimits);
      MapParser parser = null;

      String extension = roadmapFilename.substring(roadmapFilename.lastIndexOf('.'), roadmapFilename.length());
      if (extension.equalsIgnoreCase(".svg")) {
        parser = new SvgMapParser();
      } else if (extension.equalsIgnoreCase(".shp")) {
        parser = new ShpMapParser();
      } else {
        System.out.println("FAILED. Unknown roadmap file extension '" + extension + "'.");
        System.exit(-1);
      }
      parser.load(roadmapFilename, roadmap);
      System.out.println("done.");

      System.out.print("Analyzing road network graph connectivity... ");
      // find maximum connected component:
      Collection<Partition> components = roadmap.getConnectedComponents();
      int maxComponentSize = -1;
      Partition maxComponent = null;
      for (Partition component : components) {
        if (component.getSegments().size() > maxComponentSize) {
          maxComponentSize = component.getSegments().size();
          maxComponent = component;
        }
      }

      // remove all segments, which are not in the max. connected component:
      int trashSegmentCount = 0;
      for (Partition component : components) {
        if (component != maxComponent) {
          for (RoadSegment segment : component.getSegments()) {
            roadmap.removeRoadSegment(segment);
            trashSegmentCount++;
          }
        }
      }
      System.out.println(components.size() + " connected components found. Removed " + trashSegmentCount + " segments that were not in the largest connected component.");

      // show stats:
      roadmap.showStats();
      Varz.set("roadmapLength", roadmap.getLengthTotal() / 1000.0);

      // partitioning:
      NodeList nl = worldNode.getElementsByTagName("partition");
      if (nl.getLength() != 0) {
        Collection<Partition> partitions = null;
        Element partitionNode = (Element) nl.item(0);
        String partitionFilename = partitionNode.getAttribute("filename");
        String overwriteAllowed = partitionNode.getAttribute("overwrite");

        if (partitionFilename.isEmpty() || overwriteAllowed.equalsIgnoreCase("yes") || !FileHelper.isNonEmptyFileOrUrl(partitionFilename)) {
          String partitionType = partitionNode.getAttribute("type");
          String radiusStr = partitionNode.getAttribute("radius");
          String seedPriorityStr = partitionNode.getAttribute("seed_priority");
          int seedPriorityMode;
          if (seedPriorityStr.equalsIgnoreCase("speed")) {
            seedPriorityMode = 1;
          } else {
            seedPriorityMode = 0;
          }
          System.out.print("Partitioning roadmap using " + partitionType + " type partitioning with radius=" + radiusStr + "... ");
          long wallStartTime = System.nanoTime();

          if (partitionType.equalsIgnoreCase("hop")) {
            int partitionRadius = Integer.parseInt(radiusStr);
            partitions = roadmap.makePartitions(partitionRadius, 1, seedPriorityMode);
            Varz.set("partitionRadius", partitionRadius);
          } else if (partitionType.equalsIgnoreCase("distance")) {
            IParamParser pparser = new DistanceParser();
            int partitionRadius = pparser.parse(radiusStr);
            partitions = roadmap.makePartitions(partitionRadius, 2, seedPriorityMode);
            Varz.set("partitionRadius", partitionRadius);
          } else if (partitionType.equalsIgnoreCase("time")) {
            IParamParser pparser = new TimeParser();
            int partitionRadius = pparser.parse(radiusStr);
            partitions = roadmap.makePartitions(partitionRadius, 3, seedPriorityMode);
            Varz.set("partitionRadius", partitionRadius);
          } else {
            System.out.println("FAILED. Unknown partitioning type '" + partitionType + "'.");
            System.exit(-1);
          }
          Varz.set("wallPartitionTime", (long) ((System.nanoTime() - wallStartTime) / 1e6));
          Varz.set("partitionCount", partitions.size());
          int partitioningNodeCount = 0;
          int partitioningNodePairsCount = 0;
          double partitioningNodeStdev = 0;

          for (Partition p : partitions) {
            partitioningNodeCount += p.getJunctionCount();
            partitioningNodePairsCount += Math.pow(p.getJunctionCount(), 2);
          }
          if (partitions.size() > 1) {
            double avgNodes = partitioningNodeCount / (double) partitions.size();
            for (Partition p : partitions) {
              partitioningNodeStdev += Math.pow(p.getJunctionCount() - avgNodes, 2);
            }
            partitioningNodeStdev = Math.sqrt(partitioningNodeStdev / (partitions.size() - 1));
          }
          Varz.set("partitioningNodeCount", partitioningNodeCount);
          Varz.set("partitioningNodePairsCount", partitioningNodePairsCount);
          Varz.set("partitioningNodeStdev", partitioningNodeStdev);

          if (!partitionFilename.isEmpty()) {
            System.out.print("saving... ");
            try {
              DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(partitionFilename)));
              out.writeInt(partitions.size());
              for (Partition partition : partitions) {
                partition.saveTo(out);
              }
              out.close();
            } catch (IOException e) {
              System.out.println("Unable to write partition file '" + partitionFilename + "'.");
              System.exit(-1);
            }
          }

          System.out.println("done.");
        } else {
          try {
            System.out.print("Loading roadmap partitioning from '" + partitionFilename + "'... ");
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(partitionFilename)));
            int partitionCount = in.readInt();
            partitions = new ArrayList<Partition>(partitionCount);
            for (int i = 0; i < partitionCount; i++) {
              Partition p = new Partition(in, roadmap);
              partitions.add(p);
            }
            roadmap.setPartitions(partitions);
            in.close();
            System.out.println("done.");
          } catch (IOException e) {
            System.out.println("Unable to read partition file '" + partitionFilename + "'.");
            System.exit(-1);
          }
        }

        // stats:
        int minPart = Integer.MAX_VALUE, maxPart = 0, onePart = 0;
        for (Partition partition : partitions) {
          minPart = Math.min(minPart, partition.size());
          maxPart = Math.max(maxPart, partition.size());
          onePart += (partition.size() == 1 ? 1 : 0);
        }
        System.out.println("Partition count:\n 1-segment= " + onePart + ", total= " + partitions.size() + "\n" + "Segments/partition:\n min= " + minPart + ", avg= " + String.format("%.1f", roadmap.getRoadSegmentCount() / (double) partitions.size()) + ", max= " + maxPart);

        // output KML:
        if (doKMLmap) {
          KMLMapWriter.write(roadmap, partitions, partitionFilename + ".kml");
        }
      } else {
        System.out.println("No partitioning.");
      }

      world = roadmap;
      /*
      } else if (worldType.equalsIgnoreCase("freeform")) {
      IParamParser pparser = new DistanceParser();
      double width = pparser.parse(worldNode.getAttribute("width"));
      double height = pparser.parse(worldNode.getAttribute("height"));
      world = new BoundingBox(0, width, 0, height);
      */
    } else {
      System.out.println("Unknown world type: " + worldType);
      System.exit(-1);
    }

    sim.setWorld(world);
  }
}