# jHDF Change Log

## v0.9.1 - March 2025
- Improve Java FileSystem support. Allow use of `FileSystem` implementations that do not support `FileChannel`, also allows wider compatability if memory mapped file access is not possible. This improves the ability to use jHDF with file systems like S3. Thanks to @tbrunsch for this contribution.
- Some improvements to test infrastructure. Also thanks to @tbrunsch
- Build updates

## v0.9.0 - February 2025
- *Breaking API change* Fix typo `WritiableDataset` to `WritableDataset` so you will need to make code updates if you are using writing. Thanks to @jshook
- Allow files contain datatype version 0 to be read. Note a warning will be logged as this is out of spec. https://github.com/jamesmudd/jhdf/issues/524
- Add support for reading the `time` datatype. This is not commonly used as it appears to be poorly specified, but it will be read as byte[] to be interpreted. https://github.com/jamesmudd/jhdf/issues/523
- Build and dependency updates

## v0.8.6 - January 2025
- Add support for reading implicit index chunked datasets https://github.com/jamesmudd/jhdf/issues/651 https://github.com/jamesmudd/jhdf/pull/655
- Test fixes for fixed-array index datasets
- Dependency updates

## v0.8.5 - December 2024
- Fix issue writing string datasets containing non-ASCII characters https://github.com/jamesmudd/jhdf/issues/656
- Add support for reading chunked datasets using fixed-array paging. https://github.com/jamesmudd/jhdf/pull/622
- Allow maven publish tasks to complete without signing, if no signing keys are available. Makes local builds easier and allows building on jitpack.io https://jitpack.io/#jamesmudd/jhdf making a rolling jar release available.
- Dependency updates

## v0.8.4 - October 2024
- Fix incorrectly written string attributes. https://github.com/jamesmudd/jhdf/issues/641
- Dependency updates

## v0.8.3 - October 2024
- Add support for accessing decompressed chunks individually. Thanks to [@marcobitplane](https://github.com/marcobitplane) https://github.com/jamesmudd/jhdf/pull/626
- Fix OSGi headers, and autogenerate them during the build. Thanks to [@mailaender](https://github.com/Mailaender) https://github.com/jamesmudd/jhdf/pull/625 https://github.com/jamesmudd/jhdf/pull/632
- Delete temporary file when closing a file read from an input stream. Thanks to [@ivanwick](https://github.com/ivanwick) https://github.com/jamesmudd/jhdf/issues/262 https://github.com/jamesmudd/jhdf/pull/636
- Build and dependency updates

## v0.8.2 - August 2024
- Add support for writing `boolean` datasets and attributes as Bitfield

## v0.8.1 - August 2024
- Add support fo writing `String` datasets and attributes
- Add `_jHDF` default attribute to root group
- Build and dependency updates

## v0.8.0 - July 2024
- Major writing support improvements
  - Attributes can now be written https://github.com/jamesmudd/jhdf/issues/552
  - Full support for `byte`, `short`, `int`, `long`, `float`, `double` and wrapper classes as datasets and attributes https://github.com/jamesmudd/jhdf/discussions/587
  - Support for scalar datasets and attributes
  - Much more complete API on writable objects, allowing introspection of data type and data layout and data space etc.
- Many test improvements for writing support
- Build and dependency updates
- Note: This may be the last release supporting Java 8

## v0.7.0 - May 2024
- Release adding HDF5 writing support! https://github.com/jamesmudd/jhdf/issues/354. Special thanks to [@thadguidry](https://github.com/thadguidry) for sponsoring this work. See [WriteHdf5.java](jhdf/src/main/java/io/jhdf/examples/WriteHdf5.java) for example usage. Supports
  - Groups
  - n-dimensional `byte`, `int` and `double` datasets
- Fix UTF-8 groups names https://github.com/jamesmudd/jhdf/issues/539
- Java 21 now officially supported
- Adds `h5dump` to CI builds to perform compatability tests.
- Build and dependency updates

## v0.7.0-alpha
- Add initial HDF5 writing support! https://github.com/jamesmudd/jhdf/issues/354. Special thanks to [@thadguidry](https://github.com/thadguidry) for sponsoring this work. See [WriteHdf5.java](jhdf/src/main/java/io/jhdf/examples/WriteHdf5.java) for example usage.
- Build and dependency updates

##  v0.6.10
- Add support for files containing superblock extensions https://github.com/jamesmudd/jhdf/issues/462
- Build and dependency updates

## v0.6.9
- Add support for LZ4 compressed datasets https://github.com/jamesmudd/jhdf/issues/415
- Improve BitShuffle performance and reduce memory usage
- Add CI on ARM64 architecture
- Build and dependency updates

## v0.6.8
- Add support for getting flat data `Dataset#getDataFlat()` https://github.com/jamesmudd/jhdf/issues/397
- Add support for dereferencing addresses/ids `HdfFile.getNodeByAddress(long address) ` https://github.com/jamesmudd/jhdf/issues/316
- Dependency updates

## v0.6.7
- Add support for Bitshuffle filter https://github.com/jamesmudd/jhdf/issues/366
- Add ability to get filter data `Dataset#getFilters();` https://github.com/jamesmudd/jhdf/issues/378
- Dependency and CI updates

## v0.6.6
- Add support for slicing of contiguous datasets. This adds a new method `Dataset#getData(long[] sliceOffset, int[] sliceDimensions)` allowing you to read sections of a dataset that would otherwise be too large in memory. Note: chunked dataset slicing support is still missing.  https://github.com/jamesmudd/jhdf/issues/52 https://github.com/jamesmudd/jhdf/pull/361
- Fix OSGi `Export-Package` header resulting in API access restriction when running in OSGi. https://github.com/jamesmudd/jhdf/issues/365 https://github.com/jamesmudd/jhdf/pull/367
- Dependency and CI updates

## v0.6.5
- Add support for array type data in multi-dimensional datasets https://github.com/jamesmudd/jhdf/issues/341
- Fix issue reading compound type attributes https://github.com/jamesmudd/jhdf/issues/338
- Dependency updates

## v0.6.4
- Fix issue with byte shuffle filter when data length is not a multiple of element length. https://github.com/jamesmudd/jhdf/issues/318
- Improve testing of byte shuffle and deflate filters
- Add validation running on Java 17

## v0.6.3
- Improve support for NIO Path. Allows jHDF to open files on non-default file systems such as zip files or remote storage systems. Thanks, @tbrunsch for this contribution https://github.com/jamesmudd/jhdf/pull/304
- Fix accessing a missing fill value could cause an exception https://github.com/jamesmudd/jhdf/pull/307
- Dependency updates
- CI and release process improvements

## v0.6.2
- *Breaking API change* Dataset#getMaxSize now returns `long[]` allowing files with max sizes larger than `int` max to be opened. https://github.com/jamesmudd/jhdf/pull/283
- Add support for opaque datatype https://github.com/jamesmudd/jhdf/pull/264
- Improve chunked dataset read performance with default logging https://github.com/jamesmudd/jhdf/pull/267
- Dependency updates
- Add GitHub Actions CI
- Switch away from Bintray https://github.com/jamesmudd/jhdf/issues/250

## v0.6.1
- Add support for committed datatypes https://github.com/jamesmudd/jhdf/issues/255
- Add support for attributes with shared datatype
- Switch dependencies repository to Maven Central https://github.com/jamesmudd/jhdf/issues/250
- Code cleanup

## v0.6.0
- Adds support for reading in-memory files from `byte[]` or `ByteBuffers` https://github.com/jamesmudd/jhdf/issues/245
- *Breaking API change* To support in-memory files `HdfFile#getHdfChannel` is replaced by `HdfFile#getHdfBackingStorage` which now returns a `HdfBackingStorage`. Internally the new interface replaces the use of `HdfFileChannel`
- Fix https://github.com/jamesmudd/jhdf/issues/247 reading empty arrays in variable length datasets
- Dependency updates
- Update Gradle

## v0.5.11
- Add LZF compression support allowing LZF datasets to be read. https://github.com/jamesmudd/jhdf/issues/239
- Test dependency updates

## v0.5.10
- Add checksum validation, with "Jenkins Lookup 3 Hash". Will help to detect file corruption.
- Add support for opening a HDF5 file from an InputStream. Many Java API provide InputStreams so this improves integration possibilities.
- Test and coverage improvements
- Test and build dependency updates

## v0.5.9
- Add support for v1 and v2 Data Layout Messages Fix https://github.com/jamesmudd/jhdf/issues/216
- Add support for Old Object Modification Time Message - Improves compatibility with older files
- Fix issue if compact datasets are read multiple times
- Improve handling of empty contiguous datasets
- Test and coverage improvements
- Test dependency updates

## v0.5.8
- *Breaking API change* `Dataset#getDiskSize` is renamed `Dataset#getSizeInBytes` and `Attribute#getDiskSize` is renamed `Attribute#getSizeInBytes`
- New API method `Dataset#getStorageInBytes` which returns the total storage size of the dataset. By comparison with `Dataset#getSizeInBytes` allows the compression ratio to be obtained
- Fixes an issue when reading empty datasets with no allocated storage https://github.com/jamesmudd/jhdf/pull/162
- Code quality improvements and cleanup
- Dependency updates
- CI and build improvements

## v0.5.7
- Fix https://github.com/jamesmudd/jhdf/issues/177 Reading null or padded strings of zero length
- Fix https://github.com/jamesmudd/jhdf/issues/182 Typo in `Dataset.isVariableLength`. *This is an breaking API change* replace calls to `isVariableLentgh()` with `isVariableLength()`
- Add initial support for reading large attributes https://github.com/jamesmudd/jhdf/pull/183
- Dependency updates
- CI and build improvements

## v0.5.6
- Add support for reading half precision (16 bit) floats
- Add support for getting the ByteBuffer backing contiguous datasets and attributes
- Memory usage and performance improvements
- Test coverage improvements
- CI and build improvements

## v0.5.5
- Add support for bitfield datasets https://github.com/jamesmudd/jhdf/issues/84
- Fix https://github.com/jamesmudd/jhdf/issues/157 support nested compound datasets
- Fix https://github.com/jamesmudd/jhdf/issues/159 reading null terminated strings filling their buffer
- Add support for raw chunk access. See https://github.com/jamesmudd/jhdf/blob/master/jhdf/src/main/java/io/jhdf/examples/RawChunkAccess.java
- Fix issues running on systems where default charset is not ASCII/UTF8
- Upgrade to Gradle 6.1.1
- Some CI improvements

## v0.5.4
- Add support for variable length datasets https://github.com/jamesmudd/jhdf/issues/123
- Add support for Compound datatype v3 messages allowing more compound datasets to be read
- Fix https://github.com/jamesmudd/jhdf/issues/139 bug accessing chunked v4 string datasets
- Fix https://github.com/jamesmudd/jhdf/issues/143 bug traversing links
- Code cleanup
- Upgrade to Gradle 6.1
- Update dependencies

## v0.5.3
- Add support for chunked v4 datasets with b-tree chunk indexing
- Improve exceptions for unsupported b-tree records
- Improve test coverage
- Upgrade to Gradle 6.0.1

## v0.5.2
- Fix https://github.com/jamesmudd/jhdf/issues/124 String padding not handled correctly.
- Fix https://github.com/jamesmudd/jhdf/issues/132 Multi dimensional fixed length string datasets read incorrectly.

## v0.5.1
- Fix bug in chunked v4 datasets (added in v0.5.0) where incorrect data was returned if, fixed array or extensible array indexing was used and the dataset dimensions were not a multiple of the chunk dimensions.
- Adds support for enum datasets (which are returned in string form) https://github.com/jamesmudd/jhdf/issues/121
- Adds `HdfFile` convenience constructors for `URI` and `Path`
- Fix https://github.com/jamesmudd/jhdf/issues/125
- Update dependencies
- Refactors test files to separate HDF5 files from scrips.
- Improvements to test coverage.

## v0.5.0
- Adds support for some types (the most common) of chunked v4 datasets:
  - Single chunk
  - Fixed array
  - Extensible array
- Fix https://github.com/jamesmudd/jhdf/issues/113 fixed length UTF8 datasets can now be read correctly.
- Fix https://github.com/jamesmudd/jhdf/issues/112 multiple accesses to a global heap object now behave correctly.
- Lots of code cleanup and minor improvements
- Updates dependencies

## v0.4.8
- Add support for reference data type. Thanks to Gisa Meier and JCzogalla https://github.com/jamesmudd/jhdf/pull/106 https://github.com/jamesmudd/jhdf/issues/91
- Creation order tracking is skipped allowing these files to be read
- `FileChannel` can now be accessed allowing more low-level access to datasets
- Add version logging when the library is used

## v0.4.7
- Fix https://github.com/jamesmudd/jhdf/issues/101
- Add additional testing of attributes
- Add attribute example

## v0.4.6
- Adds support for compound datasets
- Adds support for array data type
- Adds support for reading chunked datasets with Fletcher32 checksums. Note: the checksum is not verified.
- Improved performance of `Dataset.isEmpty` method
- Dependency updates

## v0.4.5
- Fix https://github.com/jamesmudd/jhdf/issues/49 - Big (>10x) performance improvement for chunked dataset reads. Chunks are now decompressed in parallel and the resulting data copies are a large as possible.
- Update Gradle to 5.5
- Update test dependencies

## v0.4.4
- Fix https://github.com/jamesmudd/jhdf/issues/88 error when running on Java 8
- Improvements to IDE support
- Improvements to exceptions in currently unsupported cases

## v0.4.3
- Initial work for #49 slow chunked dataset reads
- Lots of typos cleaned up
- Add additional build data to MANIFEST.MF

## v0.4.2
- Add support for byte shuffle filter
- Many filter management improvements including support for dynamically loaded filters
- Add support for reading dataset fill values https://github.com/jamesmudd/jhdf/issues/74
- Checkstyle added to improve code consistency - not full code formatting yet...
- Update Gradle to 5.4
- Update `commons-lang3` to 3.9 (Java 8)
- Update `mockito-core` to 2.27.+

## v0.4.1
- Add support for broken links
- Add support for attribute and link creation order tracking https://github.com/jamesmudd/jhdf/issues/70
- Allow superblock v1 files to be loaded
- Improve exceptions thrown when lazy loading fails
- Fix bug to allow non-cached groups to be loaded
- Improvement to documentation
- Update Gradle
- Update test dependencies
- Code base cleanup
- Improvements to CI builds and PR validation

## v0.4.0
- Add support for accessing attributes (see [Attribute.java](jhdf/src/main/java/io/jhdf/api/Attribute.java))
- Add support for scalar datasets
- Add support for empty datasets
- Add support for files with user blocks
- Fix bug where "old" style groups containing soft links could not be opened
- Fix bug reading unsigned numbers from "awkward" buffer sizes
- Lots of minor code cleanup and refactoring
- Improvements to tests and coverage

## v0.3.2
- Fix bug when fixed size string datasets contain strings of exactly that size.
- Fix bug where >1D fixed size datasets could not be read
- Add more JavaDoc
- Minor refactoring

## v0.3.1
- Add support for String datasets
- Remove `Dataset.getDataBuffer` - Not all datasets can reasonably support accessing the backing buffer
- `Dataset.getMaxSize` now always returns a result previously returned `Optional` if no max size was in the file now it returns the dataset size if no max size is present.
- Remove dependency on `org.slf4j.slf4j-simple`, now just depends on `slf4j-api`
- Update SLF4J to 1.8.0-beta4
- Update to Gradle 5.2.1 and Gradle plugins

## v0.3.0
- First release to support reading chunked datasets. (note: v1.8 files only)
- Initial support for compressed datasets, GZIP only at the moment.

## Pre 0.3.0
Lots of initial development towards being able to read HDF5 files in pure Java. See Git history if your interested.
