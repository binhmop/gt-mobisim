// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator;

import java.io.IOException;

public interface ITraceGenerator {

  public void generateTrace(String traceFilename) throws IOException;

}
