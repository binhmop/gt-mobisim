// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.paramdistribution;

import java.util.Random;

import edu.gatech.lbs.core.vector.IVector;

public class UniformParamDistribution implements IParamDistribution {
  public static final String xmlName = "uniform";

  private double min; // [m] or [m/s] or [s]
  private double max; // [m] or [m/s] or [s]

  private Random rnd;

  public UniformParamDistribution(double min, double max) {
    this.min = min;
    this.max = max;

    rnd = new Random();
  }

  public double getNextValue(IVector location) {
    double speed;
    do {
      speed = min + rnd.nextDouble() * (max - min);
    } while (speed <= 0);
    return speed;
  }

  public double getMaxValue() {
    return max;
  }
}
