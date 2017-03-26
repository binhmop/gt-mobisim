// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.gui.drawer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import edu.gatech.lbs.core.world.roadnet.RoadJunction;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.core.world.roadnet.partition.Partition;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.gui.SimPanel;

public class RoadMapDrawer implements IDrawer {
  public boolean isPartitionBordersOn = false;

  private Simulation sim;
  private SimPanel panel;
  private List<Color> partitionColors;

  public RoadMapDrawer(Simulation sim, SimPanel panel) {
    this.sim = sim;
    this.panel = panel;
  }

  public void draw(Graphics g) {
    RoadMap roadmap = (RoadMap) sim.getWorld();

    if (partitionColors == null) {
      partitionColors = new ArrayList<Color>();
      for (Partition partition : roadmap.getPartitions()) {
        partitionColors.add(new Color((int) (Math.random() * 256 * 256 * 256)));
      }
    }

    // show roadmap:
    for (RoadSegment seg : roadmap.getRoadSegments()) {
      int partitionID = seg.getPartition().getId();
      g.setColor(partitionColors.get(partitionID));

      IDrawer segDrawer = new SegmentDrawer(panel, seg, Color.lightGray);
      segDrawer.draw(g);
    }

    if (isPartitionBordersOn) {
      g.setColor(Color.black);
      for (Partition partition : roadmap.getPartitions()) {
        for (RoadJunction roadJunction : partition.getBorderJunctions()) {
          Point p = panel.getPixel(roadJunction.getCartesianLocation());
          g.drawRect(p.x - 1, p.y - 1, 2, 2);
        }

        if (partition.getId() == 0) {
          for (RoadSegment seg : partition.getSegments()) {
            IDrawer segDrawer = new SegmentDrawer(panel, seg, Color.black);
            segDrawer.draw(g);
          }
        }
      }
    }
  }
}
