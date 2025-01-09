#!/bin/bash

# Path to the JAR file containing JMH benchmarks
JAR_FILE="/Users/mj/workspace/RxJava/build/libs/rxjava-3.0.0-SNAPSHOT-jmh.jar"

# Path to the text file containing the list of JMH benchmarks
BENCHMARK_LIST="benchmark_list.txt"

# Output directory for individual coverage reports
OUTPUT_DIR="individual_coverage_reports"

# Python script to convert XML report to CSV
PYTHON_SCRIPT="xml_extractor.py"

# Create output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Additional JMH configurations (modify as needed)
JMH_CONFIG="-f 1 -wi 0 -i 1 -r 1 -w 1 -bm ss"

# Iterate over each benchmark in the list
while IFS= read -r benchmark; do
    # Create directory for the benchmark
    benchmark_dir="$OUTPUT_DIR/$benchmark"
    mkdir -p "$benchmark_dir"
    
    # Run JMH benchmark with JaCoCo agent and generate coverage report
    java -javaagent:/Users/mj/workspace/RxJava/org.jacoco.agent.jar=output=file,destfile="$benchmark_dir/coverage.exec" \
         -jar "$JAR_FILE" "$benchmark\$" $JMH_CONFIG
    
    # Generate code coverage report
    java -jar /Users/mj/workspace/RxJava/org.jacoco.cli.jar report --classfiles /Users/mj/workspace/RxJava/build/classes/ --xml "$benchmark_dir/report.xml" "$benchmark_dir/coverage.exec"
    
    # Convert XML report to CSV
    python "$PYTHON_SCRIPT" "$benchmark_dir/report.xml" "$benchmark_dir/report.csv"
    
    # Remove the XML report
    rm "$benchmark_dir/report.xml"
done < "$BENCHMARK_LIST"
