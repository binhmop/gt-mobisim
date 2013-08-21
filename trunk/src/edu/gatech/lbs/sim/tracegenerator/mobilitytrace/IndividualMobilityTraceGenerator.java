// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.mobilitytrace;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import edu.gatech.lbs.sim.scheduling.SimEventQueue;
import edu.gatech.lbs.sim.scheduling.event.SimEvent;
import edu.gatech.lbs.sim.tracegenerator.ITraceGenerator;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual.IndividualMobilityModel;

public class IndividualMobilityTraceGenerator implements ITraceGenerator {
  protected long simStartTime;
  protected long simEndTime;
  protected List<IndividualMobilityModel> mobilityModels;

  public IndividualMobilityTraceGenerator(long simStartTime, long simEndTime, List<IndividualMobilityModel> mobilityModels) {
    this.simStartTime = simStartTime;
    this.simEndTime = simEndTime;
    this.mobilityModels = mobilityModels;
  }

  // -trace generation broken up into generate-then-write-to-disk time-chunks (eg. 1 min)
  // -trace loading is also done in scheduled chunks, so is a low-memory operation
  public void generateTrace(String traceFilename) throws IOException {
    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(traceFilename)));

    long wallStartTime = System.nanoTime();

    SimEventQueue overflowQueue = new SimEventQueue();
    int eventCount = 0;
    long simStageLength = (long) (1000 * 60 * 5.0 / (mobilityModels.size() / 10000.0)); // stage length [ms]
    for (long simStageEndTime = Math.min(simEndTime, simStageLength); simStageEndTime <= simEndTime; simStageEndTime = Math.min(simEndTime, simStageEndTime + simStageLength)) {
      SimEventQueue queue = new SimEventQueue();

      // sift thru the overflow queue from previous stage, and separate events belonging
      // to current stage, and to events overflowing even the current stage:
      SimEventQueue overflowQueue2 = new SimEventQueue();
      SimEvent e;
      while ((e = overflowQueue.pop()) != null) {
        if (e.getTimestamp() < simStageEndTime) {
          queue.addEvent(e);
        } else {
          overflowQueue2.addEvent(e);
        }
      }
      overflowQueue = overflowQueue2;

      // maintain which mobility models have finished current stage:
      int simStageEndedCount = 0;
      boolean[] simStageEnded = new boolean[mobilityModels.size()];
      for (int i = 0; i < mobilityModels.size(); i++) {
        simStageEnded[i] = false;
      }

      // collect events until everybody's finished the stage:
      do {
        for (int i = 0; i < mobilityModels.size(); i++) {
          if (!simStageEnded[i]) {
            SimEvent event = mobilityModels.get(i).getNextEvent();

            // store in queue for current stage:
            if (event.getTimestamp() < simStageEndTime) {
              queue.addEvent(event);
            }
            // store for next stage, and signal end-of-stage for this object:
            else {
              overflowQueue.addEvent(event);
              simStageEnded[i] = true;
              simStageEndedCount++;
            }
          }
        }
      } while (simStageEndedCount < mobilityModels.size());

      eventCount += queue.size();
      queue.saveTo(out);

      System.out.println("  " + String.format("%.2f", simStageEndTime / 60.0 / 1000) + " simulated minutes elapsed...");

      // terminate if stage end is also simulation end:
      if (simStageEndTime == simEndTime) {
        break;
      }
    }

    double simSeconds = (simEndTime - simStartTime) / (1000.0);
    System.out.println("  done. (" + eventCount + " trace records, " + String.format("%.2f", simSeconds / ((double) eventCount / mobilityModels.size())) + " simulated seconds/trace record)");
    double simToWallSpeedRatio = (simEndTime - simStartTime) / ((System.nanoTime() - wallStartTime) / 1e6);
    System.out.println("  Speed: " + String.format("%.1f", simToWallSpeedRatio) + "x realtime (" + String.format("%.1f", simToWallSpeedRatio / 60.0) + " simulated hours/wall minute)");
    out.close();
  }

  /*
  public void writeKmlTrace(){
  	// kml trace:
  	if (i == 0) {
  		// write KML trace:
  		try {
  			BufferedWriter kml = new BufferedWriter(new FileWriter(traceFilename + ".kml"));
  			kml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
  			kml.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
  			kml.write("<Document>\n");

  			kml.write("<Style id=\"user\">\n");
  			kml.write("<IconStyle>");
  			kml.write("<color>ffffffff</color>");
  			kml.write("<Icon>");
  			kml.write("<href>http://maps.google.com/mapfiles/kml/shapes/truck.png</href>");
  			kml.write("</Icon>");
  			kml.write("</IconStyle>");
  			kml.write("</Style>\n");

  			kml.write("<Folder>\n");
  			// kml.write("<name>User #" + user.getUID() + "</name>\n");
  			IMobilityChangeEvent event0 = (IMobilityChangeEvent) trace.pop();
  			IMobilityChangeEvent nextEvent = (IMobilityChangeEvent) trace.pop();
  			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
  			while (event0 != null) {
  				kml.write("<Placemark>\n");
  				kml.write("<styleUrl>#user</styleUrl>\n");
  				kml.write("<TimeSpan>\n");
  				kml.write("<begin>" + dateFormat.format(new Date(event0.getTimestamp())) + "</begin>\n");
  				kml.write("<end>" + dateFormat.format(new Date((nextEvent != null ? nextEvent.getTimestamp() : simEndTime))) + "</end>\n");
  				kml.write("</TimeSpan>\n");
  				kml.write("<Point>\n");
  				CartesianVector loc = event0.getLocation().toCartesianVector();
  				kml.write("<coordinates>" + loc.getLongitude() + "," + loc.getLatitude() + "</coordinates>\n");
  				kml.write("</Point>\n");
  				kml.write("</Placemark>\n");

  				event0 = nextEvent;
  				if (event0 != null) {
  					nextEvent = (IMobilityChangeEvent) trace.pop();
  				}
  			}
  			kml.write("</Folder>\n");

  			kml.write("</Document>\n");
  			kml.write("</kml>\n");
  			kml.close();
  		} catch (IOException e) {
  			System.err.println("Error: " + e.getMessage());
  		}
  	}
  }*/
}
