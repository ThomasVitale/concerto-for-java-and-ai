# Kontakt MCP Server

A read-only MCP server for the [Kontakt 8](https://www.native-instruments.com/products/kontakt) libraries installed on this Mac. It reads Kontakt's Browser database (`komplete.db3`) and tells which presets are installed, which ones can be loaded right now, and where each library  keeps its manuals and audio previews. The same catalogue is also available over REST. Nothing here plays sound or writes to Kontakt's files.

## Pre-requisites

* Java 25
* Kontakt 8
* Podman/Docker (for the Grafana LGTM dev service)

## Running the application

Run the application as follows:

```shell
./gradlew bootRun
```

Alternatively, you can use the [Arconia CLI](https://docs.arconia.io/arconia-cli/latest/index.html):

```shell
arconia dev
```

Under the hood, [Arconia Dev Services](https://docs.arconia.io/arconia/latest/dev-services/) start a Grafana LGTM container and send the app's traces, metrics, and logs to it. The Grafana URL is logged at startup.

The application will be accessible at http://localhost:4400, with the MCP endpoint at http://localhost:4400/mcp (streamable HTTP).

## Using the MCP server

The server exposes these tools:

| Tool | Description |
|---|---|
| `search_instruments` | Search presets by name, author, product, bank, Sound Type, Character or comment |
| `get_instrument` | One preset in full: comment, file, preview, library folder and manuals |
| `list_libraries` | Installed libraries with version, preset count and availability |
| `list_sound_types` | Sound Types (Type and Subtype) with counts |
| `list_characters` | Character tags with counts |

It also exposes the `kontakt://summary` resource with headline numbers.

### Claude Code

```shell
claude mcp add --transport http kontakt http://localhost:4400/mcp
```

### Mistral Vibe

```shell
vibe mcp add kontakt --url http://localhost:4400/mcp --no-login
```

Alternatively, add it to your Vibe `~/.vibe/config.toml`:

```toml
[[mcp_servers]]
name = "kontakt"
transport = "streamable-http"
url = "http://localhost:4400/mcp"
```

### OpenCode

In `opencode.jsonc`:

```jsonc
{
  "$schema": "https://opencode.ai/config.json",
  "mcp": {
    "servers": {
      "kontakt": {
        "type": "remote",
        "url": "http://localhost:4400/mcp",
        "oauth": false
      }
    }
  }
}
```

Then check the connection with `opencode mcp list`.

## Using the REST API

You can also browse the catalogue using the [httpie](https://httpie.io/) command line tool.

Search for presets:

```shell
http :4400/api/instruments q=="solo cello legato"
```

Narrow the search by Sound Type, Character or Browser category:

```shell
http :4400/api/instruments q==strings soundType=="Bowed Strings" character==Dark category==Instruments limit==5
```

Include libraries on drives that are not mounted:

```shell
http :4400/api/instruments q==taiko availableOnly==false
```

Get the details of a preset by its ID:

```shell
http :4400/api/instruments/3039
```

Download the audio preview of a preset:

```shell
http --download :4400/api/instruments/3039/preview
```

List the installed libraries, and get the details and presets of one of them:

```shell
http :4400/api/libraries
http ":4400/api/libraries/Albion ONE"
http ":4400/api/libraries/Albion ONE/instruments"
```

List the Sound Types and Characters, and get a summary:

```shell
http :4400/api/sound-types
http :4400/api/characters
http :4400/api/summary
```

## Configuration

| Property | Default | Description |
|---|---|---|
| `kontakt.database` | Kontakt 8's `komplete.db3` | The database file, opened read-only |
| `kontakt.url` | `http://localhost:${server.port}` | Base URL of the manual and preview links |
| `server.port` | `4400` | The HTTP port |

## Running the tests

You can run the tests using the following command:

```shell
./gradlew test
```

Alternatively, you can use the [Arconia CLI](https://docs.arconia.io/arconia-cli/latest/index.html):

```shell
arconia test
```

The tests build a small Kontakt database in a temp folder and call the server as both an MCP client and a REST client. No Kontakt or Docker/Podman needed.
