#usr/bin/env python
import sys
import os
import pandas as pd
from tqdm import tqdm
import numpy as np
import matplotlib.pyplot as plt

#from siml.sk_utils import *
from signal_analysis_utils import *

### FUNCTIONALITY ###
# This script will parse all (selected) processed data and aggregate all legs into windows.
# All defined features will be aggregated over the time of one window and the ground truth set according to the mode

# data files
DATA_FILE_PATH = "./example_data"
PROCESSED_DATA_FILE_PATH = "./processed_data"
LEGS_FILENAME = "legs.csv"
LEGS_FILEPATH = DATA_FILE_PATH + "/" + LEGS_FILENAME
OUTPUT_FILENAME = "features.csv"

# map transport mode ID from raw data to machine learning suitable indides (according to the README)
tmode_map = {
    2:   0, # foot
    3:   1, # train
    4:   2, # bus
    5:   3, # car
    304: 4, # tram
    601: 5, # bicycle
    602: 6, # ebike
    605: 7, # motorcycle
}

tmode_names = ["On Foot", "Train", "Bus", "Car", "Tram", "Bicycle", "E-Bike", "Motorbike"]

leg_df = pd.read_csv(LEGS_FILEPATH, index_col=0)

# check if window_size is passed as argument

window_size = 20000

if len(sys.argv) > 1:
    window_size = int(sys.argv[1])

print("Using window_size = " + str(window_size))

# ask which user we want to select (default is all)
user_ids = []
for file in os.listdir(PROCESSED_DATA_FILE_PATH):
    user_ids.append(file[5:]) # cut off "user_"

#print("Please select a user:")
#print(0, ":",  "all")
#for idx, user in enumerate(user_ids):
#    print(idx + 1, ":",  user)

#user_id_idx = int(input("User idx (default is 0): ") or 0) - 1
# TODO: debug
user_id_idx = -1

# If all, then user_ids contains all the user ids, otherwise just the one we want
if user_id_idx < 0:
    print ("Aggregating data for all users")
else:
    print ("Selected user", user_ids[user_id_idx])
    user_ids = [user_ids[user_id_idx]]

# DataFrame to store all features. One row equals one window
features_df = pd.DataFrame()

def getWindow(df, start, end):
    return df.loc[
            (df["reading_time"] > start) &
            (df["reading_time"] <= end)
            ].copy()

# for every selected user, go through all processed legs
selected_ids = {}
for user_id in user_ids:
    user_fp = PROCESSED_DATA_FILE_PATH + "/user_" + user_id
    for file in tqdm(os.listdir(user_fp)):
        leg_id = int(file[4:])

        # totally-not-hacky mode extraction:
        leg_row = leg_df.loc[(leg_df.user == user_id) & (leg_df.id == leg_id)].iloc[0]
        raw_tmode = int(leg_row["mode"])
        tmode = tmode_map[raw_tmode]

        cur_fp = user_fp + "/" + file

        # load csv for this leg
        acc_df = pd.read_csv(cur_fp + "/acc_readings.csv", index_col=0)
        bt_df = pd.read_csv(cur_fp + "/bluetooth_scans.csv", index_col=0)
        gyro_df = pd.read_csv(cur_fp + "/gyro_readings.csv", index_col=0)
        loc_df = pd.read_csv(cur_fp + "/locations_scans.csv", index_col=0)
        magn_df = pd.read_csv(cur_fp + "/magn_readings.csv", index_col=0)
        wifi_df = pd.read_csv(cur_fp + "/wifi_scans.csv", index_col=0)

        boundary_left = leg_row["start"]
        boundary_right = boundary_left + window_size

        while boundary_right < leg_row["end"]:
            features = {}

            ## Accelerator
            acc_window = getWindow(acc_df, boundary_left, boundary_right)

            # mean magnitude
            features["acc_mean"] = acc_window["magnitude"].mean()

            # FFT of acc
            N = acc_window.shape[0]
            if N > 0:
                t_n = window_size / 1000 # in sec
                T = t_n / N
                f_s = 1.0 / T # sample frequency
                denominator = 10

                acc_data = [acc_window["x"], acc_window["y"], acc_window["z"]]
                acc_features = extract_features(acc_data, T, N, f_s, denominator)
                print(len(acc_features))
                features["acc_mixed"] = acc_features
            else:
                features["acc_mixed"] = np.array([0] * 9)

            # visualize!
            do_visualize = False
            if do_visualize:
                fig, (axsX, axsY, axsZ) = plt.subplots(3)
                f_values, fft_values = get_fft_values(acc_window["x"], T, N, f_s)
                axsX.plot(f_values, fft_values, linestyle='-', color='blue')
                axsX.title.set_text("Acc X axis")

                print(T)

                f_values, fft_values = get_fft_values(acc_window["y"], T, N, f_s)
                axsY.plot(f_values, fft_values, linestyle='-', color='red')
                axsY.title.set_text("Acc Y axis")

                f_values, fft_values = get_fft_values(acc_window["z"], T, N, f_s)
                axsZ.plot(f_values, fft_values, linestyle='-', color='green')
                axsZ.title.set_text("Acc Z axis")

                plt.xlabel('Frequency [Hz]', fontsize=16)
                plt.ylabel('Amplitude', fontsize=16)

                plt.suptitle("Transport mode = " + tmode_names[tmode])
                plt.show()

            ## Bluetooth
            bt_window = getWindow(bt_df, boundary_left, boundary_right)

            # average connected devices
            bt_grp = bt_window.groupby("scan")
            features["avg_con_bt"] = bt_grp.size().mean()

            ## Gyro
            gyro_window = getWindow(gyro_df, boundary_left, boundary_right)
            features["gyro_mean"] = gyro_window["magnitude"].mean()

            # FFT magic for gyro
            N = gyro_window.shape[0]
            if N > 0:
                t_n = window_size / 1000 # in sec
                T = t_n / N
                f_s = 1.0 / T # sample frequency
                denominator = 10

                gyro_data = [gyro_window["x"], gyro_window["y"], gyro_window["z"]]
                gyro_features = extract_features(gyro_data, T, N, f_s, denominator)
                features["gyro_mixed"] = gyro_features
            else:
                features["gyro_mixed"] = np.array([0] * 9)

            ## Location
            loc_window = getWindow(loc_df, boundary_left, boundary_right)
            
            # maximum speed
            features["max_speed"] = min(0, loc_window["speed"].max())

            # mean speed
            features["avg_speed"] = loc_window["speed"].mean()

            # maximum altitude "speed" 
            # skip this feature if we're missing altitude infos..
            min_alt = loc_window["alt"].min()
            if min_alt > 0:
                bucket_array = np.linspace(boundary_left, boundary_right, 9) # 8 buckets
                alt_cut = pd.cut(loc_window["reading_time"], bucket_array)
                features["max_alt_speed"] = loc_window.groupby(alt_cut)["alt"].mean().diff().max()

            # min altitude / max altitude -> for debugging
            #features["alt_max"] = loc_window["alt"].max()
            #features["alt_min"] = loc_window["alt"].min()


            ## Magnetic Field
            magn_window = getWindow(magn_df, boundary_left, boundary_right)
            features["mag_mean"] = magn_window["magnitude"].mean()

            # TODO: How much you turn around
            # (i.e. in a train it should be very stable but on foot you turn left right alot)

            ## WiFi
            # TODO: reading times are scuffed as fuck here. if possible extract avg nearby access points..?

            ## TARGET
            features["legID"] = leg_id
            features["userID"] = user_id
            features["mode"] = tmode

            features_df = features_df.append(features, ignore_index=True)

            # New boundaries
            boundary_left = boundary_right
            boundary_right = boundary_right + window_size

features_df.to_csv(OUTPUT_FILENAME)
