#!/usr/bin/env python
import os
import pandas as pd
from tqdm import tqdm
import numpy as np

# Get all data files
DATA_FILE_PATH = "./example_data"
PROCESSED_DATA_FILE_PATH = "./processed_data"
LEGS_FILENAME = "legs.csv"
LEGS_FILEPATH = DATA_FILE_PATH + "/" + LEGS_FILENAME

leg_df = pd.read_csv(LEGS_FILEPATH, index_col=0)

# ask which user we want to select (default is all)
user_ids = leg_df.user.unique()
print("Please select a user:")
print(0, ":",  "all")
for idx, user in enumerate(user_ids):
    print(idx + 1, ":",  user)

user_id_idx = int(input("User idx (default is 0): ") or 0) - 1

# If all, then user_ids contains all the user ids, otherwise just the one we want
if user_id_idx < 0:
    print ("Aggregating data for all users")
else:
    print ("Selected user", user_ids[user_id_idx])
    user_ids = [user_ids[user_id_idx]]

selected_ids = {}
# For every selected user
for user_id in user_ids:
    print("Please select a leg (just one of the first 10):")
    print(0, ":",  "all")
    leg_ids = leg_df.loc[leg_df.user == user_id].id
    for idx, leg_id in enumerate(leg_ids[0:10]):
        print(idx + 1, ":",  leg_id)

    leg_id_idx = int(input("Leg idx (default is 0): ") or 0) - 1
    if leg_id_idx < 0:
        selected_ids[user_id] = leg_ids
    else:
        selected_ids[user_id] = [leg_ids[leg_id_idx]]

# Init data files (filename, dataframe) tuple
data_dataframes = []
print("Loading sensor csvs")
for file in tqdm(os.listdir(DATA_FILE_PATH)):
    if not file == LEGS_FILENAME:
        # Read in data
        df = pd.read_csv(DATA_FILE_PATH + '/' + file)
        # Rename time reading column (to have the same names everywhere, right now there is 3 different keys for the same value)
        df.rename(columns={
            'reading': 'reading_time',
            'time_of_reading_since_start': 'reading_time'
        }, inplace=True)
        data_dataframes.append((file, df))

print("")

# Create folder for processed data
def create_folder(folder_path):
    if not os.path.exists(folder_path):
        os.makedirs(folder_path)

create_folder(PROCESSED_DATA_FILE_PATH)
# For every user, for every selected leg, for every datafile
for user_id in user_ids:
    # Create user directory in processed
    user_fp = PROCESSED_DATA_FILE_PATH + "/user_" + user_id
    create_folder(user_fp)
    print("Processing legs for user ", user_id)
    for leg_id in tqdm(selected_ids[user_id]):
        # Create leg directory in processed
        leg_fp = user_fp + "/leg_" + str(leg_id).split(".")[0]
        create_folder(leg_fp)
        for data_df in data_dataframes:
            # Create csv file with all sensor readings of the user's trip
            leg_sensor_fp = leg_fp + "/" + data_df[0];
            df = data_df[1]

            # Drop unwanted columns
            drop_cols = ["leg", "user", "Unnamed: 0", "acc"]
            drop_cols = [c for c in drop_cols if c in df.columns]
            leg_sensor_data = df.loc[(df.user == user_id) & (df.leg == leg_id)].drop(drop_cols, axis=1)

            # If sensor data contains x, y, z columns, then compute the mean
            if all([item in leg_sensor_data.columns for item in ['x', 'y', 'z']]):
                leg_sensor_data["magnitude"] = np.linalg.norm(leg_sensor_data[['x','y','z']].values,axis=1)

            leg_sensor_data.to_csv(leg_sensor_fp)
