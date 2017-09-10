#!/usr/bin/env python3

import json
import sys
import time
from urllib.request import urlopen

STATS_URL = "http://localhost:18001/stats"
MAXIMUM_TIME_SECONDS = 2 * 60
SLEEPING_INTERVAL_SECONDS = 1

STATUS_CODE_OK = 200

def is_initialised():
    try:
        response = urlopen(STATS_URL)
        if (response.getcode() == STATUS_CODE_OK):
            encoding = response.info().get_content_charset('utf-8')
            content = response.read().decode(encoding)
            return json.loads(content)['initialised']
        else:  
            return False
    except Exception as e:
        return False     
    
def wait_until_cluster_initialised():
    start = time.time()
    elapsed = 0.0
    while elapsed < MAXIMUM_TIME_SECONDS:
        if is_initialised():
            print("Cluster initialised!")
            break
        elapsed = time.time() - start
        print("Cluster not initialised... keep waiting... elapsed time: {1:.2f} seconds.".format(
            SLEEPING_INTERVAL_SECONDS, elapsed))
        time.sleep(SLEEPING_INTERVAL_SECONDS)
        elapsed = time.time() - start
    else:
        sys.exit("Cluster not initialised after {} seconds. I give up!".format(MAXIMUM_TIME_SECONDS))
        
if __name__ == "__main__":
    wait_until_cluster_initialised()