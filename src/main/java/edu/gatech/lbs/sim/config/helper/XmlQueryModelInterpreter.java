// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config.helper;

import org.w3c.dom.Element;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.config.IXmlConfigInterpreter;
import edu.gatech.lbs.sim.config.paramparser.DistanceParser;
import edu.gatech.lbs.sim.config.paramparser.TimeParser;
import edu.gatech.lbs.sim.scheduling.activity.TraceGenerationActivity;
import edu.gatech.lbs.sim.scheduling.activity.TraceLoadingActivity;
import edu.gatech.lbs.sim.tracegenerator.ITraceGenerator;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.ILocationDistribution;
import edu.gatech.lbs.sim.tracegenerator.paramdistribution.IParamDistribution;
import edu.gatech.lbs.sim.tracegenerator.querytrace.GlobalFixedNumberRangeQueryModel;

public class XmlQueryModelInterpreter implements IXmlConfigInterpreter {
  protected String mobilityTraceFilename;

  public XmlQueryModelInterpreter(String mobilityTraceFilename) {
    this.mobilityTraceFilename = mobilityTraceFilename;
  }

  public void initFromXmlElement(Element querymodelNode, Simulation sim) {
    ITraceGenerator queryTraceGenerator = null;

    if (querymodelNode == null) {
      return;
    }

    String queryTraceFilename = querymodelNode.getAttribute("filename");
    String querymodelType = querymodelNode.getAttribute("type");
    String overwriteAllowed = querymodelNode.getAttribute("overwrite");

    if (querymodelType.equalsIgnoreCase(GlobalFixedNumberRangeQueryModel.getXmlName())) {
      String queryCountStr = querymodelNode.getAttribute("count");
      int queryCount = Integer.parseInt(queryCountStr.replace("%", ""));
      if (queryCountStr.contains("%")) {
        queryCount *= -1; // % comes with a uniform distribution of queries over agents
      }

      // radius distribution:
      Element rangeDistributionNode = (Element) querymodelNode.getElementsByTagName("radius").item(0);
      XmlParamDistributionInterpreter interpreter = new XmlParamDistributionInterpreter(new DistanceParser());
      interpreter.initFromXmlElement(rangeDistributionNode, sim);
      IParamDistribution rangeDistribution = interpreter.getParamDistribution();

      // lifetime:
      Element lifetimeDistributionNode = (Element) querymodelNode.getElementsByTagName("lifetime").item(0);
      XmlParamDistributionInterpreter interpreter2 = new XmlParamDistributionInterpreter(new TimeParser());
      interpreter2.initFromXmlElement(lifetimeDistributionNode, sim);
      IParamDistribution lifetimeDistribution = interpreter2.getParamDistribution();

      // location distribution:
      // if there is no locationdistribution defined, the queries will be uniformly distributed across simAgentIds
      Element locationDistributionNode = (Element) querymodelNode.getElementsByTagName("locationdistribution").item(0);
      XmlLocationDistributionInterpreter ldInterpreter = new XmlLocationDistributionInterpreter();
      ldInterpreter.initFromXmlElement(locationDistributionNode, sim);
      ILocationDistribution locationDistribution = ldInterpreter.getLocationDistribution();

      queryTraceGenerator = new GlobalFixedNumberRangeQueryModel(mobilityTraceFilename, sim, rangeDistribution, lifetimeDistribution, locationDistribution, queryCount);

    } else {
      System.out.println("Unknown query model: " + querymodelType);
      System.exit(-1);
    }

    sim.addActivity(new TraceGenerationActivity(queryTraceFilename, queryTraceGenerator, overwriteAllowed.equalsIgnoreCase("yes")));
    sim.addActivity(new TraceLoadingActivity(queryTraceFilename));
  }
}
