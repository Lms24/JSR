# JSR - The Java Test Suite Reduction Framework 

JSR is a test suite reduction framework for Java. It consists of the following modules:


* JSR Core
* JSR CLI
* JSR IntelliJ IDE plugin

## JSR Core

The core framework module, containing the functionality and implementation of algorithms, calls to programs, libraries, etc.

More specifically, the Core modules provides APIs to clients for the following features:

* Test suite parsing and TS information collection
* Coverage calculation
* TSR calculation
* Spectrum-based Fault Localization (SFL) Matrix export

## JSR CLI

A CLI frontend of the JSR framework, supporting the following features:

* Test Suite Reduction
  * Using different coverage metrics
  * Using different TSR algorithms
    * Greedy HGS
    * Genetic 
* FSL Matrix export

## IDE Plugins/Extensions

The IntelliJ IDE Plugin serves as the front-end GUI. It directly integrates with IntelliJ so that developers can interact with JSR in a convenient and already established way. The Plugini provides JSR-Core functionality with a few additional features that only make sense in the IDE/GUI context:

* Define and persist JSR settings (paths, package names)
* Adjust TSR parameters (coverage type, TSR algorithm, Code generation)
* Perform a fresh TSR round (including coverage report generation)
* Perform  TSR round based on an already existent coverage report
* List retained and redundant test cases after a TSR round
* On click of a TC from the lists, the TC is opened up in the editor

## Installation

This chapter outlines how to install JSR. All steps are required to set it up properly.

Please note that the installation process is subject to change as the project progresses.

### 1. Requirements

* Java JDK 11 
  * make sure it is set as the default JDK version on your system
  * make sure, `JAVA_HOME` points to your default JDK
* Maven (required for building Slicer4J)
* Gradle (required for building this project)

### 2. JSR Installation

1. Clone this repository and navigate to the project root directory

```shell
git clone git@github.com:Lms24/JSR.git 
cd jsr
```

2. Execute the installation script to clone and build Slicer4J. 

```shell
sh install.sh
```

3. Build the JSR project (this will build the Core, CLI and IntelliJ Plugin modules) 

    Before building the actual project though, we need to initialize our testing environment so that the 
project build can properly run the unit tests:
   
```shell
cd JSR-Core/src/test/resources/smallProject
gradle
gradle build #this will fail due to a unit test case that should fail on purpose
gradle testJar
```

Then, we can finally build the JSR framework


```shell
cd ../../../../../ #so that we're in the 'jsr' root directory 
gradle
gradle build
```