Binary delta patches for directories
====================================

Utilities and library for creating and applying binary patches to file system directories.
It's based on [Javaxdelta](http://javaxdelta.sourceforge.net/) library - java implementation of [xdelta](http://xdelta.org/) binary diff algorithm.
[GDIFF](http://www.w3.org/TR/NOTE-gdiff-19970901) format is used for patches.

It can be used to implement auto-updating for rich-client applications.

Delta-diff and delta-patch utilities are available in the [Downloads section](https://github.com/alexkasko/delta-updater/downloads)
libraries are available in [Maven cental](http://repo1.maven.org/maven2/com/alexkasko/delta/).

Javadocs: [delta-diff](http://alexkasko.github.com/delta-updater/diff-javadocs),
[delta-patch](http://alexkasko.github.com/delta-updater/patch-javadocs)

###Features

 - supports directories with arbitrary structure
 - supports changed, added and deleted files
 - directory patch includes human readable '.index' file
 - streaming patch creation and applying
 - fail-fast patch applying with hash sum checks
 - pure java, tested on linux and windows

###Directory patch creation

 - takes two directories (to create delta between) and `IOFilter` to include/exclude files
 - creates ZIP file (or stream) with GDIFF deltas for all changed files and '.index' text file (with '.index_' prefix)
 with list of unchanged, added, updated and deleted files with SHA1 hash sums

###Patch application

Patches are applied in fail-fast mode, application will be aborted on first wrong hash-sum or IO error.

 - takes directory to patch and patch file (or stream)
 - reads '.index' file and using it for futher steps:
 - checks hash sums for 'unchanged' files
 - reads from stream 'added' files, puts them into directory checking hash sums
 - check hash sums for 'updated' files
 - reads '.gdiff' patches from stream, applies them, checks hash sums for applied files
 - checks hash sums for 'deleted' files
 - deletes 'deleted' files

Using library
-------------

Maven dependency (available in central repository):

    <dependency>
        <groupId>com.alexkasko.delta</groupId>
	    <artifactId>delta-diff</artifactId>
	    <!--artifactId>delta-patch</artifactId-->
	    <version>1.1.1</version>
    </dependency>

For patch file/stream creation you should use one of these methods:

     new DirDeltaCreator().create(oldDirectory, newDirectory, patchFile);
     new DirDeltaCreator().create(oldDirectory, newDirectory, filesFilter, patchOutputStream);

For patch application (it will throw `IOException` on hash-sum error):

    new DirDeltaPatcher().patch(directory, patchFile);
    new DirDeltaPatcher().patch(directory, patchZipInputStream);

Both `DirDeltaCreator` and `DirDeltaPatcher` are thread-safe (stateless).

Using utilities
---------------

`delta-diff` and `delta-patch` programs (they will be put into `delta-updater/delta-xxx/target/delta-xxx-yyy-distr`) can be used as
command line utilities.

Patch creation:

    java -jar delta-diff.jar dir1 dir2 -o patch.zip

Patch application:

    java -jar delta-patch.jar dir patch.zip

How to build
------------

All dependencies are in Maven Central. To build project run:

    mvn clean install

License Information
-------------------

_Note: javaxdelta depends on GNU Trove 1.0.2 library which is released under the [LGPL license](http://www.gnu.org/licenses/lgpl-2.1.html)_.

This project is released under the [MIT License](http://www.opensource.org/licenses/mit-license.php)
(the same license is used by javaxdelta project).

Changelog
---------

**1.1.1** (2014-01-31)

 * remove `NullOutputStream` usage to support recent versions of Guava

**1.1** (2012-10-26)

 * code and dependencies cleanup, maven central upload

**1.0** (2011-11-20)

 * initial version