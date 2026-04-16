# Todo API - AI-Assisted Development Demo

This project demonstrates how to build a production-ready REST API using AI coding assistants (Cursor AI and GitHub Copilot).

## 🎯 Learning Objectives

- Generate API endpoints with AI assistance
- Create comprehensive tests using AI
- Write documentation automatically
- Follow best practices suggested by AI

## 🚀 Setup

```bash
npm install
npm run dev
```

## 📌 API Endpoints

### Get All Todos
```
GET /api/todos
```

### Create Todo
```
POST /api/todos
{
  "title": "Buy groceries",
  "completed": false
}
```

### Update Todo
```
PUT /api/todos/:id
{
  "title": "Buy groceries",
  "completed": true
}
```

### Delete Todo
```
DELETE /api/todos/:id
```

## 🤖 AI-Assisted Development Workflow

### Step 1: Generate Boilerplate
**Prompt used in Cursor/Copilot:**
```
Create an Express TypeScript server with:
- CORS and Helmet for security
- Health check endpoint
- Todo routes structure
```

### Step 2: Create Data Models
**Prompt:**
```
Create a Todo interface with id, title, completed, and createdAt fields.
Add type-safe CRUD operations.
```

### Step 3: Generate Tests
**Prompt:**
```
Generate Jest tests for all Todo API endpoints including:
- Success cases
- Error handling
- Edge cases
```

### Step 4: Add Documentation
**Prompt:**
```
Generate comprehensive README with API documentation,
setup instructions, and usage examples.
```

##  🧪 Testing

```bash
npm test
npm run test:watch
```

## 📚 What You Learned

- ✅ How to use AI for rapid API scaffolding
- ✅ AI-assisted test generation
- ✅ Documentation automation
- ✅ TypeScript best practices from AI suggestions

See the [main tutorial](https://techyowls.io/blog/ai-coding-assistants-2025-masterclass) for the complete workflow!
