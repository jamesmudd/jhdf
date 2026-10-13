#!/usr/bin/env python3
# -------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
#
# https://jhdf.io
#
# Copyright (c) 2025 James Mudd
#
# MIT License see 'LICENSE' file
# -------------------------------------------------------------------------------
"""Downloads the large chunked HDF5 dataset used by ChunkedDatasetReadBenchmark.

The file is fetched from Zenodo (found using the search approach in
jhdf/src/test/resources/scripts/zenodo.py):

  Record : 19882183 - Scanning transmission electron microscopy data of
           polymer blend semiconductors F8:F8BT
  DOI    : 10.5281/zenodo.19882183
  License: CC-BY-4.0
  File   : 20230722_105647_data_binned2.hdf5 (368 MiB)

The file contains a 4D chunked, gzip compressed dataset:

  Experiments/__unnamed__/data
      shape  = (255, 255, 257, 257)  (~4 GiB uncompressed)
      dtype  = uint8
      chunks = (2, 4, 257, 257)      (~516 KiB per chunk uncompressed)
      compression = gzip

Usage:
    python3 src/jmh/resources/download_benchmark_data.py

The download is verified against the MD5 checksum published by Zenodo.
"""

import hashlib
import sys
from pathlib import Path

import requests

ZENODO_URL = (
    "https://zenodo.org/api/records/19882183/files/20230722_105647_data_binned2.hdf5/content"
)
EXPECTED_MD5 = "d99b3ee896ccbee5e59e35c456008110"
EXPECTED_SIZE = 386_328_553  # bytes

OUTPUT_FILE = Path(__file__).parent / "stem_data_binned2.hdf5"


def download():
    print(f"Downloading {ZENODO_URL}")
    print(f"        -> {OUTPUT_FILE}")
    md5 = hashlib.md5()
    size = 0
    with requests.get(ZENODO_URL, stream=True) as response:
        response.raise_for_status()
        with open(OUTPUT_FILE, "wb") as f:
            for chunk in response.iter_content(chunk_size=8 * 1024 * 1024):
                f.write(chunk)
                md5.update(chunk)
                size += len(chunk)
                print(f"\r{size / 1e6:.0f} / {EXPECTED_SIZE / 1e6:.0f} MB", end="", flush=True)
    print()

    if size != EXPECTED_SIZE:
        sys.exit(f"FAILED: size mismatch {size} != {EXPECTED_SIZE}")
    if md5.hexdigest() != EXPECTED_MD5:
        sys.exit(f"FAILED: md5 mismatch {md5.hexdigest()} != {EXPECTED_MD5}")
    print("Checksum OK")


if __name__ == "__main__":
    if OUTPUT_FILE.exists():
        print(f"{OUTPUT_FILE} already exists, delete it to re-download")
    else:
        download()
