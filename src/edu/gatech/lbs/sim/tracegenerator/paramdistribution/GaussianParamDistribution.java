// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.paramdistribution;

import java.util.Random;

import edu.gatech.lbs.core.vector.IVector;

public class GaussianParamDistribution implements IParamDistribution {
  public static final String xmlName = "gaussian";

  private int mean; // [mm] or [mm/s] or [ms]
  private int stdev; // [mm] or [mm/s] or [ms]
  private int min; // [mm] or [mm/s] or [ms]
  private int max; // [mm] or [mm/s] or [ms]

  private Random rnd;

  public GaussianParamDistribution(int mean, int stdev, int min, int max) {
    this.mean = mean;
    this.stdev = stdev;
    this.min = min;
    this.max = max;

    rnd = new Random();
  }

  public int getNextValue(IVector location) {
    int value;
    do {
      value = (int) (mean + rnd.nextGaussian() * stdev);
    } while (value <= min || value > max);

    return value;
  }
}
