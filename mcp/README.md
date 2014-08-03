# World Downloader (Base Class Mod)

## Setup development environment

* Download [MCP](http://mcp.ocean-labs.de/news.php)
* Extract the zip file into this folder
* Run `decompile.bat` or `decompile.sh`
* Run `createSymLinks.bat` or `createSymLinks.sh` (this is in the repo base directory)
* If you use Eclipe you should use the `eclipse` folder as workspace

## Updating the mod

* TBD (maybe a local(!) git branch of the decompiled MCP sources can help here...)
* The very first thing to do is look for missing field and method names and correct them (see below)

## Missing field and method names

MCP's deobfuscation does not cover 100% of Minecraft's source code. There will always be some obfuscated names in new releases.
Please do not change them in the Java code because this is hard to update in the future and will break the other variants of this mod.

There are "MCP test" mappings available from [here](http://mcpold.ocean-labs.de/files/mcptest/). They are usually more complete than the ones in the MCP zip file. Just place the csv files in the conf folder, run the cleanup script and the decompile script again.

If that still didn't help you can add unobfuscation mappings to MCP manually by editing the files `conf/fields.csv` and `conf/methods.csv`.
To change the field `field_123456_a` to its correct name `chunkCache` add this line to fields.csv:

    field_123456_a,chunkCache,0,For WDL

The same applies to methods ("func_654321_A") but they need to be added to methods.csv.
When finished run the cleanup script and the decompile script again.