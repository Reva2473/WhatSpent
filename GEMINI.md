# Project Orchestration & Agent Guidelines

## Agent Model Hierarchy

- **Main Orchestrator**: **Gemini 4.0 Argon** (`gemini-4-argon`)
  - Thinking level: `MEDIUM`
  - Configured in `.antigravity/config.yaml` and selected via the Model Selector dropdown in the Antigravity toolbar.
  - The main chat agent acts as the primary orchestrator responsible for deep reasoning, architectural design, planning, decomposing complex tasks, and synthesizing results.

- **Subagents**: **Gemini 3.8 Flash** (`gemini-3.8-flash` / `flash`)
  - Thinking level: `MEDIUM`
  - Defined in `.agents/agents/fast-worker/agent.md` (or `.agents/agents/fast-worker.md`).
  - When delegating any subtasks or invoking subagents via `invoke_subagent`, the orchestrator MUST always explicitly set:
    ```json
    {
      "Model": "flash",
      "TypeName": "fast-worker"
    }
    ```
  - Subagents run concurrently with `thinking_level: "MEDIUM"` to execute implementation, testing, file searching, and refactoring with low latency.
