# How to get _gt-mobisim_, along with source code, sample simulation configs & maps? #

  1. Download and unzip Java SDK: http://java.sun.com/javase/downloads/index.jsp, eg. choose _JDK 6 Update 16_
  1. Choose:
    1. Development with Eclipse:
      1. Download and unzip Eclipse: http://www.eclipse.org/downloads/, eg. choose _Eclipse IDE for Java Developers_
      1. Get gt-mobisim by choosing one of these 2 methods:
        1. From Google Code SVN source depot:
          1. Install Subclipse SVN plugin for Eclipse: http://subclipse.tigris.org/servlets/ProjectProcess?pageID=p4wYuA
          1. In Eclipse:
            * _File/New/Other.../SVN/Checkout projects from SVN_
            * Select _Create new repository location_
            * _URL_: `http://gt-mobisim.googlecode.com/svn` (for non-read-only access, use `https` here)
            * Select _trunk_ folder
            * Select _Check out as a project configured using the New Project Wizard_
            * Select _Java Project_, and name it _gt-mobisim_
            * more instructions: http://blog.msbbc.co.uk/2007/06/using-googles-free-svn-repository-with.html
        1. Without source depot:
          1. Download and unpack the latest zip into a _gt-mobisim_ directory in your Eclipse workspace: http://code.google.com/p/gt-mobisim/downloads/list
          1. In Eclipse: _File/Import.../General/Existing Projects into Workspace_
      1. Create a run target in Eclipse:
        1. Select _gt-mobisim/edu.gatech.lbs.sim.Simulation_, then right click/_Run As/Run Configurations..._
        1. on the _Arguments_ tab:
          1. _Program arguments:_ `web-demo.xml`
          1. _VM arguments:_ `-Xmx500M` (increase for simulations with many agents)
          1. _Working directory: Other:_ `${workspace_loc:gt-mobisim/configs}`
          1. _Run_
    1. Just use the ready-made _gtmobisim.jar_ (no Eclipse):
      1. Download and unpack the latest zip into a _gt-mobisim_ directory: http://code.google.com/p/gt-mobisim/downloads/list
      1. In the _configs_ subdirectory run:<br> <code>java -Xmx500M -cp "../thirdparty/*.jar" -jar ../gtmobisim.jar web-demo.xml</code> <br> You could also try <code>runjar.bat web-demo.xml</code> for the same result. <br> Also consider the example commands that run the <a href='ExampleCode.md'>ExampleCode</a>s