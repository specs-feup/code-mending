import requests
import os
from logger import logger

class GithubClient:
    def __init__(self, gh_pat, api_version="2022-11-28"):
        self.api_version = api_version
        self.api_base_url = "https://api.github.com"
        self.repos_api_url = os.path.join(self.api_base_url, "repos")
        self.graphql_api_url = "https://api.github.com/graphql"
        self.gh_pat = gh_pat
        self.base_headers = {
            "X-GitHub-Api-Version": f"{api_version}",
            "Authorization": f"Bearer {self.gh_pat}",
            "Accept": "application/vnd.github+json"
        }

    def fetch_repo_details(self, owner=None, repo=None, full_name=None):
        self._validate_repo_params(owner, repo, full_name)

        if owner and repo:
            full_name = f"{owner}/{repo}"

        repo_api_url = os.path.join(self.repos_api_url, full_name)

        response = requests.get(repo_api_url, headers=self.base_headers)

        logger.debug(f"Repo details request response: {response.text}")
        
        try:
            response_obj = response.json()
        except Exception as e:
            raise Exception(f"Failed to parse response JSON: {e}")
        
        if response.status_code == 200:
            return response_obj
        
        message = response_obj.get("message", "")
        documentation_url = response_obj.get("documentation_url", "")
        # {"message":"API rate limit exceeded for 78.137.195.161. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)","documentation_url":"https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"}
        #response.text
        raise Exception(f"Repo details request failed with status code {response.status_code} and message: {message}. Documentation URL: {documentation_url}")

    # Lists languages for the specified repository. The value shown for each language is the number of bytes of code written in that language.
    def fetch_repo_languages(self, owner=None, repo=None, full_name=None):
        self._validate_repo_params(owner, repo, full_name)

        if owner and repo:
            full_name = f"{owner}/{repo}"

        langs_api_url = os.path.join(self.repos_api_url, full_name, "languages")

        response = requests.get(langs_api_url, headers=self.base_headers)

        try:
            response_obj = response.json()
        except Exception as e:
            raise Exception(f"Failed to parse response JSON: {e}")
        
        if response.status_code == 200:
            return response_obj
        
        message = response_obj.get("message", "")
        documentation_url = response_obj.get("documentation_url", "")

        raise Exception(f"Repo details request failed with status code {response.status_code} and message: {message}. Documentation URL: {documentation_url}")

    def fetch_default_branch(self, owner=None, repo=None, full_name=None, branch="master"):
        self._validate_repo_params(owner, repo, full_name)

        if owner and repo:
            full_name = f"{owner}/{repo}"

        branch_api_url = os.path.join(self.repos_api_url, full_name, "branches", branch)

        response = requests.get(branch_api_url, headers=self.base_headers)

        try:
            response_obj = response.json()
        except Exception as e:
            raise Exception(f"Failed to parse response JSON: {e}")
        
        if response.status_code == 200:
            return response_obj
        
        message = response_obj.get("message", "")
        documentation_url = response_obj.get("documentation_url", "")

        raise Exception(f"Repo details request failed with status code {response.status_code} and message: {message}. Documentation URL: {documentation_url}")

    def fetch_repo_names_paginated(self, q, n):
        query = """
            query($queryString: String!, $cursor: String) {
              search(query: $queryString, type: REPOSITORY, first: 100, after: $cursor) {
                pageInfo {
                  hasNextPage
                  endCursor
                }
                edges {
                  node {
                    ... on Repository {
                      nameWithOwner
                    }
                  }
                }
              }
            }
        """

        variables = {
            "queryString": q,
            "cursor": None
        }

        repo_names = []

        while len(repo_names) < n:
            response = requests.post(self.graphql_api_url, headers=self.base_headers, 
                                     json={"query": query, "variables": variables})

            try:
                response_obj = response.json()
            except Exception as e:
                raise Exception(f"Failed to parse response JSON: {e}")

            if response.status_code != 200:
                message = response_obj.get("message", "")
                documentation_url = response_obj.get("documentation_url", "")
                raise Exception(f"Repo details request failed with status code {response.status_code} and message: {message}. Documentation URL: {documentation_url}")

            repos = response_obj["data"]["search"]["edges"]
            for repo in repos:
                repo_names.append(repo["node"]["nameWithOwner"])

                if len(repo_names) == n:
                    break
            
            page_info = response_obj["data"]["search"]["pageInfo"]

            if not page_info["hasNextPage"]:
                break

            variables["cursor"] = page_info["endCursor"]
        
        return repo_names, len(repo_names) == n
        
        '''
        query = """
            {
              search(query: "language:C language:C++", type: REPOSITORY, first: 100) {
                edges {
                  node {
                    ... on Repository {
                      nameWithOwner,
                      primaryLanguage {
                        name
                      },
                      languages(first: 100) {
                        totalSize
                        edges {
                          size
                          node {
                            name
                            id
                          }
                        }
                      }                      
                    }
                  }
                }
              }
            }
        """
        '''

    def get_branch_zip_url(self, repo_full_name, branch_name):
        return f"https://github.com/{repo_full_name}/archive/refs/heads/{branch_name}.zip"

    def _validate_repo_params(self, owner, repo, full_name):
        if owner and not repo or repo and not owner:
            raise Exception("Repo name must be provided if owner is provided and vice versa")
        
        if not full_name and not owner and not repo:
            raise Exception("Either full_name or owner and repo must be provided")
        
        if full_name and (owner or repo):
            raise Exception("Only one of full_name or owner and repo must be provided")
