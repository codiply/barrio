#!/usr/bin/env python3

import json
from urllib.request import urlopen

STATS_WITH_GARBAGE_COLLECTION_URL = "http://localhost:18001/stats"
    
def print_stats():
    # Perform garbage collection a couple of times
    urlopen(STATS_WITH_GARBAGE_COLLECTION_URL)
    response = urlopen(STATS_WITH_GARBAGE_COLLECTION_URL)
    
    encoding = response.info().get_content_charset('utf-8')
    content = response.read().decode(encoding)
    parsed = json.loads(content)
    pretty = json.dumps(parsed, indent=2, sort_keys=True)
    print(pretty)
        
if __name__ == "__main__":
    print_stats()