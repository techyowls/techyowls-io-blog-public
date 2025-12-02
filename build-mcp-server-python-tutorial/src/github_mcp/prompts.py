"""
MCP Prompts for GitHub workflows.

Prompts are reusable templates for common tasks.
"""


def code_review_prompt(owner: str, repo: str, pr_number: int) -> str:
    """Prompt: code_review"""
    return f"""# Code Review Request

Review Pull Request #{pr_number} in {owner}/{repo}.

## Checklist
1. **Functionality** - Does it work correctly?
2. **Code Quality** - Is it readable and organized?
3. **Performance** - Any obvious issues?
4. **Security** - Any vulnerabilities?
5. **Testing** - Adequate test coverage?

Use `get_pull_request` to get PR details, then provide your review.
Rate each category and give an overall recommendation.
"""


def release_notes_prompt(owner: str, repo: str, from_tag: str, to_tag: str = "HEAD") -> str:
    """Prompt: release_notes"""
    return f"""# Release Notes Generator

Generate release notes for {owner}/{repo} from {from_tag} to {to_tag}.

## Instructions
1. Use `list_commits` to get commits
2. Categorize by type (feat, fix, docs, etc.)
3. Highlight breaking changes
4. Credit contributors

## Format
- Features
- Bug Fixes
- Documentation
- Contributors
"""


def bug_report_prompt() -> str:
    """Prompt: bug_report"""
    return """# Bug Report Template

Gather this information:
1. Title - Clear bug description
2. Environment - OS, version, etc.
3. Steps to Reproduce
4. Expected vs Actual Behavior
5. Screenshots/Logs

Then use `create_issue` with labels ["bug", "needs-triage"].
"""
