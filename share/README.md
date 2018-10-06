This pseudo-project contains code shared between all versions of the mod.

It does _not_ contain anything directly runnable.  The code must be compiled _with_ another project.

Most code in this project is version-agnostic.  A few files contain version-specific code; these files are named with a version in the name.  They contain package-accessible classes (which are allowed to be in files of any name), which are then either referenced by another public class (e.g. in `wdl.versioned`), or extended by a public class (most other cases, such as GUI code) so that they can be used everywhere.  This approach is used to avoid duplicate per-version code.