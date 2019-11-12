# jHDF Change Log

## v0.5.2
- Fix https://github.com/jamesmudd/jhdf/issues/124 String padding not handled correctly.
- Fix https://github.com/jamesmudd/jhdf/issues/132 Multi dimensional fixed length string datasets read incorrectly.

## v0.5.1
- Fix bug in chunked v4 datasets (added in v0.5.0) where incorrect data was returned if, fixed array or extensible array indexing was used and the dataset dimensions were not a multiple of the chunk dimensions.
- Adds support for enum datasets (which are returned in string form) https://github.com/jamesmudd/jhdf/issues/121
- Adds HdfFile convenience constructors for URI and Path
- Fix https://github.com/jamesmudd/jhdf/issues/125
- Update dependencies
- Refactors test files to separate HDF5 files from scrips.
- Improvements to test coverage.

## v0.5.0
- Adds support for some types (the most common) of chunked v4 datasets:
  - Single chunk
  - Fixed array
  - Extensible array
- Fixes https://github.com/jamesmudd/jhdf/issues/113 fixed length UTF8 datasets can now be read correctly.
- Fixes https://github.com/jamesmudd/jhdf/issues/112 multiple accesses to a global heap object now behave correctly.
- Lots of code cleanup and minor improvements
- Updates dependencies

## v0.4.8
- Add support for reference data type. Thanks to Gisa Meier and JCzogalla https://github.com/jamesmudd/jhdf/pull/106
- Creation order tracking is skipped allowing these files to be read
- FileChannel can now be accessed allowing more low-level access to datasets
- Add version logging when the library is used

## v0.4.7
- Fix bug #101 https://github.com/jamesmudd/jhdf/issues/101
- Add additional testing of attributes
- Add attribute example

## v0.4.6
- Adds support for compound datasets
- Adds support for array data type
- Adds support for reading chunked datasets with Fletcher32 checksums, Note: the checksum is not verified.
- Improved performance of Dataset.isEmpty method
- Dependency updates

## v0.4.5
- Fix #49 - Big (>10x) performance improvement for chunked dataset reads. Chunks are now decompressed in parallel and the resulting data copies are a large as possible.
- Update Gradle to 5.5
- Update test dependencies

## v0.4.4
- Fix #88 error when running on Java 8 
- Improvements to IDE support
- Improvements to exceptions in currently unsupported cases

## v0.4.3
- Initial work for #49 slow chunked dataset reads
- Lots of typos cleaned up
- Add additional build data to MANIFEST.MF

## v0.4.2
- Add support for byte shuffle filter
- Many filter management improvements including support for dynamically loaded filters
- #74 Add support for reading dataset fill values
- Checkstyle added to improve code consistency - not full code formatting yet...
- Update Gradle to 5.4
- Update commons-lang3 to 3.9 (Java 8)
- Update mockito-core to 2.27.+

## v0.4.1
- Add support for broken links
- #70 Add support for attribute and link creation order tracking
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
- Remove Dataset.getDataBuffer - Not all datasets can reasonably support accessing the backing buffer
- Dataset.getMaxSize now always returns a result previously returned Optional if no max size was in the file now it returns the dataset size if no max size is present.
- Remove dependency on org.slf4j.slf4j-simple, now just depends on slf4j-api
- Update SLF4J to 1.8.0-beta4
- Update to Gradle 5.2.1 and Gradle plugins

## v0.3.0
- First release to support reading chunked datasets. (note: v1.8 files only)
- Initial support for compressed datasets, GZIP only at the moment.

## Pre 0.3.0
Lots of initial development towards being able to read HDF5 files in pure Java. See Git history if your interested.