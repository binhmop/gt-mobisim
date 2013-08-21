// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config.helper;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.config.IXmlConfigInterpreter;
import edu.gatech.lbs.sim.config.paramparser.IParamParser;
import edu.gatech.lbs.sim.tracegenerator.paramdistribution.ClassedRoadnetSpeedDistribution;
import edu.gatech.lbs.sim.tracegenerator.paramdistribution.GaussianParamDistribution;
import edu.gatech.lbs.sim.tracegenerator.paramdistribution.IParamDistribution;
import edu.gatech.lbs.sim.tracegenerator.paramdistribution.UniformParamDistribution;

public class XmlParamDistributionInterpreter implements IXmlConfigInterpreter {
  protected IParamParser pparser;
  protected IParamDistribution paramDistribution;

  public XmlParamDistributionInterpreter(IParamParser pparser) {
    this.pparser = pparser;
  }

  public IParamDistribution getParamDistribution() {
    return paramDistribution;
  }

  public void initFromXmlElement(Element paramDistributionNode, Simulation sim) {
    paramDistribution = null;

    if (paramDistributionNode == null) {
      return;
    }

    String paramDistributionType = paramDistributionNode.getAttribute("type");

    if (paramDistributionType.equalsIgnoreCase(UniformParamDistribution.xmlName)) {
      String maxStr = paramDistributionNode.getAttribute("max");
      int max = maxStr.length() > 0 ? pparser.parse(maxStr) : Integer.MAX_VALUE;

      String minStr = paramDistributionNode.getAttribute("min");
      int min = minStr.length() > 0 ? pparser.parse(minStr) : Integer.MIN_VALUE;

      paramDistribution = new UniformParamDistribution(min, max);

    } else if (paramDistributionType.equalsIgnoreCase(GaussianParamDistribution.xmlName)) {
      int mean = pparser.parse(paramDistributionNode.getAttribute("mean"));
      int dev = pparser.parse(paramDistributionNode.getAttribute("stdev"));

      String maxStr = paramDistributionNode.getAttribute("max");
      int max = maxStr.length() > 0 ? pparser.parse(maxStr) : Integer.MAX_VALUE;

      String minStr = paramDistributionNode.getAttribute("min");
      int min = minStr.length() > 0 ? pparser.parse(minStr) : 0;

      paramDistribution = new GaussianParamDistribution(mean, dev, min, max);

    } else if (paramDistributionType.equalsIgnoreCase(ClassedRoadnetSpeedDistribution.xmlName)) {
      NodeList classNodes = paramDistributionNode.getElementsByTagName("class");
      int classCount = classNodes.getLength();
      IParamDistribution[] speedDistributions = new IParamDistribution[classCount];

      for (int roadclass = 0; roadclass < classCount; roadclass++) {
        Element classNode = (Element) classNodes.item(roadclass);
        XmlParamDistributionInterpreter interpreter = new XmlParamDistributionInterpreter(pparser);
        interpreter.initFromXmlElement(classNode, sim);
        speedDistributions[roadclass] = interpreter.getParamDistribution();
      }
      paramDistribution = new ClassedRoadnetSpeedDistribution(speedDistributions);

    } else {
      System.out.println("Unknown speed distribution type: " + paramDistributionType);
      System.exit(-1);
    }
  }
}
