// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import edu.gatech.lbs.core.FileHelper;
import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.world.BoundingBox;
import edu.gatech.lbs.core.world.roadnet.ClassedRoadMap;
import edu.gatech.lbs.core.world.roadnet.ClassedRoadSegment;
import edu.gatech.lbs.core.world.roadnet.RoadJunction;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;

public class ShpMapParser extends MapParser {

  // see:
  // http://svn.osgeo.org/geotools/trunk/demo/example/src/main/java/org/geotools/demo/FirstProject.java
  // GeoTools jar dependency tree:
  // http://www.geotools.org/quickstart.html
  //
  // For TIGER/Line shapefile MTFCC code interpretation, see:
  // http://www.census.gov/geo/www/tiger/cfcc_to_mtfcc.xls
  public void load(String filename, RoadMap roadmap) {
    junctionMap.clear();
    BoundingBox bounds = roadmap.getBounds();
    boolean isClassed = (roadmap instanceof ClassedRoadMap);

    try {
      Map<String, Serializable> connectParameters = new HashMap<String, Serializable>();

      connectParameters.put("url", FileHelper.getAsUrl(filename));
      connectParameters.put("create spatial index", false);

      DataStore dataStore = DataStoreFinder.getDataStore(connectParameters);
      if (dataStore == null) {
        Logger.getLogger(ShpMapParser.class.getName()).log(Level.WARNING, "No DataStore found to handle" + filename);
        System.exit(1);
      }

      String[] typeNames = dataStore.getTypeNames();
      String typeName = typeNames[0];

      System.out.print("(content " + typeName + ") ");

      FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
      FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures();
      FeatureIterator<SimpleFeature> iterator = collection.features();

      double unit = 1e6 * 40075 / 360; // [mm/degree]
      try {
        while (iterator.hasNext()) {
          SimpleFeature feature = iterator.next();

          int roadClassIndex = -1;
          if (isClassed) {
            String mtfccCode = feature.getAttribute("MTFCC").toString();
            roadClassIndex = ((ClassedRoadMap) roadmap).getRoadClassIndex(mtfccCode);
            if (roadClassIndex < 0) {
              continue;
            }
          }

          Geometry geometry = (Geometry) feature.getDefaultGeometry();
          Coordinate[] coords = geometry.getCoordinates();
          ArrayList<CartesianVector> points = new ArrayList<CartesianVector>();
          for (int i = 0; i < coords.length; i++) {
            CartesianVector loc = new CartesianVector((long) (coords[i].x * unit), (long) (coords[i].y * unit));
            points.add(loc);
            bounds.includePoint(loc.getX(), loc.getY());
          }
          RoadJunction[] junctions = getJunctions(points);

          // create segment:
          CartesianVector[] pointArray = new CartesianVector[points.size()];
          points.toArray(pointArray);
          RoadSegment seg;
          if (isClassed) {
            seg = new ClassedRoadSegment(roadmap.getNextSegmentId(), junctions[0], junctions[1], false, pointArray, ((ClassedRoadMap) roadmap).getSpeedLimit(roadClassIndex), roadClassIndex);
          } else {
            seg = new RoadSegment(roadmap.getNextSegmentId(), junctions[0], junctions[1], false, pointArray, Integer.MAX_VALUE);
          }
          roadmap.addRoadSegment(seg);
        }
      } finally {
        if (iterator != null) {
          iterator.close();
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(-1);
    }
  }
}
