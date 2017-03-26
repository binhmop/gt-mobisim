// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.world.roadnet.RoadJunction;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.core.world.roadnet.RoadSegmentGeometry;
import edu.gatech.lbs.core.world.roadnet.partition.Partition;

public class KMLMapWriter {

  public static void write(RoadMap roadmap, Collection<Partition> partitions, String filename) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(filename));
      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      out.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
      out.write("<Document>\n");

      out.write("<Style id=\"border\">\n");
      out.write("<IconStyle>");
      out.write("<color>80ffffff</color>");
      out.write("<Icon>");
      out.write("<href>http://maps.google.com/mapfiles/kml/shapes/placemark_circle.png</href>");
      out.write("</Icon>");
      out.write("</IconStyle>");
      out.write("</Style>\n");

      // style definitions for partitions:
      for (Partition partition : partitions) {
        out.write("<Style id=\"style" + partition.getId() + "\">\n");
        out.write("<LineStyle>\n");
        // String colorStr = Integer.toHexString((128 + i) % 256);
        // out.write("<color>ff" + colorStr + colorStr + colorStr + "</color>\n");
        String colorStr = Integer.toHexString((int) (Math.random() * 256 * 256 * 256));
        while (colorStr.length() < 6) {
          colorStr = '0' + colorStr;
        }
        out.write("<color>ff" + colorStr + "</color>\n");
        out.write("<width>3</width>\n");
        out.write("</LineStyle>\n");
        // out.write("<PolyStyle>\n");
        // out.write("<color>7d" + colorStr + colorStr + colorStr + "</color>\n");
        // out.write("</PolyStyle>\n");
        out.write("</Style>\n");
      }

      for (Partition partition : partitions) {
        // partition boundary points:
        Collection<RoadJunction> borderJunctions = partition.getBorderJunctions();
        out.write("<Folder>\n");
        out.write("<name>Partition #" + partition.getId() + "</name>\n");
        out.write("<description>" + partition.getSegments().size() + " segments\n" + borderJunctions.size() + " border points</description>");
        for (RoadJunction borderJunction : borderJunctions) {
          out.write("<Placemark>\n");
          out.write("<description>Partition #" + partition.getId() + "</description>\n");
          out.write("<styleUrl>#border</styleUrl>\n");
          out.write("<Point>\n");
          CartesianVector loc = borderJunction.getCartesianLocation();
          out.write("<coordinates>" + loc.getLongitude() + "," + loc.getLatitude() + "</coordinates>\n");
          out.write("</Point>\n");
          out.write("</Placemark>\n");
        }

        // road segments:
        for (RoadSegment segment : partition.getSegments()) {
          RoadSegmentGeometry geometry = segment.getGeometry();
          CartesianVector[] points = geometry.getPoints();
          out.write("<Placemark>\n");
          out.write("<styleUrl>#style" + segment.getPartition().getId() + "</styleUrl>\n");
          out.write("<LineString>\n");
          out.write("<coordinates>\n");
          for (CartesianVector point : points) {
            out.write("\t" + point.getLongitude() + "," + point.getLatitude() + "\n");
          }
          out.write("</coordinates>\n");
          out.write("</LineString>\n");
          out.write("</Placemark>\n");
        }

        out.write("</Folder>\n");
      }

      out.write("</Document>\n");
      out.write("</kml>\n");
      out.close();
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
    }
  }
}
