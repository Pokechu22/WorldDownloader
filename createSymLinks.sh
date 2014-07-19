#!/bin/sh

# This script creates symbolic links to the "src" folder (that contains WDL's main classes)
#  in every subfolder that needs these shared files.
# This is useful to synchronise our shared code base across all variants of the mod.
#  They create a writable "view" into into another part of the filesystem.
# Symbolic links are transparent to applications and behave like regular files or folders
#  with the exception that deleting a symlink does not delete the linked resources.

# Run this script after you've cloned the repo and anytime this file was changed in a new commit.

ln -sr "src/wdl" "mcp/src/minecraft/wdl"
ln -sr "src/wdl" "forge/src/main/java/wdl"
