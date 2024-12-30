import sys
import os
import json
import re
import shutil
import subprocess
import random
import pandas as pd

from concurrent.futures import ThreadPoolExecutor, as_completed

def get_c_files(path, max_files=None):
    c_files = set()

    for root, dirs, files in os.walk(path):
        for file in files:
            if max_files is not None and len(c_files) >= max_files:
                break

            if file.endswith(".c"):
                c_files.add(os.path.abspath(os.path.join(root, file)))

    return list(c_files)

def convert_include_path(include_line):
    pattern = r'(#include\s+[<"])(.*?)([">])'

    def remove_parent_directories(match):
        path = match.group(2)

        while "../" in path:
            path = re.sub(r'(^|/)\.\./', '/', path, count=1)

        # Remove any leading or trailing slashes (just in case)
        path = path.strip("/")

        return f'{match.group(1)}{path}{match.group(3)}'

    return re.sub(pattern, remove_parent_directories, include_line)

if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("Usage: python repos_prep.py <repos_df_filepath> <repos_dirpath> <output_dirpath>")
        sys.exit(1)

    repos_df_filepath = sys.argv[1]
    repos_dirpath = sys.argv[2]
    output_dirpath = sys.argv[3]

    repos_df = pd.read_csv(repos_df_filepath)

    projects_info = [
        {
            "name": "id-Software/DOOM",
            "branch": "master",
            "commit": "a77dfb96cb91780ca334d0d4cfd86957558007e0",
            "total_files": 0,
            "sample_size": None,
            "files": []
        },
        {
            "name": "torvalds/linux",
            "branch": "master",
            "commit": "48f506ad0b683d3e7e794efa60c5785c4fdc86fa",
            "total_files": 0,
            "sample_size": 1000,
            "files": []
        },
        {
            "name": "openssl/openssl",
            "branch": "master",
            "commit": "b3bb214720f20f3b126ae4b9c330e9a48b835415",
            "total_files": 0,
            "sample_size": None,
            "files": []
        },
        {
            "name": "OpenVPN/openvpn",
            "branch": "master",
            "commit": "435cc9a7535c86c1ab513ae106895960c183095e",
            "total_files": 0,
            "sample_size": None,
            "files": []
        },
        {
            "name": "git/git",
            "branch": "master",
            "commit": "ff795a5c5ed2e2d07c688c217a615d89e3f5733b",
            "total_files": 0,
            "sample_size": None,
            "files": []
        }
    ]

    processed_files_dirpath = os.path.join(output_dirpath, "projects")
    os.makedirs(output_dirpath, exist_ok=True)
    os.makedirs(processed_files_dirpath, exist_ok=True)
    
    for project in projects_info:
        # select the repo details from the dataframe
        project_df = repos_df[repos_df["full_name"] == project["name"]]

        project["commit"] = project_df["default_branch_commit_sha"].values[0]

        project_dirname = project["name"].replace("/", "_") + "_" + project["branch"]

        project_path = os.path.join(repos_dirpath, project_dirname)

        processed_files_project_dirpath = os.path.join(processed_files_dirpath, project_dirname)

        os.makedirs(processed_files_project_dirpath, exist_ok=True)

        unprocessed_filepaths = get_c_files(project_path)

        project["total_files"] = len(unprocessed_filepaths)

        unprocessed_filepaths = unprocessed_filepaths if project["sample_size"] is None else random.sample(unprocessed_filepaths, project["sample_size"] + 200)

        files = []

        project_relative_filepaths = [os.path.relpath(filepath, project_path) for filepath in unprocessed_filepaths]

        for i in range(len(unprocessed_filepaths)):
            if project["sample_size"] is not None and len(files) >= project["sample_size"]:
                break

            unprocessed_filepath = unprocessed_filepaths[i]

            relative_filepath = project_relative_filepaths[i]

            processed_filename = project_relative_filepaths[i].replace("/", "_")

            output_filepath = os.path.join(processed_files_project_dirpath, processed_filename)

            with open(unprocessed_filepath, "r") as f:
                unprocessed_file_content = f.readlines()

                if len(unprocessed_file_content) == 0:
                    print(f"Empty file: {unprocessed_filepath}")
                
                modified_lines = [convert_include_path(line) for line in unprocessed_file_content]

                with open(output_filepath, 'w') as file:
                    file.writelines(modified_lines)
            
                try:
                    result = subprocess.run(
                        #["clang", "-fsyntax-only", output_filepath],
                        ["clang", "-fsyntax-only", "-nostdinc", "-isysroot", "\"\"", output_filepath],
                        stdout=subprocess.PIPE,  # Suppress stdout
                        stderr=subprocess.PIPE   # Suppress stderr
                    )
        
                    if result.returncode != 0:
                        files.append(relative_filepath)
                    else:
                        print("Compilation successful. Ignoring the file", relative_filepath, " from the project ", project["name"])
                        os.remove(output_filepath)
                except Exception as e:
                    print(f"Error: {e}")

        project["selected_files"] = len(files)
        project["files"] = files

    with open(f"{output_dirpath}/projects_info.json", "w") as f:
        f.write(json.dumps(projects_info, indent=2))
    