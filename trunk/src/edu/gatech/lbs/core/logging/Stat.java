// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.logging;

public class Stat {

  public static String getString(double[] numbers) {
    double stat[] = Stat.getSumMeanStdev(numbers);
    double min = Stat.getMin(numbers);
    double max = Stat.getMax(numbers);

    return (int) stat[0] + "\t" + stat[1] + "\t" + stat[2] + "\t" + min + "\t" + max;
  }

  public static double[] getSumMeanStdev(double[] numbers) {
    double sum = getSum(numbers);
    double mean = sum / numbers.length;

    // standard deviation:
    double stdev = 0;
    for (int i = 0; i < numbers.length; i++) {
      stdev += Math.pow(mean - numbers[i], 2);
    }
    stdev = Math.sqrt(stdev / numbers.length);

    // package results:
    double[] result = new double[3];
    result[0] = sum;
    result[1] = mean;
    result[2] = stdev;
    return result;
  }

  public static double getSum(double[] numbers) {
    double sum = 0;
    for (int i = 0; i < numbers.length; i++) {
      sum += numbers[i];
    }
    return sum;
  }

  public static double getMax(double[] numbers) {
    int maxIdx = 0;

    for (int i = 1; i < numbers.length; i++) {
      if (numbers[i] > numbers[maxIdx]) {
        maxIdx = i;
      }
    }

    return numbers[maxIdx];
  }

  public static double getMin(double[] numbers) {
    int minIdx = 0;

    for (int i = 1; i < numbers.length; i++) {
      if (numbers[i] < numbers[minIdx]) {
        minIdx = i;
      }
    }

    return numbers[minIdx];
  }

  public static int getValueCount(double[] numbers, double value) {
    int count = 0;

    for (int i = 1; i < numbers.length; i++) {
      if (numbers[i] == value) {
        count++;
      }
    }

    return count;
  }
}
