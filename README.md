#Overview

This repository provides the full replication materials for the study:
“On the diffuseness and the impact of multi-language code smells”

The study revisits and enhances the state-of-the-art detector for multi-language design smells, validates its accuracy on a larger dataset, and reanalyzes the prevalence and quality impact (fault- and change-proneness) of these smells in 
multi-language software systems.

#Repository Contents

The repository is organized into two main stages, reflecting the structure of the study:

#Stage 1 — Evaluation and Refinement of the Detector

Contains all resources related to assessing and improving the existing multi-language design smell detector.

Dataset/ – Project metadata and scripts for fetching 60 multi-language systems.

Ground Truth/ – Manually annotated smell instances and labeling guidelines.

Detection Approach Revised/ – Refined detector implementation.

Results/ – Detection outputs and precision–recall results for both baseline and refined detectors.

#Stage 2 — Reanalysis of Smell Prevalence and Quality Impact

Includes results and scripts for reanalyzing the updated smell prevalence and their correlation with change- and fault-proneness.

Prevalence Results/ – Updated distribution of smells across systems.

Change-Proneness Results/ – Data and statistical results for change-proneness.

Fault-Proneness Results/ – Data and statistical results for fault-proneness.

#Documentation

A detailed guide to reproducing all results, including setup instructions, script usage, and output interpretation, is provided in:

ReplicationUtilities.pdf

This document serves as the complete procedural manual for replication.

#Dependencies

Java 8+

Python 3.8+ (with pandas, numpy, scipy)

srcML (for parsing multi-language code)

#Outputs

The main results included in this package are:

Detector evaluation results (precision and recall)

Updated smell prevalence statistics

Fault- and change-proneness analysis results
