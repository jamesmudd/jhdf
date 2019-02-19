# jHDF - Pure Java HDF5 library
[![Build Status](https://dev.azure.com/jamesmudd/jhdf/_apis/build/status/jhdf-CI)](https://dev.azure.com/jamesmudd/jhdf/_build/latest?definitionId=3) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jamesmudd_jhdf&metric=alert_status)](https://sonarcloud.io/dashboard?id=jamesmudd_jhdf) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=jamesmudd_jhdf&metric=coverage)](https://sonarcloud.io/dashboard?id=jamesmudd_jhdf) [ ![Download](https://api.bintray.com/packages/jamesmudd/jhdf/jhdf/images/download.svg) ](https://bintray.com/jamesmudd/jhdf/jhdf/_latestVersion) [![Javadocs](http://javadoc.io/badge/io.jhdf/jhdf.svg)](http://javadoc.io/doc/io.jhdf/jhdf)

This is a hobby project for me to see if I can write a loader for HDF5 files. The file format specification is available from the HDF Group [here](https://support.hdfgroup.org/HDF5/doc/H5.format.html).

The intension is to make a clean Java API to access HDF5 data. Currently the project is targeting HDF5 1.8 read-only compatibility. For progress see the [change log](CHANGES.md)

Here is an example of reading a dataset with jHDF (see [ReadDataset.java](jhdf/src/main/java/io/jhdf/examples/ReadDataset.java))

```java
File file = new File("/path/to/file.hdf5");
try (HdfFile hdfFile = new HdfFile(file)) {
	Dataset dataset = hdfFile.getDatasetByPath("/path/to/dataset");
	// data will be a Java array with the dimensions of the HDF5 dataset
	Object data = dataset.getData();
}
```

For an example of traversing the tree inside a HDF5 file see [PrintTree.java](jhdf/src/main/java/io/jhdf/examples/PrintTree.java)