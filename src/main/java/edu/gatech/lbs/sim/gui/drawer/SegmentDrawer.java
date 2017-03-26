// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.gui.drawer;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.core.world.roadnet.RoadSegmentGeometry;
import edu.gatech.lbs.sim.gui.SimPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class SegmentDrawer implements IDrawer {
	private SimPanel panel;
	private RoadSegment seg;
	private Color color;

	public SegmentDrawer(SimPanel panel, RoadSegment seg, Color color) {
		this.panel = panel;
		this.seg = seg;
		this.color = color;
	}

	public void draw(Graphics g) {
		RoadSegmentGeometry geometry = seg.getGeometry();
		CartesianVector[] points = geometry.getPoints();
		for (int i = 0; i < points.length - 1; i++) {
			Point p0 = panel.getPixel(points[i]);
			Point p1 = panel.getPixel(points[i + 1]);

			// straight-line segment section:
			g.setColor(color);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);

			// start point:
			if (i == 0) {
				g.setColor(Color.black);
				g.drawLine(p0.x, p0.y, p0.x, p0.y);
			}
			// end point:
			if (i == points.length - 2) {
				g.setColor(Color.black);
				g.drawLine(p1.x, p1.y, p1.x, p1.y);
			}
		}
	}
}
