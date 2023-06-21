import numpy as np
import pickle
import json
import random
from sklearn.model_selection import train_test_split, cross_val_score, KFold
from sklearn.ensemble import RandomForestClassifier
from os.path import join
from com.chaquo.python import Python


def main(path):
    FILES = []
    for i in range(0, 15):
        i += 1
        d = "User" + str(i) + ".json"
        FILES.append(d)

    print("FILES: " + str(FILES))

    def load_data():
        dataset = []
        for_print = []

        for file in FILES:
            filename = join(path, file)
            with open(filename, 'r') as f:
                data = json.load(f)

                for letter in data:
                    num = len(letter["fingerprint"])

                    if num > 0:
                        l = letter["letter"]
                        points = letter["fingerprint"]

                        d = [l, num, points]
                        dataset.append(d)
                        d = []

                        ar = [l, num]
                        for_print.append(ar)

            print(str(file) + " done.")

        return dataset, for_print

    dataset, for_print = load_data()

    print("\nDataset length: " + str(len(dataset)))

    all_points = 0

    for d in dataset:
        all_points += d[1]

    average_points = all_points // len(dataset)
    #average_points = 350
    print("Average number of points: " + str(average_points))
    print("--------------------------------")

    for d in dataset:
        letter_points = d[1]
        difference = letter_points - average_points

        if difference > 0:
            while difference > 0:
                random_index = random.randint(0, d[1]-1)
                d[2].pop(random_index)
                d[1] -= 1
                difference -= 1

        elif difference < 0:
            while difference < 0:
                before_index = random.randint(0, d[1]-2)
                after_index = before_index + 1

                new_x = (d[2][before_index][0] + d[2][after_index][0]) / 2
                new_y = (d[2][before_index][1] + d[2][after_index][1]) / 2
                new_z = (d[2][before_index][2] + d[2][after_index][2]) / 2

                new_point = [new_x, new_y, new_z]

                d[2].insert(after_index, new_point)
                d[1] += 1

                difference += 1

    #random.shuffle(dataset)

    X = []
    y = []

    for label, num, points in dataset:
        X.append(points)
        y.append(label)

    X_reshaped = []

    for x in X:
        x = np.array(x).reshape((198*3))
        X_reshaped.append(x)

    ### SPLIT TRAIN AND TEST DATA ###

    X_train , X_test , y_train, y_test = train_test_split(X_reshaped, y, test_size = 0.15, stratify=y)

    print("X_train length: " + str(len(X_train)) + "  y_train length: " + str(len(y_train)))
    print("X_test length: " + str(len(X_test)) + "  y_test length: " + str(len(y_test)))

    ### RANDOM FOREST ###

    model = RandomForestClassifier(n_estimators=202, min_samples_split=2, min_samples_leaf=1, max_features='log2', max_depth=33, bootstrap=False, random_state=13)
    #kf = KFold(n_splits=10)
    
    #scores = cross_val_score(model, X_train, y_train, cv=kf)
    #cv_result = np.mean(scores)
    #print ("Rezultati cross-validacije: ", scores)
    #print ("Prosječni rezultat cross-validacije: ", cv_result)

    model.fit(X_train , y_train)

    y_pred = model.predict(X_test)

    from sklearn.metrics import accuracy_score, confusion_matrix, classification_report

    accuracy_result = accuracy_score(y_pred,y_test)
    print(classification_report(y_pred,y_test))

    ### RANDOM FOREST TOP-4 ###

    predictions = model.classes_[np.argsort(model.predict_proba(X_test))[:, :-4 - 1:-1]]
    
    print(len(y_pred))
    test_number = len(y_test)
    counter = 0
    top1 = 0
    top2 = 0
    top3 = 0
    top4 = 0

    for i in range(0, len(y_test)):
        if y_test[i] in predictions[i]:
            counter += 1

            if y_test[i] == predictions[i][0]:
                top1 += 1
            elif y_test[i] == predictions[i][1]:
                top2 += 1
            elif y_test[i] == predictions[i][2]:
                top3 += 1
            elif y_test[i] == predictions[i][3]:
                top4 += 1

    counter_per = counter / test_number
    top1_per = top1 / test_number
    top2_per = top2 / test_number
    top3_per = top3 / test_number
    top4_per = top4 / test_number

    import math
    #counter_per = math.floor(counter_per * 10 ** 4) / 10 ** 4
    print("Predicted in top 4: " + str(counter) + " ---> " + str(round(counter_per, 2)))
    print("Top1: " + str(top1) + " ---> " + str(round(top1_per, 2)*100))
    print("Top2: " + str(top2) + " ---> " + str(round(top2_per, 2)*100))
    print("Top3: " + str(top3) + " ---> " + str(round(top3_per, 2)*100))
    print("Top4: " + str(top4) + " ---> " + str(round(top4_per, 2)*100))

    top4_result = round(counter_per, 2)

    scores = cross_val_score(model, X_train, y_train, cv=10)
    cv_result = np.mean(scores)
    print ("Prosječni rezultat je: ", cv_result)

    model_file = join(path, "RandomForestModel.pickle")

    with open(model_file, 'wb') as f:
        pickle.dump(model, f, pickle.HIGHEST_PROTOCOL)

    return "Model je istreniran i spremljen na uređaj.\nProsječna točnost na validacijskom setu: " + str(cv_result) + "\nTočnost na test setu: " + str(accuracy_result) + "\nTočnost za top-4: " + str(top4_result)
