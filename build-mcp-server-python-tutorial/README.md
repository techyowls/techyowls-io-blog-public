# GitHub MCP Server

A comprehensive MCP (Model Context Protocol) server that connects Claude to the GitHub API.

**Tutorial:** [Build Your First MCP Server in Python](https://techyowls.io/blog/build-mcp-server-python-tutorial)

## Features

### Tools (8)
- `list_repos` - List repositories for a user/org
- `get_file` - Read any file from a repository
- `create_issue` - Create a new issue
- `list_pull_requests` - List PRs with filters
- `get_pull_request` - Get detailed PR info
- `search_code` - Search code across repos
- `list_commits` - Get commit history
- `get_repo_stats` - Repository analytics

### Resources (3)
- `repo://{owner}/{repo}` - Repository metadata
- `readme://{owner}/{repo}` - README content
- `issues://{owner}/{repo}` - Open issues list

### Prompts (3)
- `code_review` - Structured code review workflow
- `release_notes` - Generate release notes from commits
- `bug_report` - Standardized bug report template

## Quick Start

```bash
# Clone this repo
git clone https://github.com/Moshiour027/techyowls-io-blog-public.git
cd techyowls-io-blog-public/build-mcp-server-python-tutorial

# Install uv (if not installed)
curl -LsSf https://astral.sh/uv/install.sh | sh

# Install dependencies
uv sync

# Create .env file
echo "GITHUB_TOKEN=ghp_your_token_here" > .env

# Run the server
uv run github-mcp
```

## Claude Desktop Configuration

Add to `~/Library/Application Support/Claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "github": {
      "command": "uv",
      "args": [
        "--directory",
        "/path/to/build-mcp-server-python-tutorial",
        "run",
        "github-mcp"
      ],
      "env": {
        "GITHUB_TOKEN": "ghp_your_token_here"
      }
    }
  }
}
```

## GitHub Token

Create a token at https://github.com/settings/tokens with these scopes:
- `repo` - Full control of private repositories
- `read:org` - Read org membership
- `read:user` - Read user profile

## Project Structure

```
build-mcp-server-python-tutorial/
├── src/
│   └── github_mcp/
│       ├── __init__.py
│       ├── server.py          # Main MCP server
│       ├── tools.py           # Tool definitions
│       ├── resources.py       # Resource definitions
│       ├── prompts.py         # Prompt templates
│       └── github_client.py   # GitHub API wrapper
├── pyproject.toml
├── .env.example
└── README.md
```

## Example Usage

Once connected to Claude Desktop:

```
"List my GitHub repositories"
"Read the README from owner/repo"
"Create an issue titled 'Bug: Login fails' in my-project"
"Show me open PRs in my-org/my-repo"
"Search for TODO comments in my codebase"
```

## License

MIT

## Links

- [Full Tutorial](https://techyowls.io/blog/build-mcp-server-python-tutorial)
- [MCP Documentation](https://modelcontextprotocol.io)
- [TechyOwls Blog](https://techyowls.io/blog)
