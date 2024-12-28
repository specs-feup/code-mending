# Variables
PYTHON = python3

REPO_COUNT = 2000
MIN_REPO_FORKS = 500
MIN_REPO_STARS = 1000
OUTPUT_DIR = ./repos_output
REPO_NAMES_OUTPUT = ./repos_output/names
REPO_DETAILS_OUTPUT = ./repos_output/details
REPOS_OUTPUT = ./repos_output/repos
DATASET_OUTPUT = ./repos_output/dataset
DATASET_DESC_OUTPUT = ./repos_output/dataset_desc

all: repo_names_fetch repo_details_fetch repo_download dataset_prep dataset_desc

# Fetch repo names
repo_names_fetch:
	$(PYTHON) ./src/repo_names_fetch.py $(REPO_COUNT) $(MIN_REPO_FORKS) $(MIN_REPO_STARS) $(REPO_NAMES_OUTPUT)

# Fetch repo details
repo_details_fetch:
	$(PYTHON) ./src/repo_details_fetch.py $(REPO_NAMES_OUTPUT)/repo_names.txt $(REPO_DETAILS_OUTPUT)

# Download repo details
repo_download:
	$(PYTHON) ./src/repos_download.py $(REPO_DETAILS_OUTPUT)/repos.csv $(REPOS_OUTPUT)

# Dataset preparation
dataset_prep:
	$(PYTHON) ./src/dataset_prep.py $(REPO_DETAILS_OUTPUT)/repos.csv $(REPOS_OUTPUT) $(DATASET_OUTPUT)

# Dataset description
dataset_desc:
	$(PYTHON) ./src/dataset_desc.py $(DATASET_OUTPUT) $(DATASET_DESC_OUTPUT)

# Phony targets
.PHONY: all repo_names_fetch repo_details_fetch repo_download dataset_prep dataset_desc