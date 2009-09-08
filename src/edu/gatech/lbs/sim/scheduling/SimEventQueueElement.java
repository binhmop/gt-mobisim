// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling;

import java.util.LinkedList;

import edu.gatech.lbs.sim.scheduling.event.SimEvent;

public class SimEventQueueElement {
  protected LinkedList<SimEvent> events; // list of events with same timestamp

  protected SimEventQueueElement next; // navigation for linked list (next element)
  protected SimEventQueueElement parent; // navigation for tree (one parent)
  protected SimEventQueueElement[] leaves; // navigation for tree (two leaves)

  public SimEventQueueElement() {
    events = null;

    next = null;

    parent = null;

    leaves = new SimEventQueueElement[2];
    leaves[0] = null;
    leaves[1] = null;
  }

  public SimEventQueueElement(SimEvent event) {
    events = new LinkedList<SimEvent>();
    events.add(event);

    next = null;

    parent = null;

    leaves = null;
  }

  public boolean isTerminalLeaf() {
    return (leaves == null);
  }

  public SimEventQueueElement getLeaf(int dir) {
    return dir == 0 ? leaves[0] : leaves[1];
  }

  public int getLeafIndex(SimEventQueueElement leaf) {
    return leaf == leaves[0] ? 0 : (leaf == leaves[1] ? 1 : -1);
  }

  public void setLeaf(int dir, SimEventQueueElement leaf) {
    leaves[dir == 0 ? 0 : 1] = leaf;
    leaf.parent = this;
  }

  public void removeLeaf(SimEventQueueElement leaf) {
    leaves[getLeafIndex(leaf)] = null;
  }

  public int getFullLeavesCount() {
    return (leaves[0] == null ? 0 : 1) + (leaves[1] == null ? 0 : 1);
  }

  public long getTimestamp() {
    return events.peek().getTimestamp();
  }

}
