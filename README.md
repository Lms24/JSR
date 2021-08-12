# JSR - The Java Test Suite Reduction Framework 

JSR is a test suite reduction framework for Java. It consists of the following modules:

* JSR CLI
* JSR Core
* Possibly: JSR VS Code extension
* Possibly: JSR IntelliJ IDEA plugin

## JSR CLI

A CLI frontend of the JSR framework, supporting the following features:

* Collecting information about specifed test suites and assertions
* Coverage calculation
  * Assertion coverage 
  * Code coverage
  * ...other forms?
* Test Suite Reduction
  * Using different coverage metrics
  * Possibly, using different TSR algorithms

## JSR Core

The core framework module, containing the functionality and implementation of algorithms, calls to programs, libraries, etc.

More specifically, the Core modules provides APIs to clients for the following features:

* Test suite parsing and TS information collection
* Coverage calculation
* TSR calculation

## IDE Plugins/Extensions

TBD
