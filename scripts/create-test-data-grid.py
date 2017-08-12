import os
from itertools import product
from random import random, randint, seed

seed(123)

data_folder = "./data/"

def createTestData(
        folder_prefix,
        n_dimensions,
        n_files,
        min_value,
        max_value,
        step,
        noise_min,
        noise_max): 
    folder = os.path.join(data_folder, "%s-%d-d" % (folder_prefix, n_dimensions))
    if(not os.path.exists(folder)):
        os.mkdir(folder)

    files = [open(os.path.join(folder, "input-" + str(i) + ".txt"), "w") for i in range(1, n_files + 1)]

    coordinate_values = [list(range(min_value, max_value + step, step)) for d in range(n_dimensions)]
    
    noise_width = noise_max - noise_min
    
    for i, coordinates in enumerate(product(*coordinate_values)):
        coordinates_with_noise = [str(x + noise_min + noise_width * random()) for x in coordinates]
        f = files[randint(0, n_files - 1)]
    
        f.write("point-" + str(i) + ":::")
        f.write(",".join(coordinates_with_noise))
        f.write("\n")

    for f in files:
        f.close()
       
       
n_files = 4
min_dimensions = 4
max_dimensions = 7
        
for d in range(min_dimensions, max_dimensions + 1):
    createTestData(folder_prefix='grid', n_dimensions=d, n_files=n_files, 
                   min_value=-3, max_value=3, step=1, noise_min=-0.1, noise_max=0.1)
