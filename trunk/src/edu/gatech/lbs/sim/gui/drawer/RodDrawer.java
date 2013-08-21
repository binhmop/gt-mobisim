// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.gui.drawer;

import java.awt.Color;
import java.awt.Graphics;

import edu.gatech.lbs.sim.gui.SimPanel;

public class RodDrawer implements IDrawer {
  private SimPanel panel;

  public RodDrawer(SimPanel panel) {
    this.panel = panel;
  }

  public void draw(Graphics g) {
    String[] unitName = new String[] { "mm", "m", "km" };
    double lengthPixel = 1 / panel.mmPerPixel;
    long lengthMm = 1;
    while (lengthPixel < 25) {
      lengthPixel *= 10;
      lengthMm *= 10;
    }

    int unitNameIdx = 0;
    while (unitNameIdx < unitName.length - 1 && lengthMm >= 1000) {
      lengthMm /= 1000;
      unitNameIdx++;
    }

    g.setColor(Color.black);
    int x0 = 20;
    int y0 = panel.getHeight() - 20;
    g.fillRect(x0, y0, (int) lengthPixel, 3);
    g.drawString(lengthMm + " " + unitName[unitNameIdx], x0, y0);
    for (int i = 0; i < 3; i++) {
      int x = (int) (x0 + i * lengthPixel / 2);
      g.drawLine(x, y0, x, y0 + 6);
    }
  }
}