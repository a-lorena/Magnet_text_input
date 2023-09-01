## Magnet text input
[![hr](https://img.shields.io/badge/lang-hr-blue.svg)](https://github.com/a-lorena/Magnet_text_input/blob/main/README.md)

Contactless text input on mobile phones with the use of permanent magnet, built-in sensors and random forest classifier.

### Development tools
- Android Studio, Java, Chaquopy
- JupyterNotebook, Python, scikit-learn

### Data collection application
Android application developed for the purpose of collecting data and creating a dataset which will later be used for training classification model. By using the built-in magnetometer, the application measures magnetic field values around the device and stores them into a local database.  The data can then be formatted and stored as JSON files. Users were required to write uppercase letters of the English alphabet. The gestures for writing each letter were not predefined because the goal of this project was to get a more "flexible" classification model.

<p align="middle">
  <img src="/Images/DatasetApp.png" width="450" />
  <img src="/Images/DatasetExample.png" width="320" />
</p>

### Calibration
An experiment was conducted to confirm the assumption that by calibrating the magnetic sensor the results obtained will be the same regardless of the location.

<p align="middle">
  <img src="/Images/Kalibracija.png" width="600" />
</p>

### Dimensionality reduction
Dimensionality reduction method was used to determine which position for writing letters, above or next to device, will give better results (so the values for the same letters are as similar as possible). Out of a few tested method the best results were obtained by using t-SNE method with the values of letters written above the device.

<p align="middle">
  <img src="/Images/tSNE.png" width="600" />
</p>

### Classification
After training and testing several classification models with differently formatted dataset the best result was obtained from random forest model that was trained on 3D dataset - X, Y, Z values. Random forest classifier used in this project is the one implemented in scikit-learn library. It was integrated into Android IME with the help of Chaquopy - SDK for running Python scripts in Android applications. Before first use the classifier has to be trained on a mobile device. After training is done, it is automatically stored on the device and may be used anytime.

<p align="middle">
  <img src="/Images/RFConfusionMatrix.png" width="600" />
</p>

### Android IME
User's interaction with mobile device is entirely contactless and based on manipulating the device's ambiental magnetic field. User can write letters and move "pointer" accros IME's "keys" by moving a permanent magnet above the device. When random forest model classifies the written letter it will offer user four letters with the highest probability. User then has to move the "pointer" over the desired letter and confirm the choice by activating the proximity sensor. Space, backspace and enter have their own "keys" which are activated the same way, by using the permanent magnet and the proximity sensor.

<p align="middle">
  <img src="/Images/LetterInput.png" width="600" />
</p>
