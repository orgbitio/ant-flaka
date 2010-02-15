

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
