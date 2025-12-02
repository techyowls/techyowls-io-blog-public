"""
GitHub API Client - Handles all GitHub API interactions.

This module provides a clean interface for GitHub operations,
including error handling, rate limiting, and response parsing.
"""

import os
from typing import Any
from dataclasses import dataclass
from enum import Enum
import httpx
from dotenv import load_dotenv

load_dotenv()


class GitHubError(Exception):
    """Custom exception for GitHub API errors."""
    def __init__(self, message: str, status_code: int = None):
        self.message = message
        self.status_code = status_code
        super().__init__(self.message)


class HttpMethod(Enum):
    GET = "GET"
    POST = "POST"
    PUT = "PUT"
    PATCH = "PATCH"
    DELETE = "DELETE"


@dataclass
class RateLimitInfo:
    """GitHub API rate limit information."""
    limit: int
    remaining: int
    reset_timestamp: int

    @property
    def is_exceeded(self) -> bool:
        return self.remaining == 0


class GitHubClient:
    """
    Async GitHub API client with built-in error handling.

    Features:
    - Automatic authentication
    - Rate limit tracking
    - Comprehensive error handling
    - Response parsing
    """

    BASE_URL = "https://api.github.com"

    def __init__(self, token: str = None):
        self.token = token or os.getenv("GITHUB_TOKEN")
        if not self.token:
            raise ValueError(
                "GitHub token required. Set GITHUB_TOKEN environment variable "
                "or pass token to constructor."
            )

        self.headers = {
            "Authorization": f"Bearer {self.token}",
            "Accept": "application/vnd.github.v3+json",
            "User-Agent": "MCP-GitHub-Server/1.0",
            "X-GitHub-Api-Version": "2022-11-28"
        }

        self.rate_limit: RateLimitInfo | None = None

    def _update_rate_limit(self, response: httpx.Response) -> None:
        """Update rate limit info from response headers."""
        try:
            self.rate_limit = RateLimitInfo(
                limit=int(response.headers.get("X-RateLimit-Limit", 0)),
                remaining=int(response.headers.get("X-RateLimit-Remaining", 0)),
                reset_timestamp=int(response.headers.get("X-RateLimit-Reset", 0))
            )
        except (ValueError, TypeError):
            pass

    async def request(
        self,
        endpoint: str,
        method: HttpMethod = HttpMethod.GET,
        data: dict | None = None,
        params: dict | None = None
    ) -> dict[str, Any] | list:
        """
        Make an authenticated request to the GitHub API.

        Args:
            endpoint: API endpoint (e.g., '/repos/owner/repo')
            method: HTTP method
            data: Request body for POST/PUT/PATCH
            params: Query parameters

        Returns:
            Parsed JSON response

        Raises:
            GitHubError: On API errors
        """
        url = f"{self.BASE_URL}{endpoint}"

        async with httpx.AsyncClient() as client:
            try:
                response = await client.request(
                    method=method.value,
                    url=url,
                    headers=self.headers,
                    json=data,
                    params=params,
                    timeout=30.0
                )

                self._update_rate_limit(response)

                # Handle specific status codes
                if response.status_code == 404:
                    raise GitHubError("Resource not found", 404)
                elif response.status_code == 401:
                    raise GitHubError("Invalid or expired token", 401)
                elif response.status_code == 403:
                    if self.rate_limit and self.rate_limit.is_exceeded:
                        raise GitHubError(
                            f"Rate limit exceeded. Resets at {self.rate_limit.reset_timestamp}",
                            403
                        )
                    raise GitHubError("Access forbidden", 403)
                elif response.status_code == 422:
                    error_data = response.json()
                    message = error_data.get("message", "Validation failed")
                    raise GitHubError(f"Validation error: {message}", 422)

                response.raise_for_status()

                # Handle empty responses
                if response.status_code == 204:
                    return {"success": True}

                return response.json()

            except httpx.HTTPStatusError as e:
                raise GitHubError(
                    f"HTTP error: {e.response.status_code}",
                    e.response.status_code
                )
            except httpx.RequestError as e:
                raise GitHubError(f"Request failed: {str(e)}")

    # Convenience methods
    async def get(self, endpoint: str, **params) -> dict | list:
        return await self.request(endpoint, HttpMethod.GET, params=params)

    async def post(self, endpoint: str, data: dict) -> dict:
        return await self.request(endpoint, HttpMethod.POST, data=data)

    async def patch(self, endpoint: str, data: dict) -> dict:
        return await self.request(endpoint, HttpMethod.PATCH, data=data)

    async def delete(self, endpoint: str) -> dict:
        return await self.request(endpoint, HttpMethod.DELETE)


# Global client instance
_client: GitHubClient | None = None


def get_client() -> GitHubClient:
    """Get or create the global GitHub client."""
    global _client
    if _client is None:
        _client = GitHubClient()
    return _client
