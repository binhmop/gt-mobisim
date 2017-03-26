// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import edu.gatech.lbs.core.world.roadnet.RoadMap;
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
        mobilityModels.add(new RandomWaypoint_IndividualMobilityModel(sim, agent, locationDistribution, speedDistribution, sim.getSimStartTime()));
      }
      mobilityTraceGenerator = new IndividualMobilityTraceGenerator(sim.getSimStartTime(), sim.getSimEndTime(), mobilityModels);

    } else if (mobilitymodelType.equalsIgnoreCase(RoadnetRandomWaypoint_IndividualMobilityModel.xmlName)) {
      Element stoppingTimeNode = (Element) mobilitymodelNode.getElementsByTagName("stopping").item(0);
      XmlParamDistributionInterpreter sInterpreter = new XmlParamDistributionInterpreter(new TimeParser());
      sInterpreter.initFromXmlElement(stoppingTimeNode, sim);
      IParamDistribution stoppingTimeDistribution = sInterpreter.getParamDistribution();

      List<IndividualMobilityModel> mobilityModels = new ArrayList<IndividualMobilityModel>(agents.size());
      for (SimAgent agent : agents) {
        mobilityModels.add(new RoadnetRandomWaypoint_IndividualMobilityModel(sim, agent, locationDistribution, speedDistribution, stoppingTimeDistribution, sim.getSimStartTime()));
      }
      mobilityTraceGenerator = new IndividualMobilityTraceGenerator(sim.getSimStartTime(), sim.getSimEndTime(), mobilityModels);

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
        mobilityModels.add(new RoadnetRandomTrip_IndividualMobilityModel(sim, agent, locationDistribution, speedDistribution, parkingTimeDistribution, stoppingTimeDistribution, sim.getSimStartTime(), (RoadMap) sim.getWorld()));
      }
      mobilityTraceGenerator = new IndividualMobilityTraceGenerator(sim.getSimStartTime(), sim.getSimEndTime(), mobilityModels);

    } else {
      System.out.println("Unknown mobility model: " + mobilitymodelType);
      System.exit(-1);
    }

    sim.addActivity(new TraceGenerationActivity(mobilityTraceFilename, mobilityTraceGenerator, overwriteAllowed.equalsIgnoreCase("yes")));
    sim.addActivity(new TraceLoadingActivity(mobilityTraceFilename));
  }
}
