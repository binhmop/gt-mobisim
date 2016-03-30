Simulator for generating mobility traces and query traces for large numbers of mobile agents moving in a road network.

### How to get gt-mobisim, along with source code, sample simulation configs & maps? ###
  * [QuickStart](QuickStart.md)
  * [FAQ](FAQ.md)

### Example codes for using & extending the simulator ###
  * [ExampleCode](ExampleCode.md)


## Demo ##
Playback of pre-generated simulation with <a href='http://code.google.com/p/gt-mobisim/source/browse/trunk/configs/web-demo.xml'>this configuration xml</a>.

<a href='http://gt-mobisim.googlecode.com/svn/trunk/jnlp/gtmobisim.jnlp'>
<img src='http://gt-mobisim.googlecode.com/svn/trunk/jnlp/img/button0.png' />
</a>
<br>
<img src='http://gt-mobisim.googlecode.com/svn/trunk/jnlp/img/screenshot.png' /><br>
<br>
<table><thead><th> <a href='http://www.youtube.com/watch?feature=player_embedded&v=zGChn4nzyEk' target='_blank'><img src='http://img.youtube.com/vi/zGChn4nzyEk/0.jpg' width='425' height=344 /></a> </th><th> <a href='http://www.youtube.com/watch?feature=player_embedded&v=BICOHufjtkQ' target='_blank'><img src='http://img.youtube.com/vi/BICOHufjtkQ/0.jpg' width='425' height=344 /></a> </th></thead><tbody></tbody></table>


<h2>Features</h2>
<h3>Does:</h3>
<ul><li>generate mobility traces for mobile objects moving in a road network<br>
<ul><li>100k objects, 10 minutes, flowing traffic speeds, velocity step-function representation, continuous time (no sampling) => ~235 MB trace<br>
</li></ul></li><li>simulation driven by an xml configuration file<br>
<ul><li>can be run as a text-mode only batch job (eg. on ssh-access-only Linux box)<br>
</li><li>simple GUI, when running on graphical terminal<br>
</li></ul></li><li>vector map formats:<br>
<ul><li>ESRI Shapefile (.shp), eg. U.S. Census Bureau, TIGER/Line Shapefiles <a href='http://www.census.gov/geo/www/tiger/'>http://www.census.gov/geo/www/tiger/</a>
</li><li><code>GlobalMapper</code>-exported USGS data (.svg), see: <a href='http://edc2.usgs.gov/geodata/index.php'>http://edc2.usgs.gov/geodata/index.php</a>
</li></ul></li><li>various mobility models on road networks<br>
<ul><li>random waypoint<br>
</li><li>random trip<br>
</li></ul></li><li>3 ways to represent continuous-time traces<br>
<ul><li>location step-function<br>
</li><li>velocity step-function<br>
</li><li>acceleration step-function<br>
</li></ul></li><li>locations of mobile objects at any time instance (ie. continuous vs. sampled locations)<br>
</li><li>conversion from continuous-time traces to periodically sampled location traces<br>
</li><li>ability to specify various parameter distributions<br>
<ul><li>Gaussian distribution with mean & standard deviation + min & max cutoff<br>
</li><li>normal distribution with min & max<br>
</li></ul></li><li>generate query traces<br>
<ul><li>query creation<br>
</li><li>query deletion</li></ul></li></ul>

<h3>Doesn't do:</h3>
<ul><li>execution of client-side codes (ie. code that runs on simulated mobile users' phones)<br>
</li><li>have a simulated server, or have simulated client-server communication