#!usr/bin/env python

import numpy as np

from scipy.fftpack import fft
from signal_analysis_utils import *


data = [4, 12, 16, 48, 99, 2, 45, 59, 4, 12, 16, 48, 99, 2, 45, 59, 4, 12, 16, 48, 99, 2, 45, 59, 4, 12, 16, 48, 99, 2, 45, 59]
res = fft(data)
#print(res)

#print(np.linspace(11, 47, 23))

x, y = get_fft_values(data, 0.01, len(data), 0)

denominator = 10
percentile = 5

print(y)
signal_min = np.percentile(y, percentile)
signal_max = np.percentile(y, 100-percentile)

print(signal_min)
print(signal_max)
mph = signal_min + (signal_max - signal_min)/denominator
mph = 0

peaks = detect_peaks(y, mph)
print("peaks:")
print(peaks)
