// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import edu.gatech.lbs.core.world.IWorld;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.scheduling.event.SimEvent;
import edu.gatech.lbs.sim.scheduling.event.TraceLoadEvent;

//TODO: see http://java.sun.com/j2se/1.4.2/docs/api/java/util/TreeSet.html
//			http://java.sun.com/j2se/1.4.2/docs/api/java/util/TreeMap.html
public class SimEventQueue {
  private SimEventQueueElement head;
  private int elementCount;
  private int treeDepth;

  private final int loadBatchSize = (int) 1e5; // max number of events to load at once

  private HashMap<Byte, Class<?>> simEventTypes; // typeCode -> SimEvent subclass

  public SimEventQueue() {
    head = new SimEventQueueElement();
    elementCount = 0;
    treeDepth = 1;

    simEventTypes = new HashMap<Byte, Class<?>>();
  }

  public SimEventQueue(Simulation sim, DataInputStream in, IWorld world) throws IOException {
    this();
    loadSome(sim, in);
  }

  public void setLoadableSimEvents(List<Class<?>> eventClasses) {
    simEventTypes.clear();

    for (Class<?> eventClass : eventClasses) {
      try {
        Field typeCodeField = eventClass.getDeclaredField("typeCode");
        byte typeCode = typeCodeField.getByte(null);
        if (simEventTypes.containsKey(typeCode)) {
          System.out.println("Duplicate simulation event type code='" + typeCode + "'.");
          System.exit(-1);
        }
        simEventTypes.put(typeCode, eventClass);
        continue;

      } catch (NoSuchFieldException e) {
        // proceed to failure
      } catch (IllegalAccessException e) {
        // proceed to failure
      }

      System.out.println("Unknown simulation event class: " + eventClass.getName());
      System.exit(-1);
    }
  }

  public void loadSome(Simulation sim, DataInputStream in) throws IOException {
    try {
      SimEvent event = null;
      for (int eventsLoaded = 0; eventsLoaded < loadBatchSize; eventsLoaded++) {
        byte typeCode = in.readByte();

        Class<?> simEventClass = simEventTypes.get(typeCode);
        if (simEventClass != null) {
          try {
            Class<?>[] argTypes = new Class[] { Simulation.class, DataInputStream.class };
            Constructor<?> con = simEventClass.getConstructor(argTypes);
            event = (SimEvent) con.newInstance(new Object[] { sim, in });

            addEvent(event);
            continue;
          } catch (NoSuchMethodException e) {
            // proceed to failure
          } catch (InvocationTargetException e) {
            // proceed to failure
          } catch (IllegalAccessException e) {
            // proceed to failure
          } catch (InstantiationException e) {
            // proceed to failure
          }
        }

        System.out.println("Unknown simulation event: type code='" + (char) typeCode + "', class=" + (simEventClass == null ? "" : simEventClass.getName()));
        System.exit(-1);
      }

      // if events remain on disk, schedule a trace load:
      addEvent(new TraceLoadEvent(sim, event.getTimestamp(), in));
    } catch (EOFException e) {
      // do not schedule trace load, since end-of-file reached
    }
  }

  /**
   * Add the event into the queue.
   */
  public void addEvent(SimEvent event) {

    // count most-significant-bit offset:
    int b = 1;
    long t = event.getTimestamp();
    while ((t = (t >> 1)) != 0) {
      b++;
    }

    // increase tree depth, if bitstring of new timestamp has high '1' bits:
    if (elementCount > 0) {
      for (; treeDepth < b; treeDepth++) {
        SimEventQueueElement p2 = new SimEventQueueElement();
        p2.setLeaf(0, head);
        // assure new tree head is also the list head:
        p2.next = head.next;
        head.next = null;
        // set new head:
        head = p2;
      }
    }

    // find path in tree:
    SimEventQueueElement p0 = head;
    for (int i = treeDepth - 1; i >= -1; i--) {
      if (p0.isTerminalLeaf()) {
        // if found events with desired timestamp:
        if (p0.getTimestamp() == event.getTimestamp()) {
          // add into list of events with same timestamp, according to priority-based ordering:
          // highest priority:
          if (event.getPriority() <= p0.events.getFirst().getPriority()) {
            p0.events.addFirst(event);
          }
          // lowest priority:
          else if (event.getPriority() >= p0.events.getLast().getPriority()) {
            p0.events.addLast(event);
          }
          // other priority:
          else {
            // seek from the end:
            ListIterator<SimEvent> it;
            boolean found = false;
            for (it = p0.events.listIterator(p0.events.size()); it.hasPrevious() && !found;) {
              // step backwards as long as priority is too low:
              if (event.getPriority() >= it.previous().getPriority()) {
                found = true;
              }
            }
            // step forward, to just after the element:
            if (found && it.hasNext()) {
              it.next();
            }
            it.add(event);
          }

          elementCount++;
          return;
        }
        // if terminal leaf on path, extend with a new intermediate leaf & push down terminal leaf:
        else {
          SimEventQueueElement p2 = new SimEventQueueElement();
          p0.parent.setLeaf(p0.parent.getLeafIndex(p0), p2);
          p2.setLeaf((int) ((p0.getTimestamp() >> i) & 1), p0);
          p0 = p2;
        }
      }

      assert (i >= 0);

      // move on path:
      int dir = (int) ((event.getTimestamp() >> i) & 1);
      SimEventQueueElement p1 = p0.getLeaf(dir);

      // if leaf doesn't exist yet, create it, and dump object there:
      if (p1 == null) {
        p1 = new SimEventQueueElement(event);
        p0.setLeaf(dir, p1);

        // insert new element in linked list:
        SimEventQueueElement p2 = getPreviousElement(p1);
        p1.next = p2.next;
        p2.next = p1;

        elementCount++;
        return;
      }
      // move down:
      else {
        p0 = p1;
      }
    }

  }

  private SimEventQueueElement getPreviousElement(SimEventQueueElement leaf) {
    if (elementCount == 0) {
      return head;
    }

    // go up the tree, while on left-branch:
    SimEventQueueElement p0, p1;
    for (p1 = leaf, p0 = p1.parent; p0 != null && (p0.getLeafIndex(p1) == 0 || p0.getFullLeavesCount() < 2); p1 = p0, p0 = p0.parent) {
    }

    // no previous element:
    if (p0 == null) {
      return head;
    }

    // get over into left branch:
    p0 = p0.getLeaf(0);
    // go down:
    while (true) {
      // if found an element on the path, we're done:
      if (p0.isTerminalLeaf()) {
        return p0;
      } else {
        // try to go right...
        p1 = p0.getLeaf(1);
        if (p1 != null) {
          p0 = p1;
        }
        // ...otherwise go left:
        else {
          p0 = p0.getLeaf(0);
        }
      }
    }

  }

  public void addQueue(SimEventQueue queue) {
    if (queue.size() == 0) {
      return;
    }

    for (SimEventQueueElement element = queue.head.next; element != null; element = element.next) {
      for (SimEvent event : element.events) {
        addEvent(event);
      }
    }
  }

  public void clear() {
    elementCount = 0;
    head = new SimEventQueueElement();
  }

  /*public SimEventQueue clone() {
  	SimEventQueue clone = new SimEventQueue();
  	for (SimEventQueueElement p0 = head.next; p0 != null; p0 = p0.next) {
  		for (SimEvent event : p0.events) {
  			clone.addEvent(event);
  		}
  	}
  	return clone;
  }*/

  public long getNextEventTime() {
    if (elementCount > 0) {
      return head.next.getTimestamp();
    } else {
      return -1;
    }
  }

  public SimEvent executeNextEvent() {
    SimEvent event = pop();
    if (event != null) {
      event.execute();
      event = null;
      return event;
    } else {
      return null;
    }
  }

  public void saveTo(DataOutputStream out) throws IOException {
    for (SimEventQueueElement element = head.next; element != null; element = element.next) {
      for (SimEvent event : element.events) {
        event.saveTo(out);
      }
    }
  }

  public int size() {
    return elementCount;
  }

  public SimEvent pop() {
    /*
    //element counting for debugging:
    int i = 0;
    for (SimEventQueueElement p0 = head.next; p0 != null; i += p0.events.size(), p0 = p0.next)
    	;
    if (i != elementCount) {
    	System.out.println(i + " vs. " + elementCount);
    	System.exit(-1);
    }*/

    if (elementCount > 0) {
      SimEvent event = head.next.events.poll();
      // System.out.println(event);

      // if the list of events for this timestamp has become empty:
      if (head.next.events.isEmpty()) {
        // remove from tree:
        SimEventQueueElement p0 = head.next;
        do {
          p0.parent.removeLeaf(p0);
          p0 = p0.parent;
        } while (p0.getFullLeavesCount() == 0 && p0 != head);
        // NOTE: this allows that a single element is at the end of a chain of leaves (permissible),
        // but makes sure there is no chain of elements with only NULLs

        // remove from linked list:
        head.next = head.next.next;
      }

      elementCount--;
      return event;
    } else {
      return null;
    }
  }
}
