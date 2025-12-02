"""
MCP Tools for GitHub API.

Tools are functions that Claude can execute to interact with GitHub.
Each tool has typed parameters and returns formatted strings.
"""

import base64
from github_mcp.github_client import get_client, GitHubError


async def list_repos(
    owner: str,
    repo_type: str = "all",
    sort: str = "updated",
    limit: int = 10
) -> str:
    """
    List GitHub repositories for a user or organization.

    Args:
        owner: GitHub username or organization name
        repo_type: Type of repos - 'all', 'public', 'private', 'sources', 'forks'
        sort: Sort by - 'updated', 'created', 'pushed', 'full_name'
        limit: Maximum number of repos to return (1-100, default: 10)

    Returns:
        Formatted markdown list of repositories with details
    """
    client = get_client()
    limit = min(max(1, limit), 100)

    try:
        repos = await client.get(
            f"/users/{owner}/repos",
            type=repo_type,
            sort=sort,
            per_page=limit
        )
    except GitHubError:
        repos = await client.get(
            f"/orgs/{owner}/repos",
            type=repo_type,
            sort=sort,
            per_page=limit
        )

    if not repos:
        return f"No repositories found for **{owner}**"

    output = [f"## Repositories for {owner}\n"]

    for repo in repos:
        visibility = "Private" if repo.get("private") else "Public"
        stars = repo.get("stargazers_count", 0)
        language = repo.get("language") or "Not specified"
        description = repo.get("description") or "*No description*"

        output.append(f"""
### [{repo['name']}]({repo['html_url']})
{visibility} | Stars: {stars} | {language}

{description}
""")

    return "\n".join(output)


async def get_file(
    owner: str,
    repo: str,
    path: str,
    branch: str = "main"
) -> str:
    """
    Get the contents of a file from a GitHub repository.

    Args:
        owner: Repository owner (username or organization)
        repo: Repository name
        path: Path to file (e.g., 'src/index.ts' or 'README.md')
        branch: Branch name (default: 'main')

    Returns:
        File contents with metadata, or error message
    """
    client = get_client()

    try:
        result = await client.get(
            f"/repos/{owner}/{repo}/contents/{path}",
            ref=branch
        )
    except GitHubError as e:
        if e.status_code == 404:
            return f"File not found: `{path}` in `{owner}/{repo}` (branch: {branch})"
        raise

    if isinstance(result, list):
        files = [f"- {item['name']}" for item in result]
        return f"## Directory: {path}\n\n" + "\n".join(files)

    if result.get("type") != "file":
        return f"Path `{path}` is not a file"

    content = result.get("content", "")
    encoding = result.get("encoding", "")

    if encoding != "base64":
        return f"Unsupported encoding: {encoding}"

    try:
        decoded = base64.b64decode(content).decode("utf-8")
    except Exception as e:
        return f"Failed to decode file: {e}"

    size = result.get("size", 0)

    return f"""## {path}

**Repository:** {owner}/{repo} | **Branch:** {branch} | **Size:** {size} bytes

```
{decoded}
```
"""


async def create_issue(
    owner: str,
    repo: str,
    title: str,
    body: str,
    labels: list[str] | None = None
) -> str:
    """
    Create a new issue in a GitHub repository.

    Args:
        owner: Repository owner (username or organization)
        repo: Repository name
        title: Issue title
        body: Issue description (supports Markdown)
        labels: Optional list of label names to apply

    Returns:
        Success message with issue URL and details
    """
    client = get_client()

    data = {"title": title, "body": body}
    if labels:
        data["labels"] = labels

    result = await client.post(f"/repos/{owner}/{repo}/issues", data)

    return f"""## Issue Created Successfully!

**Title:** {result['title']}
**Number:** #{result['number']}
**URL:** {result['html_url']}
"""


async def list_pull_requests(
    owner: str,
    repo: str,
    state: str = "open",
    limit: int = 10
) -> str:
    """
    List pull requests for a repository.

    Args:
        owner: Repository owner
        repo: Repository name
        state: PR state - 'open', 'closed', 'all'
        limit: Maximum number of PRs to return (1-100)

    Returns:
        Formatted list of pull requests
    """
    client = get_client()
    limit = min(max(1, limit), 100)

    prs = await client.get(
        f"/repos/{owner}/{repo}/pulls",
        state=state,
        per_page=limit
    )

    if not prs:
        return f"No {state} pull requests found in **{owner}/{repo}**"

    output = [f"## Pull Requests: {owner}/{repo}\n"]

    for pr in prs:
        output.append(f"""
### [#{pr['number']}]({pr['html_url']}) {pr['title']}
**Author:** @{pr['user']['login']} | `{pr['head']['ref']}` -> `{pr['base']['ref']}`
""")

    return "\n".join(output)


async def get_pull_request(
    owner: str,
    repo: str,
    pr_number: int
) -> str:
    """
    Get detailed information about a specific pull request.

    Args:
        owner: Repository owner
        repo: Repository name
        pr_number: Pull request number

    Returns:
        Detailed PR information
    """
    client = get_client()

    pr = await client.get(f"/repos/{owner}/{repo}/pulls/{pr_number}")
    files = await client.get(f"/repos/{owner}/{repo}/pulls/{pr_number}/files")

    file_list = [f"- `{f['filename']}` (+{f['additions']}/-{f['deletions']})"
                 for f in files[:20]]

    return f"""## Pull Request #{pr['number']}: {pr['title']}

**State:** {pr['state'].upper()}
**Author:** @{pr['user']['login']}
**Branch:** `{pr['head']['ref']}` -> `{pr['base']['ref']}`
**URL:** {pr['html_url']}

### Stats
- Commits: {pr.get('commits', 0)}
- Changed Files: {pr.get('changed_files', 0)}
- Additions: +{pr.get('additions', 0)}
- Deletions: -{pr.get('deletions', 0)}

### Description
{pr.get('body') or '*No description*'}

### Files Changed
{chr(10).join(file_list)}
"""


async def search_code(
    query: str,
    owner: str | None = None,
    repo: str | None = None,
    limit: int = 10
) -> str:
    """
    Search for code across GitHub repositories.

    Args:
        query: Search query
        owner: Optional - limit to specific user/org
        repo: Optional - limit to specific repository
        limit: Maximum results (1-100)

    Returns:
        Formatted search results
    """
    client = get_client()
    limit = min(max(1, limit), 100)

    q = query
    if owner and repo:
        q += f" repo:{owner}/{repo}"
    elif owner:
        q += f" user:{owner}"

    result = await client.get("/search/code", q=q, per_page=limit)
    items = result.get("items", [])

    if not items:
        return f"No code found matching: `{query}`"

    output = [f"## Code Search Results\n**Query:** `{q}`\n"]

    for item in items:
        output.append(f"- [{item['path']}]({item['html_url']}) in `{item['repository']['full_name']}`")

    return "\n".join(output)


async def list_commits(
    owner: str,
    repo: str,
    branch: str = "main",
    limit: int = 10
) -> str:
    """
    Get commit history for a repository.

    Args:
        owner: Repository owner
        repo: Repository name
        branch: Branch name (default: 'main')
        limit: Number of commits to return (1-100)

    Returns:
        Formatted list of commits
    """
    client = get_client()
    limit = min(max(1, limit), 100)

    commits = await client.get(
        f"/repos/{owner}/{repo}/commits",
        sha=branch,
        per_page=limit
    )

    if not commits:
        return f"No commits found on branch `{branch}`"

    output = [f"## Commit History: {owner}/{repo}\n*Branch: `{branch}`*\n"]

    for commit in commits:
        sha = commit['sha'][:7]
        message = commit['commit']['message'].split('\n')[0]
        author = commit['commit']['author']['name']
        date = commit['commit']['author']['date'][:10]

        output.append(f"- [`{sha}`]({commit['html_url']}) {message} - {author} ({date})")

    return "\n".join(output)


async def get_repo_stats(owner: str, repo: str) -> str:
    """
    Get comprehensive statistics for a repository.

    Args:
        owner: Repository owner
        repo: Repository name

    Returns:
        Repository analytics
    """
    client = get_client()

    repo_info = await client.get(f"/repos/{owner}/{repo}")
    languages = await client.get(f"/repos/{owner}/{repo}/languages")

    total_bytes = sum(languages.values()) if languages else 0
    lang_list = []
    for lang, bytes_count in sorted(languages.items(), key=lambda x: -x[1])[:5]:
        pct = (bytes_count / total_bytes * 100) if total_bytes > 0 else 0
        lang_list.append(f"- {lang}: {pct:.1f}%")

    return f"""## Repository Stats: {owner}/{repo}

### Overview
- **Stars:** {repo_info.get('stargazers_count', 0)}
- **Forks:** {repo_info.get('forks_count', 0)}
- **Open Issues:** {repo_info.get('open_issues_count', 0)}
- **License:** {repo_info.get('license', {}).get('name', 'None')}

### Languages
{chr(10).join(lang_list) or 'No language data'}

### Description
{repo_info.get('description') or '*No description*'}
"""
