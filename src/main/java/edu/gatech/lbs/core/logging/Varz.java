// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.logging;

import java.util.HashMap;

public class Varz {

  private static HashMap<String, Long> longValues = new HashMap<String, Long>();
  private static HashMap<String, Double> doubleValues = new HashMap<String, Double>();
  private static HashMap<String, String> stringValues = new HashMap<String, String>();

  public static void set(String field, long value) {
    longValues.put(field, value);
  }

  public static void set(String field, double value) {
    doubleValues.put(field, value);
  }

  public static void set(String field, String value) {
    stringValues.put(field, value);
  }

  public static void delete(String field) {
    longValues.remove(field);
    doubleValues.remove(field);
    stringValues.remove(field);
  }

  public static long increment(String field) {
    return add(field, 1);
  }

  public static long add(String field, long plus) {
    Long value = longValues.get(field);
    long v = (value == null) ? 0 : value;
    v += plus;
    longValues.put(field, v);
    return v;
  }

  public static double add(String field, double plus) {
    Double value = doubleValues.get(field);
    double v = (value == null) ? 0 : value;
    v += plus;
    doubleValues.put(field, v);
    return v;
  }

  public static long getLong(String field) {
    Long value = longValues.get(field);
    return (value == null) ? 0 : value;
  }

  public static double getDouble(String field) {
    Double value = doubleValues.get(field);
    return (value == null) ? 0 : value;
  }

  public static String getString(String field) {
    String value = stringValues.get(field);
    return (value == null) ? "" : value;
  }

  public static void clear() {
    longValues.clear();
    doubleValues.clear();
    stringValues.clear();
  }
}
