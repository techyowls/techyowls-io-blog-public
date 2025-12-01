# Model Context Protocol (MCP) - Code Examples

Working examples from the [MCP tutorial](https://techyowls.io/blog/model-context-protocol-mcp-guide).

## Examples

| Folder | Description |
|--------|-------------|
| `python-server` | Basic Python MCP server with resources and tools |

## Quick Start (Python)

```bash
cd python-server

# Create virtual environment
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run server (for testing)
python server.py
```

## Configure with Claude Desktop

Add to `~/Library/Application Support/Claude/claude_desktop_config.json` (macOS):

```json
{
  "mcpServers": {
    "demo-server": {
      "command": "python",
      "args": ["/path/to/python-server/server.py"]
    }
  }
}
```

Then restart Claude Desktop.

## What You Can Do

Once configured, Claude can:
- List users and products (Resources)
- Add new users (Tool: `add_user`)
- Search products (Tool: `search_products`)
- Calculate totals (Tool: `calculate_total`)

## Learn More

Read the full tutorial: [techyowls.io/blog/model-context-protocol-mcp-guide](https://techyowls.io/blog/model-context-protocol-mcp-guide)
