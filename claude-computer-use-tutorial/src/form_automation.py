"""Real-world form automation examples."""

from .basic_agent import ComputerUseAgent


def automate_contact_form() -> str:
    """Fill out a contact form automatically.
    
    This example demonstrates how to:
    - Navigate form fields
    - Enter text data
    - Submit the form
    - Verify success
    
    Returns:
        Result message from the agent
    """
    agent = ComputerUseAgent()
    
    task = """
    Complete the following steps to fill out the contact form:
    1. Find and click on the Name input field
    2. Type "John Doe"
    3. Find and click on the Email input field
    4. Type "john@example.com"
    5. Find and click on the Message text area
    6. Type "Hello, I'm interested in your services. Please contact me."
    7. Find and click the Submit button
    8. Wait for the response and confirm the form was submitted successfully
    
    Report the final status when complete.
    """
    
    result = agent.run(task)
    print(f"Form automation result: {result}")
    return result


def automate_login_flow(email: str, password: str) -> str:
    """Automate a login process.
    
    Args:
        email: User email address
        password: User password
        
    Returns:
        Result message from the agent
    """
    agent = ComputerUseAgent()
    
    # Note: In production, never pass real passwords to AI agents
    task = f"""
    Complete the login process:
    1. Find the username or email input field
    2. Click on the field and type: {email}
    3. Find the password input field  
    4. Click on the field and type: {password}
    5. Find and click the Login or Sign In button
    6. Wait for the page to load
    7. Report what you see after the login attempt
    """
    
    result = agent.run(task)
    return result


def extract_dashboard_data() -> str:
    """Read and summarize data from a dashboard.
    
    This example shows how to use Computer Use for
    data extraction rather than interaction.
    
    Returns:
        Extracted data summary
    """
    agent = ComputerUseAgent()
    
    task = """
    Analyze the dashboard currently displayed on screen:
    1. Take a screenshot to observe the current state
    2. Identify all visible metrics, numbers, and data points
    3. Note any charts or graphs and describe their trends
    4. Look for any alerts, warnings, or status indicators
    5. Summarize all the key information in a structured format
    
    Provide your findings as a clear, organized summary.
    """
    
    result = agent.run(task, max_iterations=5)
    return result


def navigate_and_download(url: str, filename: str) -> str:
    """Navigate to a URL and download a file.
    
    Args:
        url: Website URL to visit
        filename: Expected filename to download
        
    Returns:
        Result message from the agent
    """
    agent = ComputerUseAgent()
    
    task = f"""
    Complete the following download task:
    1. Open Safari or Chrome browser
    2. Navigate to: {url}
    3. Wait for the page to fully load
    4. Find and click the download link or button for: {filename}
    5. If a save dialog appears, confirm the download
    6. Report whether the download started successfully
    """
    
    result = agent.run(task)
    return result


def multi_app_workflow() -> str:
    """Demonstrate working across multiple applications.
    
    Returns:
        Result message from the agent
    """
    agent = ComputerUseAgent()
    
    task = """
    Complete this multi-application workflow:
    1. Take a screenshot of the current screen
    2. Open the Notes application (use Cmd+Space to open Spotlight, then type "Notes")
    3. Create a new note if needed
    4. Type "Meeting notes from Claude agent"
    5. Add today's date on the next line
    6. Switch to Safari (use Cmd+Tab or click the Safari icon)
    7. Report what applications are currently visible
    """
    
    result = agent.run(task)
    return result


if __name__ == "__main__":
    # Example: Run contact form automation
    print("Starting form automation example...")
    automate_contact_form()
