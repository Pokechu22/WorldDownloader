REM This script creates symbolic links to the "src" folder (that contains WDL's main classes)
REM  in every subfolder that needs these shared files.
REM This is useful to synchronise our shared code base across all variants of the mod.
REM  They create a writable "view" into into another part of the filesystem.
REM Symbolic links are transparent to applications and behave like regular files or folders
REM  with the exception that deleting a symlink does not delete the linked resources.

REM Run this script after you've cloned the repo and anytime this file was changed in a new commit.

mklink /j "mcp\src\minecraft\wdl" "src\wdl"
mklink /j "forge\src\main\java\wdl" "src\wdl"
