# gt-mobisim
Simulator for generating mobility traces and query traces for large numbers of mobile agents moving in a road network.

Modified from @pestip's original repo: https://code.google.com/archive/p/gt-mobisim/

Documentation is under `wiki` branch

## QuickStart

### Prerequisites

* Java 1.7+
* Maven 3.2+

### Build

```
$git clone https://github.com/binhmop/gt-mobisim.git 

$cd gt-mobisim

$mvn clean compile package
```
This will create an executable jar at `target/gt-mobisim-1.1-jar-with-dependencies.jar`

### Run in Eclipse
* Import gt-mobisim project into Eclipse

`Eclipse -> File -> Import -> Existing Maven Projects`

* Choose `Run Configurations`, select `gt-mobisim/edu.gatech.lbs.sim.Simulation` as the Main class. On the Arguments tab, put `configs/web-demo.xml` as Program arguments then run the Simulation.  

### Run jar

```
$java -jar target/gt-mobisim-1.1-jar-with-dependencies.jar configs/test-tracegen.xml
```
Note that you can use any of the xml configuration files under `configs` directory as the program argument. You can modify the attribute values in a config file to run different simulations. During the simulation, the mobility traces are output to `configs/traces` directory.

### Disclaimer
This software is published for academic and non-commercial use only.
