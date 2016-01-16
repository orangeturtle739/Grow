#!/bin/bash
cd "$(dirname "$0")"
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home
ant bundle
