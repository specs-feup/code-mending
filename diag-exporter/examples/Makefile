EXAMPLES := \
	incompatible_types.c \
	missing_headers.c \
	missing_vars.c \
	recursive_macro_function.c \
	missing_functions.c
OUTPUT_PATH ?= ./output/output.json
LANGUAGE ?= c
STANDARD ?= c11
ERROR_LIMIT ?= 0

run_all:
	@echo "Running command with all examples: $(EXAMPLES)"
	@echo "Output Path: $(OUTPUT_PATH)"
	@echo "Language: $(LANGUAGE)"
	@echo "Standard: $(STANDARD)"
	@echo "Command: ../cmake-build-debug/diag-exporter $(EXAMPLES) -o $(OUTPUT_PATH) \
			-- -x $(LANGUAGE) -std=$(STANDARD) -ferror-limit=$(ERROR_LIMIT)"
	../cmake-build-debug/diag-exporter $(EXAMPLES) -o $(OUTPUT_PATH) -- -x $(LANGUAGE) -std=$(STANDARD) -ferror-limit=$(ERROR_LIMIT)

run:
	$(if $(filter-out run, $(MAKECMDGOALS)),,$(error No examples provided. Please specify at least one example.))
	@echo "Running command with examples: $(filter-out run, $(MAKECMDGOALS))"
	@echo "Output Path: $(OUTPUT_PATH)"
	@echo "Language: $(LANGUAGE)"
	@echo "Standard: $(STANDARD)"
	@echo "Command: ../cmake-build-debug/diag-exporter $(filter-out run, $(MAKECMDGOALS)) -o $(OUTPUT_PATH) \
 			-- -x $(LANGUAGE) -std=$(STANDARD) -ferror-limit=$(ERROR_LIMIT)"
	../cmake-build-debug/diag-exporter $(filter-out run, $(MAKECMDGOALS)) -o $(OUTPUT_PATH) -- -x $(LANGUAGE) -std=$(STANDARD) -ferror-limit=$(ERROR_LIMIT)

# This target allows setting the optional variables when invoking make
# Prevents make from treating command-line arguments as filenames
%:
	@:
