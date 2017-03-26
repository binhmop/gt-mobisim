// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config.helper;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.config.IXmlConfigInterpreter;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.HierarchicGaussianLocationDistribution;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.HotspotRoadnetLocationDistribution;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.ILocationDistribution;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.UniformLocationDistribution;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.UniformRoadnetLocationDistribution;

public class XmlLocationDistributionInterpreter implements IXmlConfigInterpreter {
  protected ILocationDistribution locationDistribution;

  public ILocationDistribution getLocationDistribution() {
    return locationDistribution;
  }

  public void initFromXmlElement(Element locationDistributionNode, Simulation sim) {
    locationDistribution = null;
    if (locationDistributionNode == null) {
      return;
    }

    String locationdistributionType = locationDistributionNode.getAttribute("type");
    if (locationdistributionType.equalsIgnoreCase(UniformLocationDistribution.xmlName)) {
      locationDistribution = new UniformLocationDistribution(sim.getWorld().getBounds());

    } else if (locationdistributionType.equalsIgnoreCase(HierarchicGaussianLocationDistribution.xmlName)) {
      NodeList levelNodes = locationDistributionNode.getElementsByTagName("level");
      int levelCount = levelNodes.getLength();
      int[] distrHierarchyStDev = new int[levelCount];
      double[] distrHierarcyExitProb = new double[levelCount];
      for (int level = 0; level < levelCount; level++) {
        Element levelNode = (Element) levelNodes.item(level);
        distrHierarchyStDev[level] = Integer.parseInt(levelNode.getAttribute("dev"));
        distrHierarcyExitProb[level] = Double.parseDouble(levelNode.getAttribute("exit"));
      }
      // int[] distrHierarchyStDev = { 5000, 500 };
      // double[] distrHierarcyExitProb = { 0.05, 0.1 };
      locationDistribution = new HierarchicGaussianLocationDistribution(sim.getWorld().getBounds(), distrHierarchyStDev, distrHierarcyExitProb);

    } else if (locationdistributionType.equalsIgnoreCase(UniformRoadnetLocationDistribution.xmlName)) {
      /** eg.:
        <locationdistribution type="uniformroadnet">
      	<class weight="0.2"/>
      	<class weight="0.5"/>
      	<class weight="0.2"/>
      	<class weight="0.1"/>
        </locationdistribution>
       */
      NodeList classNodes = locationDistributionNode.getElementsByTagName("class");
      int classCount = classNodes.getLength();
      double[] classWeight;
      if (classCount > 0) {
        classWeight = new double[classCount];

        for (int roadclass = 0; roadclass < classCount; roadclass++) {
          Element classNode = (Element) classNodes.item(roadclass);
          classWeight[roadclass] = Double.parseDouble(classNode.getAttribute("weight"));
        }
      } else {
        classWeight = null;
      }

      boolean doLengthWeighting = true;
      String weightingString = locationDistributionNode.getAttribute("length_weighting");
      if (weightingString != null && weightingString.equalsIgnoreCase("off")) {
        doLengthWeighting = false;
      }

      locationDistribution = new UniformRoadnetLocationDistribution((RoadMap) sim.getWorld(), classWeight, doLengthWeighting);

    } else if (locationdistributionType.equalsIgnoreCase(HotspotRoadnetLocationDistribution.xmlName)) {
      int hotspotCount = Integer.parseInt(locationDistributionNode.getAttribute("count"));
      double coeff = Double.parseDouble(locationDistributionNode.getAttribute("coeff"));
      String seedStr = locationDistributionNode.getAttribute("seed");
      long seed = seedStr.isEmpty() ? -1 : Long.parseLong(seedStr);

      locationDistribution = new HotspotRoadnetLocationDistribution((RoadMap) sim.getWorld(), hotspotCount, coeff, seed);

    } else {
      System.out.println("Unknown location distribution: " + locationdistributionType);
      System.exit(-1);
    }
  }
}
