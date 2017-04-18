// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config.helper;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.w3c.dom.Element;
import edu.gatech.lbs.core.logging.Logz;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.config.IXmlConfigInterpreter;
import edu.gatech.lbs.sim.config.paramparser.SpeedParser;
import edu.gatech.lbs.sim.config.paramparser.TimeParser;
import edu.gatech.lbs.sim.scheduling.activity.TraceGenerationActivity;
import edu.gatech.lbs.sim.scheduling.activity.TraceLoadingActivity;
import edu.gatech.lbs.sim.tracegenerator.ITraceGenerator;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.IndividualMobilityTraceGenerator;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual.IndividualMobilityModel;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual.RandomWaypoint_IndividualMobilityModel;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual.RoadnetFixedEndpoint_IndividualMobilityModel;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual.RoadnetRandomTrip_IndividualMobilityModel;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual.RoadnetRandomWaypoint_IndividualMobilityModel;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.ILocationDistribution;
import edu.gatech.lbs.sim.tracegenerator.paramdistribution.IParamDistribution;

public class XmlMobilityModelInterpreter implements IXmlConfigInterpreter {

  public void initFromXmlElement(Element mobilitymodelNode, Simulation sim) {
    ITraceGenerator mobilityTraceGenerator = null;

    String mobilityTraceFilename = mobilitymodelNode.getAttribute("filename");
    String mobilitymodelType = mobilitymodelNode.getAttribute("type");
    String overwriteAllowed = mobilitymodelNode.getAttribute("overwrite");

    // location distribution:
    Element locationDistributionNode = (Element) mobilitymodelNode.getElementsByTagName("locationdistribution").item(0);
    XmlLocationDistributionInterpreter ldInterpreter = new XmlLocationDistributionInterpreter();
    ldInterpreter.initFromXmlElement(locationDistributionNode, sim);
    ILocationDistribution locationDistribution = ldInterpreter.getLocationDistribution();

    // speed distribution:
    Element speedDistributionNode = (Element) mobilitymodelNode.getElementsByTagName("speeddistribution").item(0);
    XmlParamDistributionInterpreter pdInterpreter = new XmlParamDistributionInterpreter(new SpeedParser());
    pdInterpreter.initFromXmlElement(speedDistributionNode, sim);
    IParamDistribution speedDistribution = pdInterpreter.getParamDistribution();

    Collection<SimAgent> agents = sim.getAgents();

    if (mobilitymodelType.equalsIgnoreCase(RandomWaypoint_IndividualMobilityModel.xmlName)) {
      List<IndividualMobilityModel> mobilityModels = new ArrayList<IndividualMobilityModel>(agents.size());
      for (SimAgent agent : agents) {
        mobilityModels.add(new RandomWaypoint_IndividualMobilityModel(sim, agent, locationDistribution,
            speedDistribution, sim.getSimStartTime()));
      }
      mobilityTraceGenerator = new IndividualMobilityTraceGenerator(sim.getSimStartTime(), sim.getSimEndTime(),
          mobilityModels);

    } else if (mobilitymodelType.equalsIgnoreCase(RoadnetRandomWaypoint_IndividualMobilityModel.xmlName)) {
      Element stoppingTimeNode = (Element) mobilitymodelNode.getElementsByTagName("stopping").item(0);
      XmlParamDistributionInterpreter sInterpreter = new XmlParamDistributionInterpreter(new TimeParser());
      sInterpreter.initFromXmlElement(stoppingTimeNode, sim);
      IParamDistribution stoppingTimeDistribution = sInterpreter.getParamDistribution();

      List<IndividualMobilityModel> mobilityModels = new ArrayList<IndividualMobilityModel>(agents.size());
      for (SimAgent agent : agents) {
        mobilityModels.add(new RoadnetRandomWaypoint_IndividualMobilityModel(sim, agent, locationDistribution,
            speedDistribution, stoppingTimeDistribution, sim.getSimStartTime()));
      }
      mobilityTraceGenerator = new IndividualMobilityTraceGenerator(sim.getSimStartTime(), sim.getSimEndTime(),
          mobilityModels);

    } else if (mobilitymodelType.equalsIgnoreCase(RoadnetRandomTrip_IndividualMobilityModel.xmlName)) {
      Element parkingTimeNode = (Element) mobilitymodelNode.getElementsByTagName("parking").item(0);
      XmlParamDistributionInterpreter pInterpreter = new XmlParamDistributionInterpreter(new TimeParser());
      pInterpreter.initFromXmlElement(parkingTimeNode, sim);
      IParamDistribution parkingTimeDistribution = pInterpreter.getParamDistribution();

      Element stoppingTimeNode = (Element) mobilitymodelNode.getElementsByTagName("stopping").item(0);
      XmlParamDistributionInterpreter sInterpreter = new XmlParamDistributionInterpreter(new TimeParser());
      sInterpreter.initFromXmlElement(stoppingTimeNode, sim);
      IParamDistribution stoppingTimeDistribution = sInterpreter.getParamDistribution();

      List<IndividualMobilityModel> mobilityModels = new ArrayList<IndividualMobilityModel>(agents.size());
      for (SimAgent agent : agents) {
        mobilityModels.add(new RoadnetRandomTrip_IndividualMobilityModel(sim, agent, locationDistribution,
            speedDistribution, parkingTimeDistribution, stoppingTimeDistribution, sim.getSimStartTime(), (RoadMap) sim
                .getWorld()));
      }
      mobilityTraceGenerator = new IndividualMobilityTraceGenerator(sim.getSimStartTime(), sim.getSimEndTime(),
          mobilityModels);

    } else if (mobilitymodelType.equalsIgnoreCase(RoadnetFixedEndpoint_IndividualMobilityModel.xmlName)) {
      String startNumStr = mobilitymodelNode.getAttribute("startCount");
      String destCountStr = mobilitymodelNode.getAttribute("destCount");
      int startCount = startNumStr.isEmpty() ? 0 : Integer.parseInt(startNumStr);
      int destCount = destCountStr.isEmpty() ? 0 : Integer.parseInt(destCountStr);

      assert destCount > 0 : "destCount must be provided and must be greater than zero";

      // Set fixed endpoints
      IVector[] locs = selectRandomLocations(sim, startCount + destCount);
      IVector[] initLocations = null;
      IVector[] destLocations = new IVector[destCount];
      if (startCount == 0) {
        destLocations = locs;
      } else {
        initLocations = new IVector[startCount];
        for (int i = 0; i < startCount; i++) {
          initLocations[i] = locs[i];
        }
        for (int j = 0; j < destCount; j++) {
          destLocations[j] = locs[startCount + j];
        }
      }

      Element parkingTimeNode = (Element) mobilitymodelNode.getElementsByTagName("parking").item(0);
      XmlParamDistributionInterpreter pInterpreter = new XmlParamDistributionInterpreter(new TimeParser());
      pInterpreter.initFromXmlElement(parkingTimeNode, sim);
      IParamDistribution parkingTimeDistribution = pInterpreter.getParamDistribution();

      Element stoppingTimeNode = (Element) mobilitymodelNode.getElementsByTagName("stopping").item(0);
      XmlParamDistributionInterpreter sInterpreter = new XmlParamDistributionInterpreter(new TimeParser());
      sInterpreter.initFromXmlElement(stoppingTimeNode, sim);
      IParamDistribution stoppingTimeDistribution = sInterpreter.getParamDistribution();

      List<IndividualMobilityModel> mobilityModels = new ArrayList<IndividualMobilityModel>(agents.size());
      for (SimAgent agent : agents) {
        mobilityModels.add(new RoadnetFixedEndpoint_IndividualMobilityModel(sim, agent, locationDistribution,
            speedDistribution, parkingTimeDistribution, stoppingTimeDistribution, sim.getSimStartTime(), (RoadMap) sim
                .getWorld(), initLocations, destLocations));
      }
      mobilityTraceGenerator = new IndividualMobilityTraceGenerator(sim.getSimStartTime(), sim.getSimEndTime(),
          mobilityModels);

    } else {
      Logz.println("Unknown mobility model: " + mobilitymodelType);
      System.exit(-1);
    }

    sim.addActivity(new TraceGenerationActivity(mobilityTraceFilename, mobilityTraceGenerator, overwriteAllowed
        .equalsIgnoreCase("yes")));
    sim.addActivity(new TraceLoadingActivity(mobilityTraceFilename));
  }

  // generate fixed locations
  protected IVector[] selectRandomLocations(Simulation sim, int locCount) {
    int segCount = ((RoadMap) sim.getWorld()).getRoadSegmentCount();
    HashSet<Integer> locIdxSet = new HashSet<Integer>();
    Random rnd = new Random();
    while (locIdxSet.size() < locCount) {
      locIdxSet.add(rnd.nextInt(segCount));
    }
    IVector[] locs = new IVector[locIdxSet.size()];
    RoadSegment[] segments = ((RoadMap) sim.getWorld()).getRoadSegments().toArray(new RoadSegment[segCount]);
    int idx = 0;
    Logz.print("List of road segments selected for fixed endpoint mode: ");
    for (int k : locIdxSet) {
      RoadSegment seg = segments[k];
      locs[idx] = new RoadnetVector(seg, (int) (rnd.nextDouble() * seg.getLength()));
      idx++;
      Logz.print(seg.getId() + " ");
    }
    Logz.println();
    return locs;

  }
}
