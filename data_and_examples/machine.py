#!usr/bin/env python
import os
import pandas as pd
import numpy as np
from sklearn.model_selection import StratifiedShuffleSplit
from sklearn.metrics import classification_report
import xgboost as xgb

# will perform a desision tree on the windowed data and test it with part of the available data.

FEATURES_FN = "features.csv" 
features_df = pd.read_csv(FEATURES_FN, index_col=0)

# shuffle data
y = np.array(features_df['mode'])
X = np.array(features_df.drop(['userID', 'legID', 'mode'], axis=1))
X = np.nan_to_num(X)

shuffle_split = StratifiedShuffleSplit(n_splits=1, test_size=0.3, random_state=42)

train_indices, test_indices = next(shuffle_split.split(X=X, y=y))

X_train, X_test = X[train_indices], X[test_indices]
y_train, y_test = y[train_indices], y[test_indices]

print('Shape of X_train', X_train.shape)
print('Shape of y_train', y_train.shape)
print('Shape of X_test', X_test.shape)
print('Shape of y_test', y_test.shape)

# train XGBoost on training set
xgb_classifier = xgb.XGBClassifier(n_jobs=-1, objective='multi:softprob', random_state=42)
xgb_classifier.fit(X_train, y_train)
print('Training done')

# classification
y_pred = xgb_classifier.predict(X_test)
print('Shape of y_pred', y_pred.shape)

print(classification_report(y_test, y_pred, digits=4))

# confusion matrix
confMatrix = pd.crosstab(y_test, y_pred, rownames=['Actual'], colnames=['Predicted'])
print(confMatrix)


# TODO:
#xgb_classifier.fit(X, y)
#xgb_classifier.save_model('xgboost.model', 'xgboost_model')
#print('model exported')
