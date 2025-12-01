import { streamText } from "ai";
import { openai } from "@ai-sdk/openai";

export async function POST(req: Request) {
  const { messages } = await req.json();

  const result = streamText({
    model: openai("gpt-4-turbo"),
    messages,
    system: `You are a helpful AI assistant. Be concise, friendly, and informative.`,
  });

  return result.toDataStreamResponse();
}
