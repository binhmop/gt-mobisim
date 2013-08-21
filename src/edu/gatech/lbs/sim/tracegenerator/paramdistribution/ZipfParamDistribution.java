// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Bugra Gedik
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.paramdistribution;

import edu.gatech.lbs.core.vector.IVector;

/*
 * Created on May 6, 2003
 *
 * Bugra Gedik
 * IBM T.J. Watson Research Center
 */
public class ZipfParamDistribution implements IParamDistribution {
  public static final String xmlName = "zipf";

  private double zp;
  private int n;
  private double p[];

  public ZipfParamDistribution(double zp, int n) {
    this.zp = zp;
    this.n = n;
    p = new double[n];

    init();
  }

  private void init() {
    double sum = 0;
    for (int i = 0; i < n; i++) {
      p[i] = 1 / Math.pow(i + 1, zp);
      sum = sum + p[i];
    }
    for (int i = 0; i < n; i++) {
      p[i] = p[i] / sum;
    }
    for (int i = 1; i < n; i++) {
      p[i] = p[i] + p[i - 1];
    }
  }

  public double getMean(double[] vals) {
    double mean = vals[0] * p[0];
    for (int i = 1; i < n; i++) {
      mean += (p[i] - p[i - 1]) * vals[i];
    }
    return mean;
  }

  public int getNextValue(IVector location) {
    double v = Math.random();
    for (int i = 0; i < n; i++) {
      if (v < p[i]) {
        return i;
      }
    }
    return 0;
  }
}
