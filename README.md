# jHDF - Pure Java HDF5 library
[![jHDF CI](https://github.com/jamesmudd/jhdf/actions/workflows/ci.yml/badge.svg)](https://github.com/jamesmudd/jhdf/actions/workflows/ci.yml) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=jamesmudd_jhdf&metric=coverage)](https://sonarcloud.io/dashboard?id=jamesmudd_jhdf) [![Maven Central](https://img.shields.io/maven-central/v/io.jhdf/jhdf.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.jhdf/jhdf) [![Javadocs](http://javadoc.io/badge/io.jhdf/jhdf.svg)](http://javadoc.io/doc/io.jhdf/jhdf) [![APIdia](https://apidia.net/mvn/io.jhdf/jhdf/badge.svg)](https://apidia.net/mvn/io.jhdf/jhdf) [![JetBrains Supported](https://img.shields.io/badge/supported-project.svg?label=&colorA=grey&colorB=orange&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz48c3ZnIHZlcnNpb249IjEuMSIgaWQ9IkxheWVyXzEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHg9IjBweCIgeT0iMHB4IiB3aWR0aD0iMTRweCIgaGVpZ2h0PSIxNHB4IiB2aWV3Qm94PSIwIDAgMTQgMTQiIGVuYWJsZS1iYWNrZ3JvdW5kPSJuZXcgMCAwIDE0IDE0IiB4bWw6c3BhY2U9InByZXNlcnZlIj48cmVjdCB4PSIxIiB5PSIxMiIgZmlsbD0iI0ZGRkZGRiIgd2lkdGg9IjciIGhlaWdodD0iMSIvPjxwYXRoIGZpbGw9IiNGRkZGRkYiIGQ9Ik0wLjMsNy4zbDEtMS4xYzAuNCwwLjUsMC44LDAuNywxLjMsMC43YzAuNiwwLDEtMC40LDEtMS4yVjFoMS42djQuN2MwLDAuOS0wLjIsMS41LTAuNywxLjlDNC4xLDguMSwzLjQsOC40LDIuNiw4LjRDMS41LDguNCwwLjgsNy45LDAuMyw3LjN6Ii8%2BPHBhdGggZmlsbD0iI0ZGRkZGRiIgZD0iTTYuOCwxaDMuNGMwLjgsMCwxLjUsMC4yLDEuOSwwLjZjMC4zLDAuMywwLjUsMC43LDAuNSwxLjJsMCwwYzAsMC44LTAuNCwxLjMtMSwxLjZDMTIuNSw0LjgsMTMsNS4zLDEzLDYuMmwwLDBjMCwxLjMtMS4xLDItMi43LDJINi44VjF6IE0xMSwzLjFjMC0wLjUtMC40LTAuNy0xLTAuN0g4LjR2MS41aDEuNUMxMC42LDMuOSwxMSwzLjcsMTEsMy4xTDExLDMuMXogTTEwLjIsNS4zSDguNHYxLjZoMS45YzAuNywwLDEuMS0wLjIsMS4xLTAuOGwwLDBDMTEuNCw1LjYsMTEuMSw1LjMsMTAuMiw1LjN6Ii8%2BPHJlY3QgeD0iMSIgeT0iMTIiIGZpbGw9IiNGRkZGRkYiIHdpZHRoPSI3IiBoZWlnaHQ9IjEiLz48L3N2Zz4%3D)](https://www.jetbrains.com/?from=jhdf) [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.3996097.svg)](https://doi.org/10.5281/zenodo.3996097)

This project is a pure Java implementation for accessing HDF5 files. It is written from the file format specification and is not using any HDF Group code, it is *not* a wrapper around the C libraries. The file format specification is available from the HDF Group [here](https://support.hdfgroup.org/documentation/hdf5/latest/_f_m_t3.html). More information on the format is available on [Wikipedia](https://en.wikipedia.org/wiki/Hierarchical_Data_Format). I presented a webinar about jHDF for the HDF Group which is available on [YouTube](https://www.youtube.com/watch?v=pM8FICF4z_U) the example code used and slides can be found [here](https://github.com/jamesmudd/jhdf-webinar).

The intention is to make a clean Java API to access HDF5 data. Currently, reading is very well-supported and writing supports limited use cases. For progress see the [change log](CHANGES.md). Java 8, 11, 17 and 21 are officially supported.

Here is an example of reading a dataset with `jHDF` (see [ReadDataset.java](jhdf/src/main/java/io/jhdf/examples/ReadDataset.java))

```java
try (HdfFile hdfFile = new HdfFile(Paths.get("/path/to/file.hdf5"))) {
	Dataset dataset = hdfFile.getDatasetByPath("/path/to/dataset");
	// data will be a Java array with the dimensions of the HDF5 dataset
	Object data = dataset.getData();
}
```

For an example of traversing the tree inside a HDF5 file see [PrintTree.java](jhdf/src/main/java/io/jhdf/examples/PrintTree.java).

An example of writing a file `jhdf.hdf5` containing a group `group`, with two datasets `ints` and `doubles`

```java
try (WritableHdfFile hdfFile = HdfFile.write(Paths.get("jhdf.hdf5"))) {
    WritableGroup group = hdfFile.putGroup("group");
    group.putDataset("ints", new int[] {1, 2, 3, 4});
    group.putDataset("doubles", new double[] {1.0, 2.0, 3.0, 4.0});
}
```
See [WriteHdf5.java](jhdf/src/main/java/io/jhdf/examples/WriteHdf5.java) for a more extensive complete example. Note: writing files is still an early feature with many more functions to be added.

For more examples see package [io.jhdf.examples](jhdf/src/main/java/io/jhdf/examples)

## Why should I use jHDF?
- Easy integration with JVM based projects. The library is available on [Maven Central](https://central.sonatype.com/artifact/io.jhdf/jhdf), [GitHub Packages](https://github.com/jamesmudd/jhdf/packages/), and [JitPack.io](https://jitpack.io/#jamesmudd/jhdf) so using it should be as easy as adding any other dependency. To use the libraries supplied by the HDF Group you need to load native code, which means you need to handle this in your build, and it complicates distribution of your software on multiple platforms.
- The API design intends to be familiar to Java programmers, so hopefully it works as you might expect. (If this is not the case, open an issue with suggestions for improvement)
- No use of JNI, so you avoid all the issues associated with calling native code from the JVM.
- Fully debug-able you can step fully through the library with a Java debugger.
- Provides access to datasets `ByteBuffer`s to allow for custom reading logic, or integration with other libraries.
- Allows integration with different file system implementations (e.g. zip files, or in-memory) and the ability to read remote files from web servers or object storage like S3.
- Integration with Java logging via SLF4J
- Performance? Maybe, the library uses Java NIO `MappedByteBuffer`s which should provide fast file access. In addition, when accessing chunked datasets, the library is parallelized to take advantage of modern CPUs. `jHDF` will also allow parallel reading of multiple datasets or multiple files. I have seen cases where `jHDF` is significantly faster than the C libraries, but as with all performance issues, it is case-specific, so you will need to do your own tests on the cases you care about. If you do run tests, please post the results so everyone can benefit, here are some results I am aware of:
    - [Peter Kirkham - Parallel IO Improvements](http://pkirkham.github.io/pyrus/parallel-io-improvements/)
    - [Apollo3zehn - hdf5-benchmark](https://github.com/Apollo3zehn/hdf5-benchmark)
- Security - jHDF is pure Java, and therefore benefits from the [memory safety](https://www.memorysafety.org/docs/memory-safety/) provided by the JVM. The HDF5 Group library is written using non-memory safe languages, therefore susceptible to [memory related security bugs](https://cve.mitre.org/cgi-bin/cvekey.cgi?keyword=hdf5).

## Why should I not use jHDF?
If you use HDF5 from Java I think you should give it a try. However, if `jHDF` does not yet support a feature you need, you should receive a `UnsupportedHdfException`, open an issue and support can be added. For scheduling, the features which will allow the most files to be read/written are prioritized. If you really want to use a new feature, feel free to work on it and open a PR. Any help is much appreciated.

## Why did I start jHDF?
Mostly it's a challenge, HDF5 is a fairly complex file format with lots of flexibility, writing a library to access it is interesting. Also, as a widely used file format for storing scientific, engineering, and commercial data, it would seem like a good idea to be able to access HDF5 files with more than one library. In particular, JVM languages are among the most widely used, so having a native HDF5 implementation seems useful.

## Developing jHDF
- Fork this repository and clone your fork
- Inside the `jhdf` directory run `./gradlew build` (`./gradlew.bat build` on Windows) this will run the build and tests fetching dependencies.
- Import the Gradle project `jhdf` into your IDE.
- Make your changes and add tests.
- Run `./gradlew check` to run the build and tests.
- Once you have made any changes, please open a pull request.

To see other available Gradle tasks run `./gradlew tasks`

If you have read this far, please consider staring at this repo. If you are using jHDF in a commercial product, please consider making a donation. Thanks!
