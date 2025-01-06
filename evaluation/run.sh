#!/bin/bash

make run_cmender CMENDER_OUTPUT_PATH=../results/cmender_output_first_error ANALYSIS=BasicFirstErrorAnalysis
make all CMENDER_OUTPUT_PATH=../results/cmender_output_multiple_pp_errors ANALYSIS=BasicMultiplePPErrorsAnalysis
make run_cmender CMENDER_OUTPUT_PATH=../results/cmender_output_multiple_errors ANALYSIS=BasicMultipleErrorsAnalysis
