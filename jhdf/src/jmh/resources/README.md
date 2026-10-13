# JMH benchmark data

The large data files used by the JMH benchmarks are **not** committed to git.
To download them run:

```bash
python3 src/jmh/resources/download_benchmark_data.py
```

from the `jhdf` project directory (or run the script from anywhere, it writes
next to itself).

## `stem_data_binned2.hdf5` (~368 MiB)

Used by `ChunkedDatasetReadBenchmark`. Real-world large chunked dataset from
Zenodo, record [19882183](https://zenodo.org/records/19882183) (DOI:
10.5281/zenodo.19882183, CC-BY-4.0): *Scanning transmission electron
microscopy data of polymer blend semiconductors F8:F8BT*.

| Property | Value |
|---|---|
| Dataset path | `Experiments/__unnamed__/data` |
| Shape | `(255, 255, 257, 257)` (~4 GiB uncompressed) |
| Type | `uint8` |
| Chunk shape | `(2, 4, 257, 257)` (~516 KiB per chunk uncompressed) |
| Compression | gzip |

The file can be placed elsewhere and passed to the benchmark with
`-Djmh.benchmark.data=/path/to/stem_data_binned2.hdf5`.
