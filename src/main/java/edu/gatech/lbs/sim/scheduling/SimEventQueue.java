// Copyright (c) 2012, Georgia Tech Research Corporation
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
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import edu.gatech.lbs.core.world.IWorld;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.scheduling.event.SimEvent;
import edu.gatech.lbs.sim.scheduling.event.TraceLoadEvent;

public class SimEventQueue {
  private final int loadBatchSize = (int) 1e5; // max number of events to load at once

  private TreeMap<Long, LinkedList<SimEvent>> eventMap; // (time|priority) -> events
  private int elementCount;

  private HashMap<Byte, Class<?>> simEventTypes; // typeCode -> SimEvent subclass

  public SimEventQueue() {
    eventMap = new TreeMap<Long, LinkedList<SimEvent>>();
    elementCount = 0;
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

  public void addEvent(SimEvent event) {
    Long l = (event.getTimestamp() << 16) | (event.getPriority() & 0xFFFF);
    LinkedList<SimEvent> eventsAtTimeAndPriority = eventMap.get(l);
    if (eventsAtTimeAndPriority == null) {
      eventsAtTimeAndPriority = new LinkedList<SimEvent>();
      eventMap.put(l, eventsAtTimeAndPriority);
    }
    // events within time&priority are in order of insertion:
    eventsAtTimeAndPriority.addLast(event);
    elementCount++;
  }

  public SimEvent pop() {
    if (eventMap.isEmpty()) {
      return null;
    }
    LinkedList<SimEvent> eventsAtTimeAndPriority = eventMap.firstEntry().getValue();
    SimEvent event = eventsAtTimeAndPriority.poll();
    if (eventsAtTimeAndPriority.isEmpty()) {
      eventMap.pollFirstEntry();
    }
    elementCount--;
    return event;
  }

  public void addQueue(SimEventQueue queue) {
    if (queue.size() == 0) {
      return;
    }

    for (LinkedList<SimEvent> element : queue.eventMap.values()) {
      for (SimEvent event : element) {
        addEvent(event);
      }
    }
  }

  public void clear() {
    eventMap.clear();
  }

  public long getNextEventTime() {
    if (eventMap.isEmpty()) {
      return -1;
    } else {
      return eventMap.firstEntry().getValue().peek().getTimestamp();
    }
  }

  public void executeNextEvent() {
    SimEvent event = pop();
    if (event != null) {
      event.execute();
    }
  }

  public void saveTo(DataOutputStream out) throws IOException {
    for (LinkedList<SimEvent> element : eventMap.values()) {
      for (SimEvent event : element) {
        event.saveTo(out);
      }
    }
  }

  public int size() {
    return elementCount;
  }
}
