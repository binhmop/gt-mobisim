// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config;

import edu.gatech.lbs.core.logging.Stat;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.config.paramparser.IParamParser;
import edu.gatech.lbs.sim.config.paramparser.TimeParser;

import org.w3c.dom.Element;

public class XmlTimesConfigInterpreter implements IXmlConfigInterpreter {

  public void initFromXmlElement(Element rootNode, Simulation sim) {
    IParamParser pparser = new TimeParser();
    long simStartTime = (long) (1000 * pparser.parse(rootNode.getAttribute("starttime")));
    long simEndTime = (long) (1000 * pparser.parse(rootNode.getAttribute("endtime")));
    String warmupTimeStr = rootNode.getAttribute("warmup");
    long simWarmupDuration = warmupTimeStr.isEmpty() ? 0 : (long) (1000 * pparser.parse(warmupTimeStr));
    System.out.println("Runtime= " + Stat.round((simEndTime - simStartTime) / (1000.0 * 60), 1) + " simulated minutes (including " + Stat.round(simWarmupDuration / (1000.0 * 60), 1) + " minutes of warmup)");

    sim.setSimTimes(simStartTime, simEndTime, simWarmupDuration);
  }
}