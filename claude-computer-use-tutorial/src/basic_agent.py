"""Main Computer Use agent implementation."""

import os
from typing import Any

import anthropic
from dotenv import load_dotenv

from .screen_controller import ScreenController

# Load environment variables
load_dotenv()


class ComputerUseAgent:
    """AI agent that can see and control the computer.
    
    This agent uses Claude's Computer Use API to:
    - Take screenshots and understand screen content
    - Control mouse (move, click, drag, scroll)
    - Control keyboard (type text, press keys)
    - Complete multi-step tasks autonomously
    """
    
    def __init__(
        self,
        display_width: int = 1920,
        display_height: int = 1080,
        model: str = "claude-sonnet-4-20250514"
    ):
        """Initialize the computer use agent.
        
        Args:
            display_width: Screen width in pixels
            display_height: Screen height in pixels
            model: Claude model to use
        """
        self.client = anthropic.Anthropic()
        self.screen = ScreenController(display_width, display_height)
        self.display_width = display_width
        self.display_height = display_height
        self.model = model
        
        # Define computer use tools
        self.tools = [
            {
                "type": "computer_20241022",
                "name": "computer",
                "display_width_px": display_width,
                "display_height_px": display_height,
                "display_number": 1
            }
        ]
        
        # System prompt for the agent
        self.system_prompt = """You are a computer use agent. You can see the screen and control the mouse and keyboard to complete tasks.

Guidelines:
1. Always start by taking a screenshot to see the current state
2. Think step by step about what you need to do
3. Click precisely on UI elements you can see
4. Wait for pages/applications to load after actions
5. Report when the task is complete or if you encounter issues

Be careful and methodical. If something doesn't work, try an alternative approach.
Do not click on elements you cannot clearly see in the screenshot."""
    
    def process_tool_call(self, tool_use: Any) -> dict:
        """Execute a tool call and return the result.
        
        Args:
            tool_use: Tool use block from Claude's response
            
        Returns:
            Tool result dictionary
        """
        name = tool_use.name
        params = tool_use.input
        
        if name != "computer":
            return {
                "type": "tool_result",
                "tool_use_id": tool_use.id,
                "content": f"Unknown tool: {name}",
                "is_error": True
            }
        
        action = params.get("action")
        
        try:
            if action == "screenshot":
                screenshot = self.screen.take_screenshot()
                return {
                    "type": "tool_result",
                    "tool_use_id": tool_use.id,
                    "content": [
                        {
                            "type": "image",
                            "source": {
                                "type": "base64",
                                "media_type": "image/png",
                                "data": screenshot
                            }
                        }
                    ]
                }
            
            elif action == "mouse_move":
                x, y = params["coordinate"]
                self.screen.mouse_move(x, y)
                
            elif action == "left_click":
                coord = params.get("coordinate")
                if coord:
                    x, y = coord
                    self.screen.click(x, y, "left")
                    
            elif action == "right_click":
                coord = params.get("coordinate")
                if coord:
                    x, y = coord
                    self.screen.click(x, y, "right")
                    
            elif action == "double_click":
                coord = params.get("coordinate")
                if coord:
                    x, y = coord
                    self.screen.double_click(x, y)
                    
            elif action == "left_click_drag":
                start = params.get("start_coordinate", params.get("coordinate"))
                end = params.get("end_coordinate")
                if start and end:
                    self.screen.drag(start[0], start[1], end[0], end[1])
                    
            elif action == "type":
                self.screen.type_text(params["text"])
                
            elif action == "key":
                self.screen.press_key(params["text"])
                
            elif action == "scroll":
                x, y = params["coordinate"]
                direction = params.get("direction", "down")
                amount = params.get("amount", 3)
                self.screen.scroll(x, y, direction, amount)
                
            else:
                return {
                    "type": "tool_result",
                    "tool_use_id": tool_use.id,
                    "content": f"Unknown action: {action}",
                    "is_error": True
                }
            
            # Return confirmation with fresh screenshot
            screenshot = self.screen.take_screenshot()
            return {
                "type": "tool_result",
                "tool_use_id": tool_use.id,
                "content": [
                    {
                        "type": "text",
                        "text": f"Action '{action}' completed successfully."
                    },
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": "image/png",
                            "data": screenshot
                        }
                    }
                ]
            }
            
        except Exception as e:
            return {
                "type": "tool_result",
                "tool_use_id": tool_use.id,
                "content": f"Error executing {action}: {str(e)}",
                "is_error": True
            }
    
    def run(self, task: str, max_iterations: int = 25) -> str:
        """Run the agent to complete a task.
        
        Args:
            task: Natural language description of the task
            max_iterations: Maximum number of agent iterations
            
        Returns:
            Final response text or status message
        """
        print(f"🤖 Starting task: {task}")
        print("-" * 50)
        
        # Get initial screenshot
        initial_screenshot = self.screen.take_screenshot()
        
        # Build initial message with screenshot
        messages = [
            {
                "role": "user",
                "content": [
                    {
                        "type": "text",
                        "text": task
                    },
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": "image/png",
                            "data": initial_screenshot
                        }
                    }
                ]
            }
        ]
        
        for iteration in range(max_iterations):
            print(f"📍 Iteration {iteration + 1}/{max_iterations}")
            
            response = self.client.messages.create(
                model=self.model,
                max_tokens=4096,
                system=self.system_prompt,
                tools=self.tools,
                messages=messages,
                betas=["computer-use-2024-10-22"]
            )
            
            # Check for completion
            if response.stop_reason == "end_turn":
                for block in response.content:
                    if hasattr(block, "text"):
                        print(f"✅ Task complete: {block.text[:200]}...")
                        return block.text
                return "Task completed"
            
            # Process any tool uses
            tool_results = []
            for block in response.content:
                if block.type == "tool_use":
                    action = block.input.get("action", "unknown")
                    print(f"   🔧 Action: {action}")
                    result = self.process_tool_call(block)
                    tool_results.append(result)
            
            if tool_results:
                messages.append({"role": "assistant", "content": response.content})
                messages.append({"role": "user", "content": tool_results})
            else:
                # No tool calls and not end_turn - might be stuck
                print("⚠️ No tool calls made, checking response...")
                break
        
        print("⚠️ Max iterations reached")
        return "Max iterations reached without completing task"


def main():
    """Example usage of the Computer Use agent."""
    # Create agent
    agent = ComputerUseAgent()
    
    # Example task
    task = "Open Safari and search for 'Python tutorials'"
    
    # Run the agent
    result = agent.run(task)
    print(f"\nFinal Result:\n{result}")


if __name__ == "__main__":
    main()
