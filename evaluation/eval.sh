#!/bin/bash

make evaluate EVAL_OUTPUT_PATH=../results/evaluation/first TUPATCHER_OUTPUT_PATH=../../clava/TranslationUnitPatcher/output CMENDER_OUTPUT_PATH=../results/cmender_output_first_error
make evaluate EVAL_OUTPUT_PATH=../results/evaluation/multiplepp TUPATCHER_OUTPUT_PATH=../../clava/TranslationUnitPatcher/output CMENDER_OUTPUT_PATH=../results/cmender_output_multiple_pp_errors
make evaluate EVAL_OUTPUT_PATH=../results/evaluation/multiple TUPATCHER_OUTPUT_PATH=../../clava/TranslationUnitPatcher/output CMENDER_OUTPUT_PATH=../results/cmender_output_multiple_errors
