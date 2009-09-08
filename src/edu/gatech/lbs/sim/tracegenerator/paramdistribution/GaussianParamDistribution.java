// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.paramdistribution;

import java.util.Random;

import edu.gatech.lbs.core.vector.IVector;

public class GaussianParamDistribution implements IParamDistribution {
  public static final String xmlName = "gaussian";

  private double mean; // [m] or [m/s] or [s]
  private double stdev; // [m] or [m/s] or [s]
  private double min; // [m] or [m/s] or [s]
  private double max; // [m] or [m/s] or [s]

  private Random rnd;

  public GaussianParamDistribution(double mean, double stdev, double min, double max) {
    this.mean = mean;
    this.stdev = stdev;
    this.min = min;
    this.max = max;

    rnd = new Random();
  }

  public double getNextValue(IVector location) {
    double value;
    do {
      value = mean + rnd.nextGaussian() * stdev;
    } while (value <= min || value > max);

    return value;
  }
}
