import json
import sys
import os
import subprocess

VM_OPTIONS = "-Dlog.level=debug -Dlog.dir=/tmp/cmender/logs -Ddata.dir=/tmp/cmender/logs/data"


def run_cmender_on_files(cmender_path, diag_exporter_path, files, output_dirpath, threads=12, analysis="BasicMultiplePPErrorsAnalysis"):
        #print("Running cmender on all files")

        all_file_paths = " ".join(files)

        # save in file for debugging
        with open("all_file_paths.txt", "w") as f:
            f.write(all_file_paths)

        #print("All file paths: ", all_file_paths)

        command = f"java {VM_OPTIONS} -jar {cmender_path} -dex {diag_exporter_path} -mendfile-cpi -diags-cpi -od {all_file_paths} -o {output_dirpath} -t {threads} -rps -a {analysis}"

        result = subprocess.run(command, shell=True, capture_output=True, text=True)

if __name__ == '__main__':    
    if len(sys.argv) != 7:
        print("Usage: python evaluation.py <dataset_dir> <cmender_output_dir> <cmender_path> <diag_exporter_path> <threads> <analysis>")
        sys.exit(1)

    dataset_dirpath = sys.argv[1]
    cmender_output_dirpath = sys.argv[2]
    cmender_path = sys.argv[3]
    diag_exporter_path = sys.argv[4]
    threads = sys.argv[5]
    analysis = sys.argv[6]

    dataset_projects_dirpath = os.path.join(dataset_dirpath, "projects")
    dataset_projects_info_filepath = os.path.join(dataset_dirpath, "projects_info.json")

    print("Dataset projects dirpath: ", dataset_projects_dirpath)
    print("Dataset projects info filepath: ", dataset_projects_info_filepath)
    print("Cmender output dirpath: ", cmender_output_dirpath)
    print("Cmender path: ", cmender_path)
    print("Diag exporter path: ", diag_exporter_path)
    print("Threads: ", threads)
    print("Analysis: ", analysis)
    print()

    dataset_projects_info = json.load(open(dataset_projects_info_filepath, "r"))

    os.makedirs(cmender_output_dirpath, exist_ok=True)

    for dataset_project in dataset_projects_info:
        project_files = []

        print("Processing project: ", dataset_project["name"], " branch: ", dataset_project["branch"])

        for root, dirs, files in os.walk(os.path.join(dataset_projects_dirpath, dataset_project["name"].replace("/", "_") + "_" + dataset_project["branch"])):
            for file in files:
                project_files.append(os.path.abspath(os.path.join(root, file)))

        project_cmender_output_dirpath = os.path.join(cmender_output_dirpath, dataset_project["name"].replace("/", "_"))

        run_cmender_on_files(cmender_path, diag_exporter_path, project_files, project_cmender_output_dirpath, threads, analysis)
