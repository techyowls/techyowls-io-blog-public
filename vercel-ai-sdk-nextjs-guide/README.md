# Vercel AI SDK with Next.js - Code Example

Working example from the [Vercel AI SDK tutorial](https://techyowls.io/blog/vercel-ai-sdk-nextjs-guide).

## Features

- Streaming chat with `useChat` hook
- OpenAI GPT-4 integration
- Clean chat UI with Tailwind CSS

## Quick Start

```bash
# Install dependencies
npm install

# Add your API key
cp .env.example .env.local
# Edit .env.local with your OpenAI API key

# Run development server
npm run dev
```

Open [http://localhost:3000](http://localhost:3000)

## Project Structure

```
├── src/app/
│   ├── page.tsx          # Chat UI component
│   ├── layout.tsx        # Root layout
│   ├── globals.css       # Tailwind styles
│   └── api/chat/
│       └── route.ts      # Streaming chat API
├── package.json
└── .env.example
```

## Environment Variables

```bash
OPENAI_API_KEY=sk-...  # Required
```

## Try Different Providers

Edit `src/app/api/chat/route.ts`:

```typescript
// OpenAI
import { openai } from '@ai-sdk/openai';
model: openai('gpt-4-turbo')

// Anthropic
import { anthropic } from '@ai-sdk/anthropic';
model: anthropic('claude-3-5-sonnet-20241022')

// Google
import { google } from '@ai-sdk/google';
model: google('gemini-1.5-pro')
```

## Learn More

Read the full tutorial: [techyowls.io/blog/vercel-ai-sdk-nextjs-guide](https://techyowls.io/blog/vercel-ai-sdk-nextjs-guide)
