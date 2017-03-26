// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution;

import java.util.Random;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.world.BoundingBox;

public class HierarchicGaussianLocationDistribution implements ILocationDistribution {
  public static final String xmlName = "hierarchicgaussian";

  private BoundingBox worldBox;

  private int[] distrHierarchyStDev; // std. deviation of the initial hierarchic levels [km]
  private double[] distrHierarcyExitProb; // exit probability for each distribution hierarchy level [0..1]

  private CartesianVector[] hier_center;
  private int maxlevel; // level 0: base; last level: individual
  private int level;
  private Random rnd;

  public HierarchicGaussianLocationDistribution(BoundingBox worldBox, int[] distrHierarchyStDev, double[] distrHierarcyExitProb) {
    this.worldBox = worldBox;

    this.distrHierarchyStDev = distrHierarchyStDev;
    this.distrHierarcyExitProb = distrHierarcyExitProb;

    maxlevel = distrHierarchyStDev.length + 1;
    level = 0;
    hier_center = new CartesianVector[maxlevel];

    rnd = new Random();
  }

  public IVector getNextLocation() {

    // force one level down (to generate a new individual's position)
    level--;
    // exit from level(s):
    while (level > 0 && rnd.nextDouble() < distrHierarcyExitProb[level - 1]) {
      level--;
    }
    // uniform distribution on top level:
    if (level <= 0) {
      for (int d = 0; d < 2; d++) {
        hier_center[0].setDimension(d, (long) (rnd.nextDouble() * worldBox.getDimension(d)));
      }
      level = 1;
    }
    // go down level(s) to required hierarchic depth:
    while (level < maxlevel) {
      for (int d = 0; d < 2; d++) {
        hier_center[level].setDimension(d, hier_center[level - 1].getDimension(d) + (int) (rnd.nextGaussian() * distrHierarchyStDev[level - 1]));

        // ensure it's within world boundaries:
        while (hier_center[level].getDimension(d) < 0) {
          hier_center[level].setDimension(d, hier_center[level].getDimension(d) + worldBox.getDimension(d));
        }
        while (hier_center[level].getDimension(d) > worldBox.getDimension(d)) {
          hier_center[level].setDimension(d, hier_center[level].getDimension(d) - worldBox.getDimension(d));
        }
      }
      level++;
    }
    return hier_center[maxlevel - 1];
  }
}
