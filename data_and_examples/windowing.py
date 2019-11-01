#!usr/bin/env python
import os
import pandas as pd
from tqdm import tqdm

# data files
DATA_FILE_PATH = "./example_data"
PROCESSED_DATA_FILE_PATH = "./processed_data"
LEGS_FILENAME = "legs.csv"
LEGS_FILEPATH = DATA_FILE_PATH + "/" + LEGS_FILENAME

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

window_size_sec = 30
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

# helper functions that:
#   Given a DataFrame of the corresponding sensor
#   Does aggregate (in window_size blocks) the data and stores it in the global Data Frames for the given transport!

def parseAcc(df, tmode):
    # iterate over all columns..
    # while `time passed` < `window_size`
    #   aggregate that shit (min, max, median, ...)
    # save that window as a new row into the global DataFrame for that mode (together with the other sensors --> HOW!?)
    print ("todo..")

def parseBT(df, tmode):
    print ("todo..")


def parseGyro(df, tmode):
    print ("todo..")


def parseLoc(df, tmode):
    print ("todo..")


def parseMagn(df, tmode):
    print ("todo..")


def parseWifi(df, tmode):
    print ("todo..")

# for every selected user, go through all processed legs
selected_ids = {}
for user_id in user_ids:
    user_fp = PROCESSED_DATA_FILE_PATH + "/user_" + user_id
    for file in tqdm(os.listdir(user_fp)):
        leg_id = int(file[4:])
        # totally-not-hacky mode extraction:
        raw_tmode = int(leg_df.loc[(leg_df.user == user_id) & (leg_df.id == leg_id)].iloc[0]["mode"])
        tmode = tmode_map[raw_tmode]

        print("Parsing leg [id=" + str(leg_id) + ", mode=" + str(tmode) + "]")

        cur_fp = user_fp + "/" + file

        parseAcc(pd.read_csv(cur_fp + "/acc_readings.csv"), tmode)
        parseBT(pd.read_csv(cur_fp + "/bluetooth_scans.csv"), tmode)
        parseGyro(pd.read_csv(cur_fp + "/gyro_readings.csv"), tmode)
        parseLoc(pd.read_csv(cur_fp + "/locations_scans.csv"), tmode)
        parseMagn(pd.read_csv(cur_fp + "/magn_readings.csv"), tmode)
        parseWifi(pd.read_csv(cur_fp + "/wifi_scans.csv"), tmode)

        # TODO:
        # go through all csvs of that leg w/ the given mode..

            # aggregate the file based on manual rules (i.e. mean, median, min, max, ..)

            # save all aggregated windows in the DataFrame for that transport mode
            # s.t. in the end we have one DataFrame per mode containing all windows we have for a given mode
            # (independent of the user or actual leg)


