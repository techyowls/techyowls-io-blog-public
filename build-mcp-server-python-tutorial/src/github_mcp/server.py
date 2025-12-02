"""
GitHub MCP Server - Connect Claude to GitHub API

Main entry point for the MCP server.
"""

import os
from dotenv import load_dotenv
from mcp.server.fastmcp import FastMCP

load_dotenv()

if not os.getenv("GITHUB_TOKEN"):
    raise ValueError(
        "GITHUB_TOKEN environment variable is required.\n"
        "Create a token at: https://github.com/settings/tokens"
    )

# Initialize MCP server
mcp = FastMCP(
    "github",
    description="GitHub API integration for Claude"
)

# Import and register tools
from github_mcp.tools import (
    list_repos,
    get_file,
    create_issue,
    list_pull_requests,
    get_pull_request,
    search_code,
    list_commits,
    get_repo_stats
)

mcp.tool()(list_repos)
mcp.tool()(get_file)
mcp.tool()(create_issue)
mcp.tool()(list_pull_requests)
mcp.tool()(get_pull_request)
mcp.tool()(search_code)
mcp.tool()(list_commits)
mcp.tool()(get_repo_stats)

# Import and register resources
from github_mcp.resources import (
    get_repo_resource,
    get_readme_resource,
    get_issues_resource
)

@mcp.resource("repo://{owner}/{repo}")
async def repo_resource(owner: str, repo: str) -> str:
    """Repository metadata and statistics."""
    return await get_repo_resource(owner, repo)

@mcp.resource("readme://{owner}/{repo}")
async def readme_resource(owner: str, repo: str) -> str:
    """Repository README content."""
    return await get_readme_resource(owner, repo)

@mcp.resource("issues://{owner}/{repo}")
async def issues_resource(owner: str, repo: str) -> str:
    """Open issues in a repository."""
    return await get_issues_resource(owner, repo)

# Import and register prompts
from github_mcp.prompts import (
    code_review_prompt,
    release_notes_prompt,
    bug_report_prompt
)

@mcp.prompt()
def code_review(owner: str, repo: str, pr_number: int) -> str:
    """Perform a code review on a pull request."""
    return code_review_prompt(owner, repo, pr_number)

@mcp.prompt()
def release_notes(owner: str, repo: str, from_tag: str, to_tag: str = "HEAD") -> str:
    """Generate release notes from commits."""
    return release_notes_prompt(owner, repo, from_tag, to_tag)

@mcp.prompt()
def bug_report() -> str:
    """Create a standardized bug report."""
    return bug_report_prompt()


def main():
    """Run the MCP server."""
    print("Starting GitHub MCP Server...")
    print("Tools: 8 | Resources: 3 | Prompts: 3")
    mcp.run(transport="stdio")


if __name__ == "__main__":
    main()
