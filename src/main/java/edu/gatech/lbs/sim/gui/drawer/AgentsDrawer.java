// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.gui.drawer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Collection;

import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.gui.SimPanel;

public class AgentsDrawer implements IDrawer {
  public boolean isAgentVectorOn = false;

  private Simulation sim;
  private SimPanel panel;

  public AgentsDrawer(Simulation sim, SimPanel panel) {
    this.sim = sim;
    this.panel = panel;
  }

  public void draw(Graphics g) {
    g.setColor(Color.magenta);
    Collection<SimAgent> agents = sim.getAgents();
    for (SimAgent agent : agents) {
      IVector loc = agent.getLocation();
      Point p0 = panel.getPixel(loc.toCartesianVector());
      g.fillOval(p0.x - 2, p0.y - 2, 5, 5);
      // g.drawLine(p0.x - 1, p0.y - 1, p0.x + 1, p0.y + 1);
      // g.drawLine(p0.x - 1, p0.y + 1, p0.x + 1, p0.y - 1);

      if (isAgentVectorOn) {
        IVector v = agent.getVelocity();
        Point p1 = panel.getPixel(loc.toCartesianVector().add(loc.toRoadnetVector().toTangentVector().times(1e-6 * v.getLength() * 5).toCartesianVector()).toCartesianVector());
        g.drawLine(p0.x, p0.y, p1.x, p1.y);
      }
    }
  }
}