"""
MCP Demo Server - TechyOwls Tutorial
https://techyowls.io/blog/model-context-protocol-mcp-guide

A simple MCP server demonstrating resources and tools.
"""

import asyncio
import json
from mcp.server import Server
from mcp.server.stdio import stdio_server
from mcp.types import Resource, Tool, TextContent, CallToolResult

# Initialize server
server = Server("demo-server")

# In-memory data store
data_store = {
    "users": [
        {"id": 1, "name": "Alice", "email": "alice@example.com"},
        {"id": 2, "name": "Bob", "email": "bob@example.com"},
        {"id": 3, "name": "Charlie", "email": "charlie@example.com"},
    ],
    "products": [
        {"id": 1, "name": "Widget Pro", "price": 29.99},
        {"id": 2, "name": "Gadget Plus", "price": 49.99},
        {"id": 3, "name": "Tool Kit", "price": 19.99},
    ],
}


@server.list_resources()
async def list_resources():
    """List available data resources."""
    return [
        Resource(
            uri="data://users",
            name="Users Database",
            description="List of all users in the system",
            mimeType="application/json",
        ),
        Resource(
            uri="data://products",
            name="Products Catalog",
            description="Available products and pricing",
            mimeType="application/json",
        ),
    ]


@server.read_resource()
async def read_resource(uri: str):
    """Read resource data by URI."""
    if uri == "data://users":
        return json.dumps(data_store["users"], indent=2)
    elif uri == "data://products":
        return json.dumps(data_store["products"], indent=2)
    raise ValueError(f"Unknown resource: {uri}")


@server.list_tools()
async def list_tools():
    """List available tools."""
    return [
        Tool(
            name="add_user",
            description="Add a new user to the database",
            inputSchema={
                "type": "object",
                "properties": {
                    "name": {"type": "string", "description": "User's full name"},
                    "email": {"type": "string", "description": "User's email address"},
                },
                "required": ["name", "email"],
            },
        ),
        Tool(
            name="search_products",
            description="Search products by name",
            inputSchema={
                "type": "object",
                "properties": {
                    "query": {"type": "string", "description": "Search query"}
                },
                "required": ["query"],
            },
        ),
        Tool(
            name="calculate_total",
            description="Calculate total price for product IDs",
            inputSchema={
                "type": "object",
                "properties": {
                    "product_ids": {
                        "type": "array",
                        "items": {"type": "integer"},
                        "description": "List of product IDs",
                    }
                },
                "required": ["product_ids"],
            },
        ),
    ]


@server.call_tool()
async def call_tool(name: str, arguments: dict) -> CallToolResult:
    """Execute tool with given arguments."""

    if name == "add_user":
        new_id = max(u["id"] for u in data_store["users"]) + 1
        new_user = {
            "id": new_id,
            "name": arguments["name"],
            "email": arguments["email"],
        }
        data_store["users"].append(new_user)
        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=f"Created user with ID {new_id}: {new_user['name']} ({new_user['email']})",
                )
            ]
        )

    elif name == "search_products":
        query = arguments["query"].lower()
        results = [
            p for p in data_store["products"] if query in p["name"].lower()
        ]
        if results:
            return CallToolResult(
                content=[TextContent(type="text", text=json.dumps(results, indent=2))]
            )
        return CallToolResult(
            content=[TextContent(type="text", text=f"No products found for '{query}'")]
        )

    elif name == "calculate_total":
        product_ids = arguments["product_ids"]
        total = sum(
            p["price"] for p in data_store["products"] if p["id"] in product_ids
        )
        found = [p["name"] for p in data_store["products"] if p["id"] in product_ids]
        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=f"Products: {', '.join(found)}\nTotal: ${total:.2f}",
                )
            ]
        )

    raise ValueError(f"Unknown tool: {name}")


async def main():
    """Run the MCP server."""
    async with stdio_server() as (read_stream, write_stream):
        await server.run(
            read_stream,
            write_stream,
            server.create_initialization_options(),
        )


if __name__ == "__main__":
    asyncio.run(main())
