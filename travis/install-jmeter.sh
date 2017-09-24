#!/bin/bash

directory="$1"
version=3.3

wget http://www.apache.org/dist//jmeter/binaries/apache-jmeter-${version}.tgz -O /tmp/apache-jmeter-${version}.tgz
tar -xf /tmp/apache-jmeter-${version}.tgz
mv apache-jmeter-${version} $directory
