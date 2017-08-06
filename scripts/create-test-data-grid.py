import os
from itertools import product
from random import random, randint, seed

# Configuration
min_dimensions = 4
max_dimensions = 7
n_files = 4
min_value = -3
max_value = 3
step = 1
noise_factor = 0.2

seed(123)

data_folder = "./data/"

def createTestDataWithDimensions(n_dimensions):
    folder = os.path.join(data_folder, "grid-" + str(d) + "-d")
    if(not os.path.exists(folder)):
        os.mkdir(folder)

    files = [open(os.path.join(folder, "input-" + str(i) + ".txt"), "w") for i in range(1, n_files + 1)]

    coordinate_values = [list(range(min_value, max_value + step, step)) for d in range(n_dimensions)]

    for i, coordinates in enumerate(product(*coordinate_values)):
        coordinates_with_noise = [str(x + noise_factor * step * (random() - 0.5)) for x in coordinates]
        f = files[randint(0, n_files - 1)]
    
        f.write("point-" + str(i) + ",")
        f.write(",".join(coordinates_with_noise))
        f.write("\n")

    for f in files:
        f.close()
        
for d in range(min_dimensions, max_dimensions + 1):
    createTestDataWithDimensions(d)
