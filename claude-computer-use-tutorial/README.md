# Claude Computer Use Tutorial

Complete code examples from [Build AI Agents That Control Your Screen](https://techyowls.io/blog/claude-computer-use-tutorial).

## Prerequisites

- Python 3.10+
- Anthropic API key with Computer Use access
- macOS (for screen control examples)
- cliclick (`brew install cliclick`)

## Setup

```bash
# Clone repository
git clone https://github.com/Moshiour027/techyowls-io-blog-public.git
cd techyowls-io-blog-public/claude-computer-use-tutorial

# Create virtual environment
python -m venv venv
source venv/bin/activate  # macOS/Linux
# or: .\venv\Scripts\activate  # Windows

# Install dependencies
pip install -e .

# Set API key
export ANTHROPIC_API_KEY="your-api-key"
```

## Project Structure

```
claude-computer-use-tutorial/
├── src/
│   ├── __init__.py
│   ├── basic_agent.py       # Main agent implementation
│   ├── screen_controller.py  # Screen capture and input
│   ├── form_automation.py    # Real-world examples
│   └── utils.py              # Helper functions
├── .env.example
├── pyproject.toml
└── README.md
```

## Usage

### Basic Agent

```python
from src.basic_agent import ComputerUseAgent

agent = ComputerUseAgent()
result = agent.run("Open Safari and search for Python tutorials")
print(result)
```

### Form Automation

```python
from src.form_automation import automate_contact_form

result = automate_contact_form()
```

## Security Warning

⚠️ **Computer Use gives AI control of your computer.**

- Run in a sandboxed environment (VM/Docker)
- Never expose sensitive credentials
- Review agent actions carefully
- Use human confirmation for sensitive operations

## License

MIT - Use freely in your projects.
