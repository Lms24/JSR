#!/bin/bash
cd "$(dirname "$0")"



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

sh slicer/Slicer4J/scripts/create_runner.sh
mv SingleJUnitTestRunner.jar -f slicer/Slicer4J/scripts/SingleJUnitTestRunner.jar