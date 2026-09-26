# Item switches live in the common config, not the per-world server config

Every gameplay number of the mod is in `serverconfig/rsm-server.toml`, per world. The switches that turn an item off (`ringEnabled`) are the exception: they are in `config/rsm-common.toml` and apply to every world of that game or server. A switched-off item must lose its recipe, and recipes are loaded before a world's server config is, so a per-world switch could only leave the recipe visible and block crafting afterwards. The server's values are sent to clients on login, so a client's own common config never overrides the server.

## Consequences

- Changing a switch needs a game or server restart.
- In single player one switch covers all worlds; a server owner sets it once for the server.
