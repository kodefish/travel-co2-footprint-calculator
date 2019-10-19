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

# CLI argument parsing
import argparse

PROCESSED_DATA_PATH = './processed_data'
COLOR_RED = '#ff0000'
COLOR_GREEN = '#00ff00'
COLOR_BLUE = '#0000ff'
COLOR_YELLOW = '#ffff00'
COLOR_PURPLE = '#ff00ff'
COLOR_CYAN = '#00ffff'

COLORS = [COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_YELLOW, COLOR_PURPLE, COLOR_CYAN]

# Main function
def main():
    # Parse CLI arguments
    parser = argparse.ArgumentParser()
    group = parser.add_mutually_exclusive_group()
    group.add_argument('-c', '--column', action='store_true')
    group.add_argument('-g', '--grid', action='store_true')
    args = parser.parse_args()

    # Determine where visualization will be rendered
    output_file('.se_visualization.html', "SE Data visualization")

    # Load csvs
    # ask which user we want to select (default is all)
    user_ids = os.listdir(PROCESSED_DATA_PATH)
    print("Please select a user:")
    for idx, user in enumerate(user_ids):
        print(idx, ":",  user)

    selected_user_idx = int(input("User idx (default is 0): ") or 0)
    selected_user = '/' + user_ids[selected_user_idx]

    print("Please select a leg:")
    leg_ids = os.listdir(PROCESSED_DATA_PATH + '/' + selected_user)
    for idx, leg_id in enumerate(leg_ids):
        print(idx, ":",  leg_id)

    selected_leg_idx = int(input("Leg idx (default is 0): ") or 0)
    selected_leg = '/' + leg_ids[selected_leg_idx]

    # Create num_colxnum_row grid (we have 6 sensors, so a 2x3 grid is nice)
    num_cols = 2
    num_rows = 3
    figures = [[None for i in range(num_cols)] for j in range(num_rows)]

    # Go through every file and plot every column as a function of time
    for idx, csv_file in tqdm(enumerate(os.listdir(PROCESSED_DATA_PATH + selected_user + selected_leg))):
        # Sensor name is just first word of file name
        sensor_name = csv_file.split('_')[0]
        # Read CSV and convert UNIX time to datetime
        sensor_df = pd.read_csv(PROCESSED_DATA_PATH + selected_user + selected_leg + '/' + csv_file).drop(["Unnamed: 0"], axis=1)
        sensor_df['reading_time'] = pd.to_datetime(sensor_df['reading_time'], unit='ms')
        sensor_cds = ColumnDataSource(sensor_df)

        # Set up figure
        sensor_fig = figure(
            x_axis_type='datetime',
            x_axis_label='Reading Time',
            title=sensor_name + ' data',
            toolbar_location='right',
        )

        # Add every column to figure
        for col_idx, col_name in enumerate(sensor_df):
            if not col_name == "reading_time":
                sensor_fig.line('reading_time', col_name,
                                legend=sensor_name + " " + col_name,
                                color=COLORS[col_idx % len(COLORS)],
                                source=sensor_cds)

        # Compute position in the grid
        col = idx % num_cols
        row = (idx - col) // num_cols
        figures[row][col] = sensor_fig

    # Organize layout
    # Create a Panel with a column of all the data and another one with the grid plot (can't really play with grid plot)
    col_layout = column([item for sublist in figures for item in sublist], sizing_mode='stretch_width')
    col_panel = Panel(child=col_layout, title="Column Layout")

    grid_layout = gridplot(figures)
    grid_panel = Panel(child=grid_layout, title="Grid Layout")
    if args.column:
        show(col_layout)
    elif args.grid:
        show(grid_layout)
    else:
        show(Tabs(tabs=[col_panel, grid_panel]))


# Python stuff to keep it tidy
if __name__ == "__main__":
    main()
