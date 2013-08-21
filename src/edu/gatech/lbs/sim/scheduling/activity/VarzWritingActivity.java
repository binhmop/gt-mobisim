// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.activity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import edu.gatech.lbs.core.logging.IVarzFormatter;
import edu.gatech.lbs.core.logging.SimpleVarzFormatter;
import edu.gatech.lbs.core.logging.Varz;
import edu.gatech.lbs.sim.Simulation;

public class VarzWritingActivity implements ISimActivity {

  protected IVarzFormatter getVarzFormatter() {
    return new SimpleVarzFormatter();
  }

  public void scheduleOn(Simulation sim) {
    // do nothing
  }

  public void cleanup() {
    String varzFilename = Varz.getString("varzFilename");
    if (!varzFilename.isEmpty()) {
      System.out.print("Writing varz file... ");
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter(varzFilename, true));
        getVarzFormatter().writeTo(out);
        out.close();
        System.out.println("done.");
      } catch (IOException e) {
        System.out.println("failed.");
        System.exit(-1);
      }
    }
    Varz.clear();
  }
}
