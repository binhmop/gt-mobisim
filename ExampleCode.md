# Example 1: How do I get the locations of agents at time t? #

An example program:
  * http://code.google.com/p/gt-mobisim/source/browse/trunk/src/edu/gatech/lbs/sim/examples/ExampleAgentLocations.java

The command line to run this example is in _configs/runjarex1.bat_:
  * http://gt-mobisim.googlecode.com/svn/trunk/configs/runjarex1.bat


# Example 2: How do I get the list of agents on a segment at time t? #

An example program:
  * http://code.google.com/p/gt-mobisim/source/browse/trunk/src/edu/gatech/lbs/sim/examples/ExampleAgentsOnSegment.java

The command line to run this example is in _configs/runjarex2.bat_:
  * http://gt-mobisim.googlecode.com/svn/trunk/configs/runjarex2.bat


# Example 3: How do I do X (eg. inspect the locations of agents) every t seconds? #

An example program:
  * http://code.google.com/p/gt-mobisim/source/browse/trunk/src/edu/gatech/lbs/sim/examples/ExamplePeriodicLocations.java

The command line to run this example is in _configs/runjarex3.bat_:
  * http://gt-mobisim.googlecode.com/svn/trunk/configs/runjarex3.bat


# Advanced example: How do I get the simulator to do X (eg. inspect the locations of agents) at periodic simulated time intervals? #
For the "inspect the locations of agents" part, see the previous examples. Here we are only concerned with extending the simulator to do something periodically. If you have some actions that need to be executed repeatedly at time intervals that are agent-dependent, and maybe not even periodic (such as a simulated client `.sleep()`), the simple solution in Example 3 will not be sufficient. We look at the steps to extend the simulator to do X at periodic time intervals here; introducing a non-constant time interval would be an even further step.

  1. First, create your own event (a `SimEvent` subclass). This event will do X, when it is scheduled for execution by the simulator, and then reschedule itself.
```
package edu.gatech.lbs.sim.scheduling.event;
public class MyEvent extends SimEvent {
  private double period; // [sec]

  public MyEvent(Simulation sim, long timestamp, double period) {
    super(sim, timestamp);
    this.period = period;
  }

  public void execute() {
    // do X here

    // reschedule for next execution:
    sim.addEvent(new MyEvent(sim, timestamp + (long) (1000 * period), period));
  }

  ...
}
```
  1. Second, create your own activity (an `ISimActivity` implementation), which will schedule the first periodic event. (Alternatively, you could modify an existing `ISimActivity` implementation.)
```
package edu.gatech.lbs.sim.scheduling.activity;
public class MyActivity implements ISimActivity {
  protected double period; // [sec]

  public MyActivity(double period) {
    this.period = period;
  }

  public void scheduleOn(Simulation sim) {
    sim.addEvent(new MyEvent(sim, sim.getSimStartTime(), period));
  }

  public void cleanup() {
  }
}
```
  1. Third, create an interpreter (an `IXmlConfigInterpreter` implementation) to read the configuration parameters from a new section of the config xml. (Alternatively, you could modify an existing `IXmlConfigInterpreter` implementation. If you don't need any parameters that you need specified in the config xml, you could choose to modify `NullInterpreter`, and then skip the remaining steps, since that interpreter is already invoked by `Simulation`.)
```
package edu.gatech.lbs.sim.config;
public class XmlMyConfigInterpreter implements IXmlConfigInterpreter {
  // interprets the following config xml tag: <mystuff period="90 sec"/>
  public void initFromXmlElement(Element rootNode, Simulation sim) {
    Element myNode = (Element) rootNode.getElementsByTagName("mystuff").item(0);
    double period = new TimeParser().parse(myNode.getAttribute("period"));
    sim.addActivity(new MyActivity(period));
  }
}
```
  1. Fourth, subclass `Simulation` and override `getConfigInterpreters()` to include your own `MyConfigInterpreter` among the interpreters that are unleashed on the config xml. (Alternatively, you could modify the existing `Simulation`.)
```
package edu.gatech.lbs.sim;
public class MySimulation extends Simulation {
  @Override
  protected Collection<IXmlConfigInterpreter> getConfigInterpreters() {
    Collection<IXmlConfigInterpreter> interpreters = new LinkedList<IXmlConfigInterpreter>();
    interpreters.add(new XmlTimesConfigInterpreter());
    interpreters.add(new XmlWorldConfigInterpreter());
    interpreters.add(new XmlAgentsConfigInterpreter());
    interpreters.add(new XmlMyConfigInterpreter()); // this is the new stuff, the rest is for the usual parts of the config xml
    interpreters.add(new NullInterpreter());
    return interpreters;
  }
}
```
  1. Finally, instantiate `MySimulation` where you would have instantiated `Simulation` before, but otherwise use the same as before.
```
  public static void main(String[] args) {
    Simulation sim = new MySimulation();
    sim.loadConfiguration(args[0]);
    sim.initSimulation();
    sim.runSimulation();
    sim.endSimulation();
  }
```