# Variables
PYTHON = python3

DATASET_PATH = ../repos/repos_output/dataset
CMENDER_OUTPUT_PATH = ../results/cmender_output
CMENDER_PATH = ../cmender/build/libs/cmender.jar
DIAG_EXPORTER_PATH = ../diag-exporter/cmake-build-debug/diag-exporter
THREADS = 25
ANALYSIS = BasicMultiplePPErrorsAnalysis
INCLUDES_OUTPUT_PATH = ../results/tupatcher_includes
TUPATCHER_OUTPUT_PATH = ../results/tupatcher_output
EVAL_OUTPUT_PATH = ./results/evaluation

all: run_cmender extract_tupatcher_include_path

# Run cmender
run_cmender:
	$(PYTHON) ./src/run_cmender.py $(DATASET_PATH) $(CMENDER_OUTPUT_PATH) $(CMENDER_PATH) $(DIAG_EXPORTER_PATH) $(THREADS) $(ANALYSIS)

# Extract tupatcher include path
extract_tupatcher_include_path:
	$(PYTHON) ./src/extract_tupatcher_include_path.py $(CMENDER_OUTPUT_PATH) $(INCLUDES_OUTPUT_PATH)

# Evaluation
evaluate:
	$(PYTHON) ./src/evaluation.py $(CMENDER_OUTPUT_PATH) $(TUPATCHER_OUTPUT_PATH) $(DATASET_PATH)/projects_info.json $(EVAL_OUTPUT_PATH) $(INCLUDES_OUTPUT_PATH)

# Phony targets
.PHONY: all run_cmender extract_tupatcher_include_path