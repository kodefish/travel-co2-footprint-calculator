#!usr/bin/env python
import os
import pandas as pd
from tqdm import tqdm
import numpy as np

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

leg_df = pd.read_csv(LEGS_FILEPATH, index_col=0)

window_size = 10000
keep_unfinished = False

# ask which user we want to select (default is all)
user_ids = []
for file in os.listdir(PROCESSED_DATA_FILE_PATH):
    user_ids.append(file[5:]) # cut off "user_"

print("Please select a user:")
print(0, ":",  "all")
for idx, user in enumerate(user_ids):
    print(idx + 1, ":",  user)

# TODO: for debugging :)
#user_id_idx = int(input("User idx (default is 0): ") or 0) - 1
user_id_idx = 0

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
            
            ## TODO: add more feature extraction here

            ## Accelerator
            acc_window = getWindow(acc_df, boundary_left, boundary_right)

            # mean magnitude
            acc_window["magnitude"] = np.linalg.norm(acc_window[['x','y','z']].values,axis=1)
            features["acc_mean"] = acc_window["magnitude"].mean()

            ## Bluetooth
            # TODO: average connected devices?

            ## Gyro
            # TODO: average "shakiness"?

            ## Location
            loc_window = getWindow(loc_df, boundary_left, boundary_right)
            
            # maximum speed
            features["max_speed"] = min(0, loc_window["speed"].max())

            ## Magnetic Field
            # TODO: How much you turn around
            # (i.e. in a train it should be very stable but on foot you turn left right alot)

            ## WiFi
            # TODO: reading times are scuffed as fuck here. if possible extract avg nearby access points..?

            ## TARGET
            features["mode"] = tmode

            features_df = features_df.append(features, ignore_index=True)

            # New boundaries
            boundary_left = boundary_right
            boundary_right = boundary_right + window_size

features_df.to_csv(OUTPUT_FILENAME)
