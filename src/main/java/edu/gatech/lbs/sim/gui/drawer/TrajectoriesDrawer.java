package edu.gatech.lbs.sim.gui.drawer;


import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.sim.gui.SimPanel;

public class TrajectoriesDrawer implements IDrawer {
  private Collection<List<IVector>> trajs;
  private SimPanel panel;


  public TrajectoriesDrawer(Collection<List<IVector>> trajs, SimPanel panel) {
    this.trajs = trajs;
    this.panel = panel;
  }

  public void draw(Graphics g) {
    g.setColor(Color.green);
    for (List<IVector> traj : trajs) {
      IDrawer trajDrawer = new TrajectoryDrawer(panel, traj, g.getColor());
      trajDrawer.draw(g);
      g.setColor(TrajectoryDrawer.getRandomColor(new Random()));
    }
  }
}
