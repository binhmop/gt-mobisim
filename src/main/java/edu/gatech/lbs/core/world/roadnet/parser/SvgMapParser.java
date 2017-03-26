// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import edu.gatech.lbs.core.FileHelper;
import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.world.BoundingBox;
import edu.gatech.lbs.core.world.roadnet.ClassedRoadMap;
import edu.gatech.lbs.core.world.roadnet.ClassedRoadSegment;
import edu.gatech.lbs.core.world.roadnet.RoadJunction;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;

/**
 * Load a classed roadmap from a GlobalMapper-exported .SVG file.
 * Currently only undirected graphs are supported.
 * 
 */
public class SvgMapParser extends MapParser {

  public void load(String filename, RoadMap roadmap) {
    junctionMap.clear();

    try {
      InputStream in = FileHelper.openFileOrUrl(filename);
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
      Element rootNode = doc.getDocumentElement();
      BoundingBox bounds = roadmap.getBounds();

      Element gNode = (Element) rootNode.getElementsByTagName("g").item(0);
      NodeList roadNodes = gNode.getElementsByTagName("g");
      for (int i = 0; i < roadNodes.getLength(); i++) {
        Element roadNode = (Element) roadNodes.item(i);
        int roadClassIndex = ((ClassedRoadMap) roadmap).getRoadClassIndex(roadNode.getAttribute("id"));

        if (roadClassIndex != -1) {
          NodeList polylineNodes = roadNode.getElementsByTagName("polyline");
          for (int j = 0; j < polylineNodes.getLength(); j++) {
            Element polylineNode = (Element) polylineNodes.item(j);
            String pointsStr = polylineNode.getAttribute("points");

            StringTokenizer st = new StringTokenizer(pointsStr);
            List<CartesianVector> points = new ArrayList<CartesianVector>();
            while (st.hasMoreTokens()) {
              String value = st.nextToken();
              int idx = value.indexOf(',');
              // see: GlobalMapper8//Tools/Configuration/Projection: set UTM, NAD83, METERS
              long x = (long) (1000 * Double.parseDouble(value.substring(0, idx))); // parsed as [m]
              long y = (long) (1000 * Double.parseDouble(value.substring(idx + 1))); // parsed as [m]
              points.add(new CartesianVector(x, y));

              bounds.includePoint(x, y);
            }

            RoadJunction[] junctions = getJunctions(points);

            // create segment:
            // for undirected graphs, only one segment is created, which can be traversed back and forth
            CartesianVector[] pointArray = new CartesianVector[points.size()];
            points.toArray(pointArray);
            RoadSegment seg = new ClassedRoadSegment(roadmap.getNextSegmentId(), junctions[0], junctions[1], false, pointArray, ((ClassedRoadMap) roadmap).getSpeedLimit(roadClassIndex), roadClassIndex);
            roadmap.addRoadSegment(seg);
          }
        }
      }
      in.close();

    } catch (IOException e) {
      System.out.println("\n" + e);
      System.exit(-1);
    } catch (SAXParseException e) {
      System.out.println("Parsing error in " + filename + ", line " + e.getLineNumber());
      System.out.println(" " + e.getMessage());
      System.exit(-1);
    } catch (SAXException e) {
      System.out.println("SAXException");
      Exception x = e.getException();
      ((x == null) ? e : x).printStackTrace();
      System.exit(-1);
    } catch (ParserConfigurationException e) {
      System.out.println("ParserConfigurationException");
      System.exit(-1);
    }
  }
}