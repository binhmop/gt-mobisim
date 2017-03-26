// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.querytrace;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import edu.gatech.lbs.core.query.LocationBasedQuery;
import edu.gatech.lbs.core.query.QueryKey;
import edu.gatech.lbs.core.query.ShortestRouteRangeQuery;
import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.scheduling.SimEventQueue;
import edu.gatech.lbs.sim.scheduling.activity.TraceLoadingActivity;
import edu.gatech.lbs.sim.scheduling.event.QueryCreateEvent;
import edu.gatech.lbs.sim.scheduling.event.QueryDeleteEvent;
import edu.gatech.lbs.sim.scheduling.event.SimEvent;
import edu.gatech.lbs.sim.tracegenerator.ITraceGenerator;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.ILocationDistribution;
import edu.gatech.lbs.sim.tracegenerator.paramdistribution.IParamDistribution;

public class GlobalFixedNumberRangeQueryModel implements ITraceGenerator {
  protected Simulation miniSim;

  protected IParamDistribution rangeDistribution;
  protected IParamDistribution lifetimeDistribution;
  protected ILocationDistribution locationDistribution;

  protected int queryCountTotal;

  /**
   * Max. query count per agent. Eg. if queryCount=10 & agentCount=10, have 1 query on 1 agent at all times.
   */
  protected int maxQueryCount;

  /**
   * simAgentId -> queries outstanding (for uniform distribution)
   * The id for an agent at max capacity should be removed, to ease finding free capacity.
   */
  protected TreeMap<Integer, Integer> queryCount;

  protected int nextSimQid = 0;

  protected Random rnd;

  public GlobalFixedNumberRangeQueryModel(String mobilityTraceFilename, Simulation sim, IParamDistribution rangeDistribution, IParamDistribution lifetimeDistribution, ILocationDistribution locationDistribution, int queryCount) {
    this.rangeDistribution = rangeDistribution;
    this.lifetimeDistribution = lifetimeDistribution;
    this.locationDistribution = locationDistribution;
    if (queryCount >= 0) {
      maxQueryCount = Integer.MAX_VALUE;
      queryCountTotal = queryCount;
    } else {
      double percent = Math.abs(queryCount) / 100.0; // interpret as percent of agents
      maxQueryCount = (int) Math.ceil(percent);
      queryCountTotal = (int) (sim.getAgentCount() * percent);
    }
    this.queryCount = new TreeMap<Integer, Integer>();

    miniSim = new Simulation();
    miniSim.setSimTimes(sim.getSimStartTime(), sim.getSimEndTime(), sim.getSimWarmupDuration());
    miniSim.setWorld(sim.getWorld());
    Collection<SimAgent> agents = new ArrayList<SimAgent>();
    for (SimAgent agent : sim.getAgents()) {
      agents.add(new SimAgent(miniSim, agent.getSimAgentId()));
      this.queryCount.put(agent.getSimAgentId(), 0);
    }
    miniSim.setAgents(agents);
    miniSim.addActivity(new TraceLoadingActivity(mobilityTraceFilename));

    rnd = new Random();
  }

  public void generateTrace(String traceFilename) throws IOException {
    miniSim.initSimulation();

    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(traceFilename)));

    long wallStartTime = System.nanoTime();

    SimEventQueue queue = new SimEventQueue();

    // ensure that initial locations are set:
    miniSim.runSimulationTo(0);
    // set initial queries:
    for (int i = 0; i < queryCountTotal; i++) {
      LocationBasedQuery lbq = new ShortestRouteRangeQuery(rangeDistribution.getNextValue(null));

      SimAgent agent = getOneAgent();
      addOneQuery(queue, miniSim.getSimStartTime(), -1, agent, lbq);
    }

    // process the queue, and add a query insertion for every query deletion, to keep number of queries constant:
    if (lifetimeDistribution != null) {
      SimEventQueue phantomQueue = new SimEventQueue();
      phantomQueue.addQueue(queue);
      long simTime;
      while ((simTime = phantomQueue.getNextEventTime()) >= 0 && simTime < miniSim.getSimEndTime()) {
        SimEvent event = phantomQueue.pop();
        if (event instanceof QueryDeleteEvent) {
          LocationBasedQuery lbq = new ShortestRouteRangeQuery(rangeDistribution.getNextValue(null));
          long t = event.getTimestamp() + 1;
          // ensure that locations are set:
          miniSim.runSimulationTo(t);

          // update number of queries for old agent:
          int oldSimAgentId = ((QueryDeleteEvent) event).getSimAgentId();
          Integer queryCnt = queryCount.get(oldSimAgentId);
          queryCnt = (queryCnt == null ? maxQueryCount : queryCnt) - 1;
          queryCount.put(oldSimAgentId, queryCnt);

          // add a new query to an agent:
          SimAgent agent = getOneAgent();
          long lifetime = addOneQuery(queue, t, -1, agent, lbq);
          addOneQuery(phantomQueue, t, lifetime, agent, lbq);
        }
      }
    }

    queue.saveTo(out);
    System.out.println("  " + queue.size() + " query events.");

    double simToWallSpeedRatio = (miniSim.getSimEndTime() - miniSim.getSimStartTime()) / ((System.nanoTime() - wallStartTime) / 1e6);
    System.out.println("  Speed: " + String.format("%.1f", simToWallSpeedRatio) + "x realtime (" + String.format("%.1f", simToWallSpeedRatio / 60.0) + " simulated hours/wall minute)");

    out.close();
  }

  private SimAgent getOneAgent() {
    SimAgent agent = null;
    while (agent == null) {
      // if no location distribution specified, follow the distribution of agents:
      if (locationDistribution == null) {
        int agentId = rnd.nextInt(miniSim.getAgentCount());
        agent = miniSim.getAgent(agentId);
      } else {
        RoadnetVector l = locationDistribution.getNextLocation().toRoadnetVector();
        List<SimAgent> agents = miniSim.getAgentsOnSegment(l.getRoadSegment().getId());
        if (agents != null && !agents.isEmpty()) {
          agent = agents.get(rnd.nextInt(agents.size()));
        }
      }
    }

    /**
     * If selected agent at max capacity, just get the first agent with free capacity. When a max. capacity
     * is defined, then we want an egalitarian distribution of queries, so all agents will have either
     * max or max-1 queries anyway; so this non-random selection is OK.
     */
    if (!queryCount.containsKey(agent.getSimAgentId())) {
      agent = miniSim.getAgent(queryCount.firstKey());
    }
    return agent;
  }

  private long addOneQuery(SimEventQueue queue, long t, long lifetime, SimAgent agent, LocationBasedQuery lbq) {
    int simAgentId = agent.getSimAgentId();
    QueryKey simKey = new QueryKey(simAgentId, nextSimQid);

    // query creation:
    queue.addEvent(new QueryCreateEvent(miniSim, t, simKey, lbq));
    // query deletion (only if lifetime is not infinite):
    if (lifetime < 0 && lifetimeDistribution != null) {
      lifetime = (long) (1000 * lifetimeDistribution.getNextValue(null));
    }
    if (lifetime > 0) {
      queue.addEvent(new QueryDeleteEvent(miniSim, t + lifetime, simKey));
    }

    // update number of queries for agent:
    int queryCnt = queryCount.get(simAgentId) + 1;
    if (queryCnt < maxQueryCount) {
      queryCount.put(simAgentId, queryCnt);
    } else {
      queryCount.remove(simAgentId);
    }

    nextSimQid++;
    return lifetime;
  }

  public static String getXmlName() {
    return "fixednumber_range";
  }
}
