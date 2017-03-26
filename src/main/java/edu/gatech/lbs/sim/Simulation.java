// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import edu.gatech.lbs.core.FileHelper;
import edu.gatech.lbs.core.logging.Logz;
import edu.gatech.lbs.core.logging.Varz;
import edu.gatech.lbs.core.query.LocationBasedQuery;
import edu.gatech.lbs.core.query.QueryKey;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.world.IWorld;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.config.IXmlConfigInterpreter;
import edu.gatech.lbs.sim.config.NullInterpreter;
import edu.gatech.lbs.sim.config.XmlAgentsConfigInterpreter;
import edu.gatech.lbs.sim.config.XmlTimesConfigInterpreter;
import edu.gatech.lbs.sim.config.XmlWorldConfigInterpreter;
import edu.gatech.lbs.sim.scheduling.SimEventQueue;
import edu.gatech.lbs.sim.scheduling.activity.ISimActivity;
import edu.gatech.lbs.sim.scheduling.event.AccelerationChangeEvent;
import edu.gatech.lbs.sim.scheduling.event.LocationChangeEvent;
import edu.gatech.lbs.sim.scheduling.event.QueryCreateEvent;
import edu.gatech.lbs.sim.scheduling.event.QueryDeleteEvent;
import edu.gatech.lbs.sim.scheduling.event.SimEvent;
import edu.gatech.lbs.sim.scheduling.event.VelocityChangeEvent;

public class Simulation {
  // event priorities from highest to lowest priority:
  public static int priorityTraceLoadEvent = 10;
  public static int priorityAccelerationChangeEvent = 20;
  public static int priorityVelocityChangeEvent = 20;
  public static int priorityLocationChangeEvent = 20;
  public static int priorityPeriodicTraceSaveEvent = 30;
  public static int priorityQueryCreateEvent = 40;
  public static int priorityQueryRemoveEvent = 40;
  public static int priorityDrawGUIEvent = 1000;

  protected long simTime; // [ms], current time in simulation
  protected long simStartTime; // [ms], absolute time when simulation starts
  protected long simEndTime; // [ms], absolute time when simulation ends
  protected long simWarmupDuration; // [ms], time period of warmup, after which logging begins

  protected IWorld world;

  protected HashMap<Integer, SimAgent> agents; // simAgentId -> agent
  protected HashMap<Integer, List<SimAgent>> agentIndex; // segmentId --> SimAgent

  protected HashMap<QueryKey, LocationBasedQuery> queries; // simQueryKey -> query

  protected SimEventQueue eventQueue; // the simulation event queue
  protected Collection<ISimActivity> simActivities;

  public Simulation() {
    simActivities = new LinkedList<ISimActivity>();
    agents = new HashMap<Integer, SimAgent>();
  }

  public void setSimTimes(long simStartTime, long simEndTime, long simWarmupDuration) {
    this.simStartTime = simStartTime;
    this.simEndTime = simEndTime;
    this.simWarmupDuration = simWarmupDuration;
  }

  public long getSimStartTime() {
    return simStartTime;
  }

  public long getSimEndTime() {
    return simEndTime;
  }

  public long getSimWarmupDuration() {
    return simWarmupDuration;
  }

  public long getTime() {
    return simTime;
  }

  public void setTime(long simTime) {
    this.simTime = simTime;
  }

  public void setWorld(IWorld world) {
    this.world = world;
  }

  public IWorld getWorld() {
    return world;
  }

  public void setAgents(Collection<SimAgent> agents) {
    this.agents.clear();
    for (SimAgent agent : agents) {
      this.agents.put(agent.getSimAgentId(), agent);
    }
  }

  public SimAgent getAgent(int simAgentId) {
    return agents.get(simAgentId);
  }

  public Collection<SimAgent> getAgents() {
    return agents.values();
  }

  public List<SimAgent> getAgentsOnSegment(int segmentId) {
    return agentIndex.get(segmentId);
  }

  public int getAgentCount() {
    return agents.size();
  }

  public void updateAgentIndex(SimAgent agent, IVector newLocation) {
    IVector oldLocation = agent.getLocation();
    int oldSegmentId = oldLocation != null ? oldLocation.toRoadnetVector().getRoadSegment().getId() : -1;
    int newSegmentId = newLocation != null ? newLocation.toRoadnetVector().getRoadSegment().getId() : -1;

    if (oldSegmentId == newSegmentId) {
      return;
    }

    if (oldSegmentId >= 0) {
      agentIndex.get(oldSegmentId).remove(agent);
    }
    if (newSegmentId >= 0) {
      List<SimAgent> agentsOnSegment = agentIndex.get(newSegmentId);
      if (agentsOnSegment == null) {
        agentsOnSegment = new LinkedList<SimAgent>();
        agentIndex.put(newSegmentId, agentsOnSegment);
      }
      agentsOnSegment.add(agent);
    }
  }

  public void simulateAddQuery(QueryKey simKey, LocationBasedQuery query) {
    SimAgent agent = agents.get(simKey.uid);
    agent.simulateAddQuery(simKey, query);
    queries.put(simKey, query);
  }

  public void simulateRemoveQuery(QueryKey simKey) {
    SimAgent agent = agents.get(simKey.uid);
    queries.remove(simKey);
    agent.simulateRemoveQuery(simKey);
  }

  public SimEventQueue getQueue() {
    return eventQueue;
  }

  public void addEvent(SimEvent event) {
    eventQueue.addEvent(event);
  }

  public void addActivity(ISimActivity activity) {
    simActivities.add(activity);
  }

  /**
   * Loads the simulation's configuration parameters from the given xml file.
   */
  public void loadConfiguration(String configFilename) {
    try {
      InputStream in = FileHelper.openFileOrUrl(configFilename);
      String contents = FileHelper.getContentsFromInputStream(in);
      in.close();

      loadConfigurationFromSpecification(contents.toString());
    } catch (IOException e) {
      Logz.println("" + e);
      System.exit(-1);
    }
  }

  /**
   * Loads the simulation's configuration parameters from the given xml text.
   */
  public void loadConfigurationFromSpecification(String configText) {
    simActivities.clear();

    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(configText)));
      Element rootNode = doc.getDocumentElement();

      Collection<IXmlConfigInterpreter> interpreters = getConfigInterpreters();
      for (IXmlConfigInterpreter interpreter : interpreters) {
        Logz.println("Config interpreter: " + interpreter.getClass().getSimpleName());
        interpreter.initFromXmlElement(rootNode, this);
      }

    } catch (IOException e) {
      Logz.println("IOException");
      System.exit(-1);
    } catch (SAXParseException e) {
      Logz.println("Parsing error on line " + e.getLineNumber());
      Logz.println(" " + e.getMessage());
      System.exit(-1);
    } catch (SAXException e) {
      Logz.println("SAXException");
      Exception x = e.getException();
      ((x == null) ? e : x).printStackTrace();
      System.exit(-1);
    } catch (ParserConfigurationException e) {
      Logz.println("ParserConfigurationException");
      System.exit(-1);
    }
    Logz.println("Configuration loaded.\n");
  }

  /**
   * Override this method to customize what events are recognized as loadable from a trace file.
   */
  protected List<Class<?>> getLoadableTraceSimEvents() {
    List<Class<?>> loadableEvents = new LinkedList<Class<?>>();

    loadableEvents.add(LocationChangeEvent.class);
    loadableEvents.add(VelocityChangeEvent.class);
    loadableEvents.add(AccelerationChangeEvent.class);

    loadableEvents.add(QueryCreateEvent.class);
    loadableEvents.add(QueryDeleteEvent.class);

    return loadableEvents;
  }

  /**
   * Initializes the simulation.
   * This method is used to put the simulation in an initial, pre-run state, based on
   * the previously loaded configuration.
   */
  public void initSimulation() {
    eventQueue = new SimEventQueue();
    eventQueue.setLoadableSimEvents(getLoadableTraceSimEvents());

    agentIndex = new HashMap<Integer, List<SimAgent>>();
    queries = new HashMap<QueryKey, LocationBasedQuery>();

    simTime = simStartTime;

    for (ISimActivity activity : simActivities) {
      Logz.println("Scheduling activity: " + activity.getClass().getSimpleName());
      activity.scheduleOn(this);
    }
  }

  /**
   * Cleans up after the simulation.
   */
  public void endSimulation() {
    for (ISimActivity activity : simActivities) {
      activity.cleanup();
    }
  }

  /**
   * Runs the simulation from start to finish.
   */
  public void runSimulation() {
    // simulation loop:
    Logz.println("Running simulation... ");
    long wallStartTime = System.nanoTime();
    long eventsProcessed = 0;
    while ((simTime = eventQueue.getNextEventTime()) >= 0 && simTime < simEndTime) {
      eventQueue.executeNextEvent();
      eventsProcessed++;

      if (eventsProcessed % 1000000 == 1) {
        Logz.println(" Queue has " + eventQueue.size() + " events, simTime= " + String.format("%.1f", eventQueue.getNextEventTime() / (1000 * 60.0)) + " min, wallTime= " + String.format("%.1f", (System.nanoTime() - wallStartTime) / (1e9 * 60.0)) + " min");
      }
    }
    Varz.set("eventsProcessed", eventsProcessed); // for the whole simulation, including warmup
    Varz.set("wallRunTime", (System.nanoTime() - wallStartTime) / 1e9); // [sec], for the whole simulation, including warmup
    double simToWallSpeedRatio = (simEndTime - simStartTime) / ((System.nanoTime() - wallStartTime) / 1e6);
    Logz.println(" Speed: " + String.format("%.1f", simToWallSpeedRatio) + "x realtime (" + String.format("%.1f", simToWallSpeedRatio) + " simulated minutes/wall minute)");
    Logz.println("DONE.");

    Varz.set("simRunTime", (simEndTime - simStartTime - simWarmupDuration) / 1000.0); // [sec], without warmup
    Varz.set("agentCount", agents.size());
  }

  /**
   * Special-purpose simulation runner, that executes quietly and without logging, up to the exact
   * time specified. The primary purpose is to run the mobility-models, so the agent locations are
   * available immediately after the specified point in time.
   */
  public void runSimulationTo(long simEndTime) {
    while ((simTime = eventQueue.getNextEventTime()) >= 0 && simTime <= simEndTime) {
      eventQueue.executeNextEvent();
    }
    simTime = simEndTime;
  }

  /**
   * Provides the ordered set of configuration interpreters, which can understand a simulation
   * configuration file.
   * Override this method to customize how a config xml is interpreted & how the simulation is set up.
   */
  protected Collection<IXmlConfigInterpreter> getConfigInterpreters() {
    Collection<IXmlConfigInterpreter> interpreters = new LinkedList<IXmlConfigInterpreter>();
    interpreters.add(new XmlTimesConfigInterpreter());
    interpreters.add(new XmlWorldConfigInterpreter());
    interpreters.add(new XmlAgentsConfigInterpreter());
    interpreters.add(new NullInterpreter());
    return interpreters;
  }

  /**
   * Runs the simulation with the configuration file given as first argument.
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage:");
      System.out.println("  java " + Simulation.class + " config.xml");
      return;
    }

    Simulation sim = new Simulation();
    sim.loadConfiguration(args[0]);
    sim.initSimulation();
    sim.runSimulation();
    sim.endSimulation();
  }

}