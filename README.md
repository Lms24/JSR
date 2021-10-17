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

* Collecting information about specifed test suites and assertions
* Coverage calculation
  * Checked coverage 
  * Line coverage
  * Method coverage
* Test Suite Reduction
  * Using different coverage metrics
  * Possibly, using different TSR algorithms
    * Greedy HGS
    * Genetic 

## IDE Plugins/Extensions

The IntelliJ IDE Plugin serves as the front-end GUI. It directly integrates with IntelliJ so that developers can interact with JSR in a convenient and already established way. The Plugini provides JSR-Core functionality with a few additional features that only make sense in the IDE/GUI context:

* Define and persist JSR settings (paths, package names)
* Adjust TSR parameters (coverage type, TSR algorithm, Code generation)
* Perform a fresh TSR round (including coverage report generation)
* Perform  TSR round based on an already existent coverage report
* List retained and redundant test cases after a TSR round
* On click of a TC from the lists, the TC is opened up in the editor
