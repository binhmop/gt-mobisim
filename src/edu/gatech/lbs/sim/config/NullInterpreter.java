// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.scheduling.activity.GuiDrawingActivity;
import edu.gatech.lbs.sim.scheduling.activity.VarzWritingActivity;

import org.w3c.dom.Element;

/**
 * Adds schedulers that are always used, regardless of what is in the config.
 */
public class NullInterpreter implements IXmlConfigInterpreter {

  public void initFromXmlElement(Element node, Simulation sim) {
    sim.addActivity(new GuiDrawingActivity(2.0));
    sim.addActivity(new VarzWritingActivity());
  }
}
