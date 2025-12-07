"""Screen capture and input simulation for Computer Use agents."""

import base64
import subprocess
from io import BytesIO

from PIL import Image


class ScreenController:
    """Handle screen capture and input simulation.
    
    This implementation is for macOS. For other platforms,
    you'll need to adapt the screenshot and input methods.
    """
    
    def __init__(self, width: int = 1920, height: int = 1080):
        """Initialize screen controller.
        
        Args:
            width: Target display width in pixels
            height: Target display height in pixels
        """
        self.width = width
        self.height = height
    
    def take_screenshot(self) -> str:
        """Capture screen and return base64-encoded PNG.
        
        Returns:
            Base64-encoded PNG image data
        """
        # macOS screenshot to stdout
        result = subprocess.run(
            ["screencapture", "-x", "-C", "-t", "png", "-"],
            capture_output=True
        )
        
        if result.returncode != 0:
            raise RuntimeError(f"Screenshot failed: {result.stderr.decode()}")
        
        # Load and resize if needed
        image = Image.open(BytesIO(result.stdout))
        
        if image.size != (self.width, self.height):
            image = image.resize(
                (self.width, self.height),
                Image.LANCZOS
            )
        
        # Convert to base64
        buffer = BytesIO()
        image.save(buffer, format="PNG", optimize=True)
        return base64.standard_b64encode(buffer.getvalue()).decode()
    
    def mouse_move(self, x: int, y: int):
        """Move mouse cursor to coordinates.
        
        Args:
            x: X coordinate
            y: Y coordinate
        """
        self._validate_coordinates(x, y)
        subprocess.run(["cliclick", f"m:{x},{y}"], check=True)
    
    def click(self, x: int, y: int, button: str = "left"):
        """Click at coordinates.
        
        Args:
            x: X coordinate
            y: Y coordinate
            button: "left" or "right"
        """
        self._validate_coordinates(x, y)
        cmd = "c" if button == "left" else "rc"
        subprocess.run(["cliclick", f"{cmd}:{x},{y}"], check=True)
    
    def double_click(self, x: int, y: int):
        """Double-click at coordinates.
        
        Args:
            x: X coordinate
            y: Y coordinate
        """
        self._validate_coordinates(x, y)
        subprocess.run(["cliclick", f"dc:{x},{y}"], check=True)
    
    def type_text(self, text: str):
        """Type a text string.
        
        Args:
            text: Text to type
        """
        # Escape special characters for cliclick
        escaped = text.replace(":", "\\:")
        subprocess.run(["cliclick", f"t:{escaped}"], check=True)
    
    def press_key(self, key: str):
        """Press a key or key combination.
        
        Args:
            key: Key name (e.g., "Return", "Tab", "ctrl+c")
        """
        key_map = {
            "Return": "return",
            "Tab": "tab", 
            "Escape": "escape",
            "BackSpace": "delete",
            "Delete": "forward-delete",
            "space": "space",
            "Up": "arrow-up",
            "Down": "arrow-down",
            "Left": "arrow-left",
            "Right": "arrow-right"
        }
        
        mapped_key = key_map.get(key, key.lower())
        subprocess.run(["cliclick", f"kp:{mapped_key}"], check=True)
    
    def scroll(self, x: int, y: int, direction: str = "down", amount: int = 3):
        """Scroll at a position.
        
        Args:
            x: X coordinate
            y: Y coordinate
            direction: "up" or "down"
            amount: Number of scroll units
        """
        self._validate_coordinates(x, y)
        scroll_cmd = "su" if direction == "up" else "sd"
        subprocess.run(
            ["cliclick", f"m:{x},{y}", f"{scroll_cmd}:{amount}"],
            check=True
        )
    
    def drag(self, start_x: int, start_y: int, end_x: int, end_y: int):
        """Click and drag from start to end.
        
        Args:
            start_x: Starting X coordinate
            start_y: Starting Y coordinate
            end_x: Ending X coordinate
            end_y: Ending Y coordinate
        """
        self._validate_coordinates(start_x, start_y)
        self._validate_coordinates(end_x, end_y)
        subprocess.run(
            ["cliclick", f"dd:{start_x},{start_y}", f"du:{end_x},{end_y}"],
            check=True
        )
    
    def _validate_coordinates(self, x: int, y: int):
        """Validate coordinates are within bounds.
        
        Args:
            x: X coordinate
            y: Y coordinate
            
        Raises:
            ValueError: If coordinates are out of bounds
        """
        if not (0 <= x < self.width and 0 <= y < self.height):
            raise ValueError(
                f"Coordinates ({x}, {y}) out of bounds "
                f"(0-{self.width}, 0-{self.height})"
            )
