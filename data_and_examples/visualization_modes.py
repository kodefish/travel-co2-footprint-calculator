#!/usr/bin/env python
# Data handling
import pandas as pd
import numpy as np

# Bokeh libraries
from bokeh.io import output_file, output_notebook
from bokeh.plotting import figure, show
from bokeh.models import ColumnDataSource
from bokeh.layouts import row, column, gridplot
from bokeh.models.widgets import Tabs, Panel

# Progress bar and directory reading
from tqdm import tqdm
import os


# load the features.csv
# sort by transport mode

# for each feature create a graph and compare how the modes perform for that feature!
# --> how do we order them tho? real time or just stack all together..?


# in the end we should have one graph per feature and in each graph we have all modes so we can compare them based on that feature
