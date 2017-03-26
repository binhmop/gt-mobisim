// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.gui.drawer;

import edu.gatech.lbs.sim.Simulation;

import java.awt.Color;
import java.awt.Graphics;

public class TimeDrawer implements IDrawer {
	private Simulation sim;

	public TimeDrawer(Simulation sim) {
		this.sim = sim;
	}

	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.drawString(sim.getTime() / 1000 / 60 + ":" + sim.getTime() / 1000 % 60 + " min", 20, 20);
	}
}
