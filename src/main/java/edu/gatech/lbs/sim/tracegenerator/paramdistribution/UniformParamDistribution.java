// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.paramdistribution;

import java.util.Random;

import edu.gatech.lbs.core.vector.IVector;

public class UniformParamDistribution implements IParamDistribution {
  public static final String xmlName = "uniform";

  private int min; // [nm] or [nm/s] or [ns]
  private int max; // [nm] or [mn/s] or [ns]

  private Random rnd;

  public UniformParamDistribution(int min, int max) {
    this.min = min;
    this.max = max;

    rnd = new Random();
  }

  public int getNextValue(IVector location) {
    int speed;
    do {
      speed = (int) (min + rnd.nextDouble() * (max - min));
    } while (speed <= 0);
    return speed;
  }

  public double getMaxValue() {
    return max;
  }
}
