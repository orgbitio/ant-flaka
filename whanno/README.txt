

__ Build from scratch  _________________________________________________________

Use build script 'build.xml' to build Flaka from scratch.

 $ ant -f build.xml

Notice: you need to satisfy the following requirements:

1. Java 1.4 and newer
2. Dependency Libraries (see below)
3. A resonable new version of Ant (anything newer than 1.7 may do
well).

__ Build dependencies __________________________________________________________

The following libraries are required:

* ant-1.7.1.jar
* ant-launcher-1.7.1.jar
* commons-httpclient-3.1.jar
* jdom-1.0.jar
* junit-3.8.1.jar
* juel-2.1.1-rc4.jar

All but the `juel` can be found on http://repo1.maven.org. Juel libs
can be fetched from Juel's project home at
http://juel.sourceforge.net/.

To simplify things, all required dependency libaries have been
uploaded at 
 
 http://download.haefelinger.it/flaka/dependencies


This is also the location used by `build.xml'. The dependency
libraries  itself are not shipped as part of the distribution 
package.


__Ant Targets __________________________________________________________________

Default target is "package".


init            Download required libraries inot ${lib.dir} 
compile         Just compile (Java) sources.
test            Run unit tests

javadoc         Generate javadoc files into ${javadoc.dir} 

package         Build flaka-${version}${patchv}.jar in ${dist.dir} and
                flaka.jar in base folder (to be used for debugging,
                Eclipse etc).
package-dist    Create dist package, contains everything needed to
                build project from scratch (but dependency libraries)
package-javadoc Generate and package Javadoc dist in ${dist.dir}
package-all     Build all packages


clean           Remove generated Java class files
clean-dist      Removes folder ${build.dir}, kind of 'hard' reset.

__ Ant Properties ______________________________________________________________

version        \d\.\d{2}       Major and twodigit minor version
patchv         [\d\w-_]*       Patch version      

build.dir      build           All generated stuff goes here
lib.dir        build/lib       Dependency libraries
obj.dir        build/classes   Compiled .class files
javadoc.dir    build/javadoc   Generated Javadoc files
dist.dir       build/dist      Distribution stuff

src.dir        src             Source code
test.dir       test            Tests (unit, others)
depot.url      (see below)     Base URL fetching dependencies

__ Depot ________________________________________________________________________


http://download.haefelinger.it/flaka/dependencies contains all
dependencies required. The structure of the depot is expected to
satisfy a Maven 2 layout folder scheme.

__ Usage ________________________________________________________________________


Once build, you can use Flaka to execute build scripts like 

  $ ant -lib flaka.jar ..

Notice that flaka.jar is prepacked with all those dependency libraries
flaka requires. There is no need to download them or make them
available in an extra step.


__ Why not using Flaka in building Flaka? ______________________________________

Done on purpose to minimize the number of requirements to get
going. To build Flaka by using Flaka, go ahead and use flaka.xml
instead. In order to do so, you need obviously Flaka itself. Use the
flaka.jar file provided as part of the package like

 $ ant -lib flaka.jar -f flaka.xml ..

