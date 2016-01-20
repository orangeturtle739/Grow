#!/bin/bash
cd "$(dirname "$0")"
ant clean
ant
./bundle.sh
ant zipapp
ant package-docs
