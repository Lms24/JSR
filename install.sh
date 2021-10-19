#!/bin/bash
cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" 



# Install Slicer4J

echo "#################################################################"
echo "Downloading Slicer4J and DynamicSlicingCore"

mkdir slicer
git clone git@github.com:resess/DynamicSlicingCore.git slicer/DynamicSlicingCore
git clone git@github.com:resess/Slicer4J.git slicer/Slicer4J


echo "#################################################################"
echo "Building Slicer4J and DynamicSlicingCore"

mvn -f slicer/DynamicSlicingCore/core -Dmaven.test.skip=true clean install 
mvn -f slicer/Slicer4J/Slicer4J -Dmaven.test.skip=true clean install 


echo "#################################################################"
echo "Building JUnit4 Runner"

javac slicer/Slicer4J/scripts/SingleJUnitTestRunner.java -cp "slicer/Slicer4J/scripts/junit-4.8.2.jar"
jar cf slicer/Slicer4J/scripts/SingleJUnitTestRunner.jar slicer/Slicer4J/scripts/SingleJUnitTestRunner.class
rm slicer/Slicer4J/scripts/SingleJUnitTestRunner.class
