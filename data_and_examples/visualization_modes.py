#!/usr/bin/env python
# Data handling
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# Progress bar and directory reading
from tqdm import tqdm
import os

# data files
FEATURES_FN = "features.csv"

# load the features.csv
# sort by transport mode

# for each feature create a graph and compare how the modes perform for that feature!
# --> how do we order them tho? real time or just stack all together..?


# in the end we should have one graph per feature and in each graph we have all modes so we can compare them based on that feature


def main():
    data = {
        "acc_mean": {},
        "avg_con_bt": {},
        "gyro_mean": {},
        "max_speed": {},
        "avg_speed": {},
        "max_alt_speed": {},
    }

    tmode_names = ["On Foot", "Train", "Bus", "Car", "Tram", "Bicycle", "E-Bike", "Motorbike"]
    data_colors = ["blue", "red", "green", "yellow", "purple", "grey", "purple", "magenta"]
    data_info = {
        "acc_mean": {
            "ylabel": "Magnitude",
            "title": "Accelerator Magnitude Mean",
        },
        "avg_con_bt": {
            "ylabel": "#Devices",
            "title": "Average Connected Bluetooth Devices",
        },
        "gyro_mean": {
            "ylabel": "Magnitude",
            "title": "Gyro Magnitude Mean",
        },
        "max_speed": {
            "ylabel": "m/s",
            "title": "Maximum Speed",
        },
        "avg_speed": {
            "ylabel": "m/s",
            "title": "Average Speed",
        },
        "max_alt_speed": {
            "ylabel": "m/s",
            "title": "Maximum Altitude Speed over Short Time",
        },
    }

    fig = plt.figure()

    # load pre-processed feature file
    features_df = pd.read_csv(FEATURES_FN, index_col=0)
    features_gp = features_df.groupby('mode')

    # loop over all transport modes and save features into data container
    for g in features_gp.groups.items():
        cur_df = features_df.loc[g[1]]
        #print(g[0])
        for f in data:
            data[f][g[0]] = cur_df[f]

    # for each feature, create a graph
    idx = 1
    for f in data:
        # plot all tmodes
#        plt.subplot(6, 1, idx)
        for tmode in data[f]:
            y = data[f][tmode][:250]
            x = range(len(y))
            plt.plot(x, y, color=data_colors[int(tmode)], label=tmode_names[int(tmode)])

        plt.xlabel('Time', fontsize=16)
        plt.ylabel(data_info[f]["ylabel"], fontsize=16)
        plt.suptitle(data_info[f]["title"])
        plt.legend(loc="upper left")
        plt.show()

        avgs = [np.nanmean(data[f][tmode]) for tmode in data[f]]
        x = np.arange(len(data[f]))
        plt.bar(x, avgs, align="center")
        plt.xticks(x, tmode_names)
        plt.ylabel(data_info[f]["ylabel"])
        plt.suptitle("Average: " + data_info[f]["title"])
        plt.show()
        #plt.plot(x, y, color=data_colors[int(tmode)], label=tmode_names[int(tmode)])


        idx += 1

    

if __name__ == "__main__":
    main()
