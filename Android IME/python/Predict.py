from os.path import join
import pickle
import numpy as np
import random
from com.chaquo.python import Python

def start(path):
	model_file = join(path, "RandomForestModel.pickle")

	with open(model_file, 'rb') as f:
		model = pickle.load(f)

	return "Skripta je uspješno učitana."

def test(X, Y, Z, fingerprint, path):

    print("X array length: " + str(len(X)))


    pts = []

    for i in range(len(X)):
        p = []
        p.append(X[i])
        p.append(Y[i])
        p.append(Z[i])

        pts.append(p)

    print(pts)

    return "Done"

def main(X, Y, Z, fingerprint, path):
	model_file = join(path, "RandomForestModel.pickle")

	pts = []
	pointsNumber = len(X)
	print(pointsNumber)

	for i in range(pointsNumber):
	    p = []
	    p.append(X[i])
	    p.append(Y[i])
	    p.append(Z[i])

	    pts.append(p)

	dataset = [pointsNumber, pts]

	average_points = 198
	#average_points = 350
	print("Average number of points: " + str(average_points))
	print("--------------------------------")

	letter_points = dataset[0]
	difference = letter_points - average_points

	if difference > 0:
		while difference > 0:
			random_index = random.randint(0, dataset[0] - 1)
			dataset[1].pop(random_index)
			dataset[0] -= 1
			difference -= 1

	elif difference < 0:
		while difference < 0:
			before_index = random.randint(0, dataset[0] - 2)
			after_index = before_index + 1

			new_x = (dataset[1][before_index][0] + dataset[1][after_index][0]) / 2
			new_y = (dataset[1][before_index][1] + dataset[1][after_index][1]) / 2
			new_z = (dataset[1][before_index][2] + dataset[1][after_index][2]) / 2

			new_point = [new_x, new_y, new_z]

			dataset[1].insert(after_index, new_point)
			dataset[0] += 1

			difference += 1


	X = []

	X = pts

	X_reshaped = []

	X_reshaped = np.array(X).reshape((198*3))



	print([X_reshaped])

	with open(model_file, 'rb') as f:
		model = pickle.load(f)

	y_pred = model.predict([X_reshaped])
	print(y_pred)

	predictions = model.classes_[np.argsort(model.predict_proba([X_reshaped]))[:, :-4 - 1:-1]]
	print(predictions)
	arr = predictions[0]
	print(arr)
	arr = [str(item) for item in arr]
	print(arr)

	return arr