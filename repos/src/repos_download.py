import os
import io
import sys
import time
import shutil
import zipfile
import hashlib
import requests
import pandas as pd
from concurrent.futures import ThreadPoolExecutor, as_completed

from logger import logger

TOTAL_RETRIES = 3

def generate_timestamp_id():
    timestamp = str(time.time())
    hash_obj = hashlib.md5(timestamp.encode(), usedforsecurity=False)
    unique_id = hash_obj.hexdigest()
    return unique_id

def download_repo(repo_details, repos_dirpath):
    #if retries == 0:
    #    logger.error(f"Failed to fetch zipfile of repo '{repo_full_name}' head branch '{default_branch}'")
    #    return

    repo_full_name = repo_details["full_name"]
    default_branch = repo_details["default_branch"]
    zip_download_url = repo_details["zip_default_branch_url"]

    if repo_full_name != "torvalds/linux" and repo_full_name != "id-Software/DOOM" and repo_full_name != "openssl/openssl" and repo_full_name != "OpenVPN/openvpn" and repo_full_name != "git/git":
        return

    logger.info(f"Fetching zipfile of repo '{repo_full_name}' head branch '{default_branch}'...")

    retries = TOTAL_RETRIES

    while retries >= 0:
        try:
            response = requests.get(zip_download_url)

            if response.status_code == 200:
                break

            logger.error(f"Failed to fetch zipfile of repo '{repo_full_name}' head branch '{default_branch}' with status code {response.status_code}")

            if retries == 0:
                logger.info(f"Retries exhausted")
            else:
                logger.info(f"Retrying... ({retries} retries left)")

            retries -= 1

        except Exception as e:
            logger.error(f"Failed to fetch zipfile of repo '{repo_full_name}' head branch '{default_branch}': {e}")

            if retries == 0:
                logger.info(f"Retries exhausted")
            else:
                logger.info(f"Retrying... ({retries} retries left)")

            retries -= 1

    if retries == -1:
        return

    logger.info(f"Succesfully fetched zipfile of repo '{repo_full_name}' head branch '{default_branch}'")

    with zipfile.ZipFile(io.BytesIO(response.content)) as zip_ref:
        owner = repo_details["owner_name"]
        repo = repo_details["repo_name"]

        created_repo_dirname = f"{repo}-{default_branch}"
        #created_repo_dirname = zip_ref
        repo_dirpath = os.path.join(repos_dirpath, f"{owner}_{repo}_{default_branch}")

        # Easy hack to remove the top level directory on the zip archive
        temp_dirpath = os.path.join(repos_dirpath, f"temp_{generate_timestamp_id()}")
        logger.info(f"Extracting repo '{repo_full_name}' to '{temp_dirpath}'...")
        zip_ref.extractall(temp_dirpath)
        os.rename(os.path.join(temp_dirpath, created_repo_dirname), repo_dirpath)
        shutil.rmtree(temp_dirpath, ignore_errors=True)

        logger.info(f"Successfully extracted repo '{repo_full_name}'")

def download_repos(repos_df_filepath, output_dirpath):
    if not os.path.exists(output_dirpath):
        os.mkdir(output_dirpath)

    #repos_dirpath = os.path.join(output_dirpath, "repos")

    #if not os.path.exists(repos_dirpath):
    #    os.mkdir(repos_dirpath)

    repos_dirpath = output_dirpath
    os.makedirs(repos_dirpath, exist_ok=True)

    try:
        repos_df = pd.read_csv(repos_df_filepath)
    except Exception as e:
        logger.error(f"Failed to read repos dataframe from '{repos_df_filepath}': {e}")
        sys.exit(1)

    with ThreadPoolExecutor(max_workers=4) as executor:
        futures = []
        for _, repo_details in repos_df.iterrows():
            futures.append(executor.submit(download_repo, repo_details, repos_dirpath))

        for future in as_completed(futures):
            future.result()  # To raise exceptions if any occurred during the process

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python repos_download.py <repos_df_filepath> <output_dirpath>")
        sys.exit(1)
    
    repos_df_filepath = sys.argv[1]
    output_dirpath = sys.argv[2]

    download_repos(repos_df_filepath, output_dirpath)

# clear ; python3 ./src/repos_download.py ./output/repos.csv ./output3