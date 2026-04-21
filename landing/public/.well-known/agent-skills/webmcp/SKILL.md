# WebMCP

The homepage registers browser-side tools for AI agents through `navigator.modelContext`.

## Exposed tools
- `biciradar.get_page_context`
- `biciradar.list_supported_cities`
- `biciradar.open_city_page`
- `biciradar.focus_beta_signup`
- `biciradar.open_store_listing`

## Compatibility
- Uses `navigator.modelContext.provideContext({ tools })` when available.
- Falls back to `navigator.modelContext.registerTool(...)` for implementations that expose the newer registration API.
