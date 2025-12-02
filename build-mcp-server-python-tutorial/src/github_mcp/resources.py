"""
MCP Resources for GitHub API.

Resources are data sources that Claude can read to get context.
"""

import base64
from github_mcp.github_client import get_client, GitHubError


async def get_repo_resource(owner: str, repo: str) -> str:
    """Resource: repo://{owner}/{repo}"""
    client = get_client()

    try:
        data = await client.get(f"/repos/{owner}/{repo}")
    except GitHubError as e:
        return f"Error loading repository: {e.message}"

    return f"""# Repository: {data['full_name']}

## Quick Facts
- **Description:** {data.get('description') or 'None'}
- **Language:** {data.get('language') or 'Not specified'}
- **License:** {data.get('license', {}).get('name') or 'None'}
- **Default Branch:** {data.get('default_branch')}

## Stats
- Stars: {data.get('stargazers_count', 0)}
- Forks: {data.get('forks_count', 0)}
- Open Issues: {data.get('open_issues_count', 0)}

## URLs
- Repository: {data.get('html_url')}
- Clone: {data.get('clone_url')}
"""


async def get_readme_resource(owner: str, repo: str) -> str:
    """Resource: readme://{owner}/{repo}"""
    client = get_client()

    try:
        data = await client.get(f"/repos/{owner}/{repo}/readme")
    except GitHubError:
        return f"No README found for {owner}/{repo}"

    content = data.get("content", "")
    encoding = data.get("encoding", "")

    if encoding == "base64":
        try:
            decoded = base64.b64decode(content).decode("utf-8")
            return f"# README: {owner}/{repo}\n\n{decoded}"
        except Exception:
            return "Error decoding README"

    return "Unsupported encoding"


async def get_issues_resource(owner: str, repo: str) -> str:
    """Resource: issues://{owner}/{repo}"""
    client = get_client()

    try:
        issues = await client.get(
            f"/repos/{owner}/{repo}/issues",
            state="open",
            per_page=25
        )
    except GitHubError as e:
        return f"Error loading issues: {e.message}"

    if not issues:
        return f"No open issues in {owner}/{repo}"

    output = [f"# Open Issues: {owner}/{repo}\n"]

    for issue in issues:
        if issue.get("pull_request"):
            continue

        labels = ", ".join([l["name"] for l in issue.get("labels", [])])
        output.append(f"""
## #{issue['number']}: {issue['title']}
- **Author:** @{issue['user']['login']}
- **Labels:** {labels or 'None'}
- **URL:** {issue['html_url']}
""")

    return "\n".join(output)
