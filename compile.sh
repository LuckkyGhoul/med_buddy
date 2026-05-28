#!/bin/bash
# Recompile from source
mkdir -p out/classes
javac -cp "lib/h2.jar" -d out/classes $(find src -name "*.java")
echo "Manifest-Version: 1.0
Main-Class: medbuddy.Main
Class-Path: h2.jar" > out/MANIFEST.MF
jar cfm MedBuddy.jar out/MANIFEST.MF -C out/classes .
echo "Build done: MedBuddy.jar"
