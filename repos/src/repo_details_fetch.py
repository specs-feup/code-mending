import os
import sys
import json
import datetime
import pandas as pd
import dotenv

from logger import logger
from github import GithubClient

dotenv.load_dotenv()

RELEVANT_LANGS = ["C", "C++", "Makefile", "CMake", "Starlark", "Meson", "Ninja", "QMake"]

RELEVANT_BUILD_LANGS = ["Makefile", "CMake", "Starlark", "Meson", "Ninja", "QMake"]

def save_metadata(api_version, output_dirpath):
    metadata = {
        "api_version": api_version,
        "timestamp": datetime.datetime.now().isoformat(),
    }

    metadata_path = os.path.join(output_dirpath, "details_extraction.json")

    with open(metadata_path, "w") as f:
        f.write(json.dumps(metadata, indent=2))

def save_repo_details(repo_details, repo_details_dirpath):
    repo = repo_details["name"]
    owner = repo_details["owner"]["login"]

    repo_details_json_path = os.path.join(repo_details_dirpath, f"{owner}_{repo}.json")

    with open(repo_details_json_path, "w") as f:
        f.write(json.dumps(repo_details, indent=2))

def fetch_raw_repo_details(client: GithubClient, repo_full_name):

    retries = 3

    while retries >= 0:
        try:
            repo_details = client.fetch_repo_details(full_name=repo_full_name)
            repo_languages = client.fetch_repo_languages(full_name=repo_full_name)
            repo_details["languages"] = repo_languages
            repo_default_branch = client.fetch_default_branch(full_name=repo_full_name, branch=repo_details["default_branch"])
            repo_details["default_branch_commit_sha"] = repo_default_branch["commit"]["sha"]
            return repo_details

        except Exception as e:
            logger.error(e)
            logger.error(f"Failed to get details for repo '{repo_full_name}'")

            if retries == 0:
                logger.info(f"Retries exhausted")
            else:
                logger.info(f"Retrying... ({retries} retries left)")

            retries -= 1

    if retries == -1:
        return None


'''
    try:
        repo_details = client.fetch_repo_details(full_name=repo_full_name)
        repo_languages = client.fetch_repo_languages(full_name=repo_full_name)
        repo_details["languages"] = repo_languages
        return repo_details
    except Exception as e:
        logger.error(e)
        logger.error(f"Failed to get details for repo '{repo_full_name}'")
        return None
'''

def transform_lang_name(lang_name):
    if lang_name == "C++":
        return "cpp"

    return lang_name.lower()

def get_lang_ratio_colname(lang_name):
    return transform_lang_name(lang_name) + "_ratio"

def calculate_relevant_lang_ratios(languages):
    global RELEVANT_LANGS

    # TODO Add terminal script languages (M4, Shell, Batchfile, PowerShell)

    total_bytes = sum(languages.values())

    if total_bytes == 0:
        return {lang: 0 for lang in RELEVANT_LANGS}, 0

    return {get_lang_ratio_colname(lang): languages.get(lang, 0) / total_bytes for lang in RELEVANT_LANGS}, total_bytes

def calculate_c_cpp_ratio(languages):
    total_bytes = sum(languages.values())

    c_bytes = languages.get("C", 0)
    cpp_bytes = languages.get("C++", 0)

    if total_bytes == 0:
        return 0

    return (c_bytes + cpp_bytes) / total_bytes

def calculate_build_lang_ratio(languages):
    global RELEVANT_BUILD_LANGS

    total_bytes = sum(languages.values())

    if total_bytes == 0:
        return 0

    return sum(languages.get(lang, 0) for lang in RELEVANT_BUILD_LANGS) / total_bytes

def extract(repo_names_path, output_dirpath, gh_pat):
    client = GithubClient(gh_pat=gh_pat)

    if not (os.path.exists(repo_names_path) and os.path.isfile(repo_names_path)):
        logger.error(f"File '{repo_names_path}' does not exist or is not a file")
        sys.exit(1)

    repo_details_dirpath = os.path.join(output_dirpath, "repo-details")

    if os.path.exists(output_dirpath):
        logger.error(f"Output directory '{output_dirpath}' already exists")
        sys.exit(1)

    os.makedirs(repo_details_dirpath)

    save_metadata(client.api_version, output_dirpath)

    repos_details_df = pd.DataFrame(columns=[
        "repo_name",
        "owner_name",
        "full_name",
        "owner_type",
        "description",
        "topics",
        "created_at",
        "size_kb",
        "default_branch",
        "default_branch_commit_sha",
        "forks",
        "stargazers",
        "subscribers",
        "license_key",
        "license_name",
        "license_spdx_id",
        "license_url",
        "language",
        *[get_lang_ratio_colname(lang) for lang in RELEVANT_LANGS],
        "c_cpp_ratio",
        "build_lang_ratio",
        "total_lang_kb",
        "html_url",
        "git_url",
        "ssh_url",
        "clone_url",
        "zip_default_branch_url",
        "homepage"
    ])

    with open(repo_names_path, "r") as f:
        for full_name_line in f:
            full_name = full_name_line.strip()

            logger.info(f"Getting details of repo '{full_name}'...")

            repo_details = fetch_raw_repo_details(client, full_name)

            if not repo_details:
                continue

            save_repo_details(repo_details, repo_details_dirpath)
            
            lang_ratios, total_bytes = calculate_relevant_lang_ratios(repo_details["languages"])
            c_cpp_ratio = calculate_c_cpp_ratio(repo_details["languages"])
            build_file_ratio = calculate_build_lang_ratio(repo_details["languages"])

            repos_details_df.loc[len(repos_details_df)] = {
                "repo_name": repo_details["name"],
                "owner_name": repo_details["owner"]["login"],
                "full_name": repo_details["full_name"],
                "owner_type": repo_details["owner"]["type"],
                "description": repo_details["description"],
                "topics": ",".join(repo_details["topics"]) if repo_details["topics"] else None,
                "created_at": repo_details["created_at"],
                "size_kb": repo_details["size"],
                "default_branch": repo_details["default_branch"],
                "default_branch_commit_sha": repo_details["default_branch_commit_sha"],
                "forks": repo_details["forks"],
                "stargazers": repo_details["stargazers_count"],
                "subscribers": repo_details["subscribers_count"],
                "license_key": repo_details["license"]["key"] if repo_details["license"] else None,
                "license_name": repo_details["license"]["name"] if repo_details["license"] else None,
                "license_spdx_id": repo_details["license"]["spdx_id"] if repo_details["license"] else None,
                "license_url": repo_details["license"]["url"] if repo_details["license"] else None,
                "language": repo_details["language"],
                **lang_ratios,
                "c_cpp_ratio": c_cpp_ratio,
                "build_lang_ratio": build_file_ratio,
                "total_lang_kb": total_bytes / 1024,
                "html_url": repo_details["html_url"],
                "git_url": repo_details["git_url"],
                "ssh_url": repo_details["ssh_url"],
                "clone_url": repo_details["clone_url"],
                "zip_default_branch_url": client.get_branch_zip_url(repo_details["full_name"], repo_details["default_branch"]),
                "homepage": repo_details["homepage"]
            }

    repos_details_df.to_csv(os.path.join(output_dirpath, "repos.csv"), index=False)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python repo_details_extract.py <repo_names_path> <output_dirpath>")
        sys.exit(1)

    repo_names_path = sys.argv[1]
    output_dirpath = sys.argv[2]

    gh_pat = os.getenv("GH_PAT", "")

    extract(repo_names_path, output_dirpath, gh_pat)

# clear ; python3 ./src/repo_details_fetch.py ./repos.txt ./output