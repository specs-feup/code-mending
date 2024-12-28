import os
import sys
import json
import datetime
import dotenv

from logger import logger
from github import GithubClient

dotenv.load_dotenv()

def save_metadata(metadata, output_dirpath):
    metadata_path = os.path.join(output_dirpath, "name_extract.json")

    with open(metadata_path, "w") as f:
        f.write(json.dumps(metadata, indent=2))

def save_repo_names(repo_names, output_dirpath):
    repo_names_path = os.path.join(output_dirpath, "repo_names.txt")

    with open(repo_names_path, "w") as f:
        if repo_names is None:
            return

        for repo_name in repo_names:
            f.write(repo_name + "\n")

def fetch_repo_names(client: GithubClient, n, query):
    try:
        return client.fetch_repo_names_paginated(query, n)
    except Exception as e:
        logger.error(e)
        logger.error("Failed to fetch repo names")
        return None, False

def build_query(min_forks, min_stars):
    return f"language:C language:C++ forks:>={min_forks} stars:>={min_stars}"

def extract(n, min_forks, min_stars, output_dirpath, gh_pat):
    client = GithubClient(gh_pat=gh_pat)

    # TODO
    #if not os.path.exists(output_dirpath):
    #    os.mkdir(output_dirpath)
    os.makedirs(output_dirpath, exist_ok=True)
    
    query = build_query(min_forks, min_stars)

    timestamp = datetime.datetime.now().isoformat()

    repo_names, fetched_desired_amount = fetch_repo_names(client, n, query)

    if not repo_names:
        return

    if not fetched_desired_amount:
        logger.warning("Failed to fetch the desired amount of repos")

    logger.info(f"Extracted {len(repo_names)} repo names")

    save_repo_names(repo_names, output_dirpath)

    save_metadata({
        "api_version": client.api_version,
        "requested_count": n,
        "query": query,
        "retrieved_count": 0 if repo_names is None else len(repo_names),
        "timestamp": timestamp }, output_dirpath)

if __name__ == "__main__":
    if len(sys.argv) != 5:
        print("Usage: python repo_names_fetch.py <n> <min_forks> <min_stars> <output_dir>")
        sys.exit(1)

    output_dirpath = sys.argv[4]

    try:
        n = int(sys.argv[1])
        min_forks = int(sys.argv[2])
        min_stars = int(sys.argv[3])
    except ValueError:
        logger.error("Invalid value for int argument")
        sys.exit(1)

    gh_pat = os.getenv("GH_PAT", "")

    extract(n, min_forks, min_stars, output_dirpath, gh_pat)

# clear ; python3 ./src/repo_names_fetch.py 2000 500 1000 ./output -> 1000
# clear ; python3 ./src/repo_names_fetch.py 1000 5000 10000 ./output -> 70