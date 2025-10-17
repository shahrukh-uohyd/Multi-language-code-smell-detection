#!/bin/bash

# ============================================================
# Script: fetch_projects.sh
# Purpose: Clone all projects listed in Dataset/projects.csv
# Author: Replication package for Multi-language Code Smell Study
# ============================================================

# Input CSV
DATASET_FILE="./Dataset/projects.csv"

# Output directory
OUTPUT_DIR="./cloned_projects"

# Create output dir if not exists
mkdir -p "$OUTPUT_DIR"

# Skip CSV header and process each line
tail -n +2 "$DATASET_FILE" | while IFS=',' read -r project_name loc java_pct c_pct native_methods github_url
do
    echo "========================================="
    echo "Cloning project: $project_name"
    echo "GitHub URL: $github_url"
    echo "========================================="

    # Clone only if not already cloned
    if [ ! -d "$OUTPUT_DIR/$project_name" ]; then
        git clone "$github_url" "$OUTPUT_DIR/$project_name"
    else
        echo "Skipping $project_name (already cloned)."
    fi
done

echo "========================================="
echo "✅ All projects cloned into: $OUTPUT_DIR"
echo "========================================="
