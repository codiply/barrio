import os
from itertools import product
from random import random, randint, seed

n_files = 4
n_dimensions = 7
min_value = -3
max_value = 3
step = 1

seed(123)

data_folder = "./data/grid/"

if(not os.path.exists(data_folder)):
    os.mkdir(data_folder)

values = [list(range(min_value, max_value + step, step)) for d in range(n_dimensions)]

files = [open(os.path.join(data_folder, "input-" + str(i) + ".txt"), "w") for i in range(1, n_files + 1)]

for i, coordinates in enumerate(product(*values)):
    coordinates_with_noise = [str(x + 0.2 * step * (random() - 0.5)) for x in coordinates]
    file = files[randint(0, n_files - 1)]
    
    file.write("point-" + str(i) + ",")
    file.write(",".join(coordinates_with_noise))
    file.write("\n")
    
for f in files:
    f.close()