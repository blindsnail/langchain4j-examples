# A2A with Quarkus and LangChain4j

This module exposes LangChain4j creative-writing and style-scoring agents as an A2A JSON-RPC server using the A2A Java SDK. The `A2aClientExample` class calls the server through LangChain4j's `langchain4j-agentic-a2a` integration.

## Run the server

Requires Java 21 and Maven. Set `OPENAI_API_KEY`; compatible OpenAI endpoints can be configured with `OPENAI_BASE_URL` and `OPENAI_MODEL`.

```bash
export OPENAI_API_KEY="..."
cd a2a
mvn quarkus:dev
```

The public agent card is available at `http://localhost:8080/.well-known/agent-card.json`.
The example disables A2A task authorization because it does not configure a
`TaskAuthorizationProvider`. Enable authorization and provide that bean before
deploying outside a local development environment.

## Call the server with LangChain4j

In another terminal, from the `a2a` directory:

```bash
mvn compile exec:java \
  -Dexec.mainClass=dev.langchain4j.examples.a2a.A2aClientExample \
  -Dexec.args="A robot discovers a garden on Mars"
```

Set `A2A_SERVER_URL` to call a server at a different address.

## Server skills

The A2A agent card declares two skills. `generate_story` receives one text part: the topic. `score_style` receives two text parts in order: the story followed by the desired style. The latter returns a numeric score from `0.0` to `1.0`.
