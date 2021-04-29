# SkywarsMapTool

## Interactive tool for configuring skywars maps.

### Designed for the [SkywarsEngine](https://github.com/KJNine/SkywarsEngine) project

This plugin should be loaded onto a build or test server, not a skywars server.

#### Command: /swmaptool <map name>
Activates the map toolbar. (Run the command without arguments to save and exit)

### Main Toolbar

Right click to use a tool. When holding most tools, the targeted block will highlight in colored glass.

* Green Tool: Begin Island
  * Selects the spawnpoint for a spawn island, and swaps to the island toolbar.
* Orange Tool: Center Chest
  * Selects a center chest. (Center chests can be loot-tabled separately from island chests)
* Purple Tool: Center Brewing Stand
  * Selects a center brewing stand. (Center brewing stands can be loot-tabled separately from island brewing stands

### Island Toolbar

* Gray Tool: Reposition Spawnpoint
  * Selects a new spawnpoint for the currently selected island.
* Orange Tool: Island Chest
  * Selects a chest on the current island. (All island chests are filled per-island, not per-chest, don't select a chest from a different island.)
* Purple Tool: Island Brewing Stand
  * Selects a brewing stand on the current island.
* Blue Tool: Save Island
  * Saves the current island settings, then returns to the main toolbar.
