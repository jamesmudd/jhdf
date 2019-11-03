# See https://help.zenodo.org/guides/search/ for search syntax

import requests
import urllib
from pprint import pprint

response = requests.get("https://zenodo.org/api/records",
                        params={'access_right': 'open',  # Only open access
                                'file_type': 'hdf5'  # Only HDF5 files
                                })
print("Request URL: " + response.url)
# pprint(r.json()['hits'])

# File name to URL
download = {}

while response.json()['links'].get('next', None):
    response = requests.get(response.json()['links'].get('next', None))
    print(response.status_code)

    # Loop over the records
    for hit in response.json()['hits']['hits']:
        # pprint(hit)
        # Loop over the files in this record
        for file in hit['files']:
            # if file is < 100MB then add it to the list to download
            if file['type'] == 'hdf5' and file['size'] < 10000000:  # < 10MB
                download[file['key']] = file['links']['self']
                print(len(download))
                urllib.request.urlretrieve(file['links']['self'], file['key'])
                break # Only get one file from each record to try and get different uses
    # Don't want to download too much!
    if len(download) > 25:
        break

pprint(download)
