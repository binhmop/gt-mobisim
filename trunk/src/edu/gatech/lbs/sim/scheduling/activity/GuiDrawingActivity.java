// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.activity;

import java.util.ArrayList;
import java.util.List;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.gui.SimPanel;
import edu.gatech.lbs.sim.gui.drawer.AgentsDrawer;
import edu.gatech.lbs.sim.gui.drawer.IDrawer;
import edu.gatech.lbs.sim.gui.drawer.RoadMapDrawer;
import edu.gatech.lbs.sim.gui.drawer.RodDrawer;
import edu.gatech.lbs.sim.gui.drawer.TimeDrawer;
import edu.gatech.lbs.sim.scheduling.event.DrawGUIEvent;

public class GuiDrawingActivity implements ISimActivity {
  protected double period; // [sec]

  public GuiDrawingActivity(double period) {
    this.period = period;
  }

  protected List<IDrawer> getDrawers(Simulation sim, SimPanel panel) {
    List<IDrawer> drawers = new ArrayList<IDrawer>();
    drawers.add(new RoadMapDrawer(sim, panel));
    drawers.add(new AgentsDrawer(sim, panel));
    drawers.add(new TimeDrawer(sim));
    drawers.add(new RodDrawer(panel));
    return drawers;
  }

  public void scheduleOn(Simulation sim) {
    if (sim.getAgentCount() > 0) {
      try {
        SimPanel panel = SimPanel.makeGui(sim);
        panel.setDrawers(getDrawers(sim, panel));
        sim.addEvent(new DrawGUIEvent(sim, sim.getSimStartTime(), panel, period));
      } catch (Exception e) {
        System.out.println("No GUI.");
      }
    }
  }

  public void cleanup() {
    // do nothing

  }
}
