// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config;

import org.w3c.dom.Element;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.config.paramparser.IParamParser;
import edu.gatech.lbs.sim.config.paramparser.TimeParser;

public class XmlTimesConfigInterpreter implements IXmlConfigInterpreter {

  public void initFromXmlElement(Element rootNode, Simulation sim) {
    IParamParser pparser = new TimeParser();
    long simStartTime = pparser.parse(rootNode.getAttribute("starttime"));
    long simEndTime = pparser.parse(rootNode.getAttribute("endtime"));
    String warmupTimeStr = rootNode.getAttribute("warmup");
    long simWarmupDuration = warmupTimeStr.isEmpty() ? 0 : pparser.parse(warmupTimeStr);
    System.out.println("Runtime= " + String.format("%.1f", (simEndTime - simStartTime) / (1000.0 * 60)) + " simulated minutes (including " + String.format("%.1f", simWarmupDuration / (1000.0 * 60)) + " minutes of warmup)");

    sim.setSimTimes(simStartTime, simEndTime, simWarmupDuration);
  }
}