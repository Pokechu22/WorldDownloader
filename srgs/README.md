For version compatibility reasons, some MC classes need to be renamed in the development environment.  These are classes that were renamed between MCP builds, but otherwise are the same; renaming them to a standard name simplifies abstractions against them.

**The way these SRG files work is currently broken**.  Exceptor fails, and thus some parameter namings fail.  However, it currently works well enough for development purposes, and should be mostly replaceable once I figure out the correct way of renaming classes without breaking MCP's naming expectations.

NOTE: The `genSrgs` task is broken in terms of custom SRGs, or at least seems to be.