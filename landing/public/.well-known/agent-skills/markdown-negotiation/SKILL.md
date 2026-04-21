# Markdown Negotiation

When an agent sends `Accept: text/markdown`, HTML pages return a Markdown rendering of the same page.

## Response behavior
- `Content-Type: text/markdown; charset=utf-8`
- `Vary: Accept`
- `x-markdown-tokens` with a rough token count for the generated Markdown

## Scope
- Browsers still receive HTML by default.
- Markdown negotiation applies to safe HTML page requests.
