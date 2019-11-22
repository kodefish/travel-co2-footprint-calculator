#!usr/bin/env python

import numpy as np

from scipy.fftpack import fft
from signal_analysis_utils import *


data = [4, 12, 16, 48, 99, 2, 45, 59]
res = fft(data)
#print(res)

#print(np.linspace(11, 47, 23))

print(get_fft_values(data, 0.01, 8, 0))
