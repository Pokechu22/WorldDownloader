# World Downloader v4

World Downloader is a mod that allows making backups of Minecraft worlds.  You can view the [Minecraft forum thread](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465-1-11-1-10-1-9-4-1-8-9-world-downloader-mod-create) here.  [Project wiki](https://github.com/pokechu22/WorldDownloader/wiki); [issue tracker](https://github.com/Pokechu22/WorldDownloader/issues).

This is a continuation [dslake's original version](https://github.com/dslake/WorldDownloader).

## What is this branch?

I'm going to be putting v4 development in this branch.

## Where's the code for this branch?

There is no code finalized yet.  I'm still figuring out _how_ I'm setting up the build system - once I've got that figured out, I can port the existing code to it.

## Why a new version?

I have been working on cleaning up the build process so that I can build for all versions of Minecraft at the same time, rather than developing against one version (usually 1.8) and porting to all the other versions at once.

However, the actual code for the project will remain more or less exactly the same - just moved to a new location within the project.  It's not a rewrite, just a refactoring.

## Why v4 specifically?

There's already `legacy` and `wdl2` branches separate from the `master` branch.  `v4` seems like the next version.  Plus, [4 is the IEEE-vetted random number](https://www.xkcd.com/221/).

## Where's the old code?

On separate branches - `master`, and `1.7.10`/`1.9`/`1.10`...  If you're interested in the current code, go there.