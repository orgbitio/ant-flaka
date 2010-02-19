

__Build from scratch__

Build Flaka from scratch, i.e. without using Flaka, by using the
provided build file `legacy.xml':

 $ ant -f legacy.xml

Notice: you need to satisfy the following requirements:

1. Java 1.4 and newer
2. Dependency Libraries (see below)
3. A resonable new version of Ant (anything newer than 1.6 may do
well).

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
uploaded at http://download.haefelinger.it/flaka/dependencies. This is
also the location where `legacy.xml' tries to satisfy those
requirements. The libraries itself are not shipped in Flaka's
distribution package.


__Ant Targets _____________

Default target is "package".


init            Download required libraries inot ${lib.dir} 
compile         Just compile (Java) sources.
test            Run unit tests
package         Build flaka-${version}${patchv}.jar in ${dist.dir} and
                flaka.jar in base folder (to be used for debugging,
                Eclipse etc).
package-javadoc Generate and package Javadoc dist in ${dist.dir}
package-all     Build all packages
javadoc         Generate javadoc files into ${javadoc.dir} 


__ Ant Properties _________

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

__ Depot ___________________

http://download.haefelinger.it/flaka/dependencies contains all
dependencies required. The structure of the depot is expected to
satisfy a Maven 2 layout folder scheme.

__Usage ___________________


Once build, you can use Flaka to execute build scripts like 

  $ ant -lib flaka.jar ..

Notice that flaka.jar is prepacked with all those dependency libraries
flaka requires. There is no need to download them or make them
available in an extra step.
