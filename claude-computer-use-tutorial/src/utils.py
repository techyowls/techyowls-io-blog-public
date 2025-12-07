"""Utility functions for Computer Use agents."""

import base64
import os
import time
from datetime import datetime
from io import BytesIO
from typing import Any, Callable

from PIL import Image


def retry_on_failure(
    func: Callable,
    max_retries: int = 3,
    delay: float = 1.0,
    backoff: float = 2.0
) -> Any:
    """Retry a function on failure with exponential backoff.
    
    Args:
        func: Function to execute
        max_retries: Maximum number of retry attempts
        delay: Initial delay between retries in seconds
        backoff: Multiplier for delay after each retry
        
    Returns:
        Result from the function
        
    Raises:
        Exception: If all retries fail
    """
    last_exception = None
    
    for attempt in range(max_retries):
        try:
            return func()
        except Exception as e:
            last_exception = e
            if attempt < max_retries - 1:
                wait_time = delay * (backoff ** attempt)
                print(f"Attempt {attempt + 1} failed: {e}. Retrying in {wait_time:.1f}s...")
                time.sleep(wait_time)
    
    raise last_exception


def validate_coordinates(x: int, y: int, width: int, height: int) -> bool:
    """Validate that coordinates are within screen bounds.
    
    Args:
        x: X coordinate
        y: Y coordinate
        width: Screen width
        height: Screen height
        
    Returns:
        True if coordinates are valid
    """
    return 0 <= x < width and 0 <= y < height


def scale_coordinates(
    x: int,
    y: int,
    from_width: int,
    from_height: int,
    to_width: int,
    to_height: int
) -> tuple[int, int]:
    """Scale coordinates from one resolution to another.
    
    Args:
        x: Original X coordinate
        y: Original Y coordinate
        from_width: Original width
        from_height: Original height
        to_width: Target width
        to_height: Target height
        
    Returns:
        Tuple of (scaled_x, scaled_y)
    """
    scaled_x = int(x * to_width / from_width)
    scaled_y = int(y * to_height / from_height)
    return scaled_x, scaled_y


def optimize_screenshot(
    image_data: bytes,
    max_width: int = 1280,
    quality: int = 85
) -> bytes:
    """Resize and optimize screenshot to reduce API costs.
    
    Args:
        image_data: Original PNG image bytes
        max_width: Maximum width in pixels
        quality: JPEG quality (if converting)
        
    Returns:
        Optimized image bytes
    """
    image = Image.open(BytesIO(image_data))
    
    # Resize if too large
    if image.width > max_width:
        ratio = max_width / image.width
        new_height = int(image.height * ratio)
        image = image.resize((max_width, new_height), Image.LANCZOS)
    
    # Save as optimized PNG
    buffer = BytesIO()
    image.save(buffer, format="PNG", optimize=True)
    return buffer.getvalue()


def save_debug_screenshot(
    image_data: str,
    prefix: str = "debug",
    output_dir: str = "debug_screenshots"
) -> str:
    """Save a base64 screenshot for debugging purposes.
    
    Args:
        image_data: Base64-encoded image string
        prefix: Filename prefix
        output_dir: Directory to save screenshots
        
    Returns:
        Path to saved file
    """
    os.makedirs(output_dir, exist_ok=True)
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
    filename = os.path.join(output_dir, f"{prefix}_{timestamp}.png")
    
    with open(filename, "wb") as f:
        f.write(base64.b64decode(image_data))
    
    print(f"Saved debug screenshot: {filename}")
    return filename


def parse_tool_response(response: Any) -> dict:
    """Parse Claude's response to extract text and tool calls.
    
    Args:
        response: Claude API response object
        
    Returns:
        Dictionary with text, tool_calls, and stop_reason
    """
    result = {
        "text": "",
        "tool_calls": [],
        "stop_reason": response.stop_reason
    }
    
    for block in response.content:
        if hasattr(block, "text"):
            result["text"] += block.text
        elif block.type == "tool_use":
            result["tool_calls"].append({
                "id": block.id,
                "name": block.name,
                "input": block.input
            })
    
    return result


class CostTracker:
    """Track API costs for computer use sessions."""
    
    # Pricing per million tokens (as of Dec 2024)
    PRICING = {
        "claude-sonnet-4-20250514": {"input": 3.0, "output": 15.0},
        "claude-3-5-haiku-20241022": {"input": 0.25, "output": 1.25}
    }
    
    def __init__(self):
        """Initialize cost tracker."""
        self.total_input_tokens = 0
        self.total_output_tokens = 0
        self.requests = 0
        self.history = []
    
    def track(self, response: Any, model: str = "claude-sonnet-4-20250514"):
        """Record usage from a response.
        
        Args:
            response: Claude API response
            model: Model name for pricing
        """
        usage = response.usage
        self.total_input_tokens += usage.input_tokens
        self.total_output_tokens += usage.output_tokens
        self.requests += 1
        
        self.history.append({
            "timestamp": datetime.now().isoformat(),
            "model": model,
            "input_tokens": usage.input_tokens,
            "output_tokens": usage.output_tokens
        })
    
    def get_cost(self, model: str = "claude-sonnet-4-20250514") -> float:
        """Calculate total estimated cost.
        
        Args:
            model: Model name for pricing
            
        Returns:
            Estimated cost in USD
        """
        prices = self.PRICING.get(model, self.PRICING["claude-sonnet-4-20250514"])
        input_cost = (self.total_input_tokens / 1_000_000) * prices["input"]
        output_cost = (self.total_output_tokens / 1_000_000) * prices["output"]
        return input_cost + output_cost
    
    def report(self) -> str:
        """Generate a usage report.
        
        Returns:
            Formatted report string
        """
        cost = self.get_cost()
        return f"""
API Usage Report
================
Requests: {self.requests}
Input tokens: {self.total_input_tokens:,}
Output tokens: {self.total_output_tokens:,}
Estimated cost: ${cost:.4f}
"""


def confirm_action(action: str, params: dict) -> bool:
    """Ask user to confirm before executing sensitive actions.
    
    Args:
        action: Action name
        params: Action parameters
        
    Returns:
        True if user confirms, False otherwise
    """
    sensitive_patterns = [
        "password", "delete", "remove", "sudo",
        "admin", "payment", "transfer", "credentials"
    ]
    
    action_str = f"{action}: {params}"
    is_sensitive = any(p in action_str.lower() for p in sensitive_patterns)
    
    if is_sensitive:
        print(f"\n⚠️  Sensitive action detected: {action}")
        print(f"   Parameters: {params}")
        response = input("Confirm execution? [y/N]: ")
        return response.lower() == "y"
    
    return True
