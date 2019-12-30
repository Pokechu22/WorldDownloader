# World Downloader v4

World Downloader is a mod that allows making backups of Minecraft worlds.  You can view the [Minecraft forum thread](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds) here.  [Project wiki](https://github.com/pokechu22/WorldDownloader/wiki); [issue tracker](https://github.com/Pokechu22/WorldDownloader/issues).

This is a continuation [dslake's original version](https://github.com/dslake/WorldDownloader).

## How do I compile this?

You first need to set up begradle.  Right now, this can be done by downloading [from this repo](https://github.com/Pokechu22/ForgeGradle-extensions) and then running `gradlew install`.  Later this'll be put into a maven repo so that it doesn't need to manually be installed, but at this phase in development, it needs to manually be done.

Once that is set up, you should be able to get everything to work by running `gradlew setupDecompWorkspace build`.  Hopefully.  If something doesn't work quite right, it may be a bug with begradle or another part of the build system; there should be a notification in the most common cases as to what you need to do.

To compile for a single Minecraft version, run `gradlew :version:build` (for instance, `gradlew :1.11.2-litemod:build`).

## What is this branch?

I'm going to be putting v4 development in this branch.

## Why a new version?

I have been working on cleaning up the build process so that I can build for all versions of Minecraft at the same time, rather than developing against one version (usually 1.8) and porting to all the other versions at once.

However, the actual code for the project will remain more or less exactly the same - just moved to a new location within the project.  It's not a rewrite, just a refactoring.

## Why v4 specifically?

There's already `legacy` and `wdl2` branches separate from the `master` branch.  `v4` seems like the next version.  Plus, [4 is the IEEE-vetted random number](https://www.xkcd.com/221/).

## Where's the old code?

On separate branches - `master`, and `1.7.10`/`1.9`/`1.10`...