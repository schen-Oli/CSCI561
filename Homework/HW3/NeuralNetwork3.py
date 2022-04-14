import numpy as np
import time
import sys

is_test = True

args_cmd = sys.argv
train_images = str(args_cmd[1])
train_labels = str(args_cmd[2]) 
test_images = str(args_cmd[3])

starttime = time.time()
pixel_num = 784
first_layer_size = 32
second_layer_size = 16
num_of_class = 10
learning_rate = 0.05
epochs = 150

test_data = np.genfromtxt(test_images, delimiter=",",  dtype=np.float32)/255
test_label = None
if is_test:
    test_label = np.genfromtxt("test_label.csv", dtype=np.int8)
train_data = np.genfromtxt(train_images, delimiter=",", dtype=np.float32)/255
train_label = np.genfromtxt(train_labels, dtype=np.int8)

w1 = np.random.uniform(-0.5, 0.5, (first_layer_size, pixel_num))
w2 = np.random.uniform(-0.5, 0.5, (second_layer_size, first_layer_size))
w3 = np.random.uniform(-0.5, 0.5, (num_of_class, second_layer_size))

b1 = np.random.uniform(-0.5, 0.5, (first_layer_size, 1))
b2 = np.random.uniform(-0.5, 0.5, (second_layer_size, 1))
b3 = np.random.uniform(-0.5, 0.5, (num_of_class, 1))

def sigmoid(input):
    return 1.0/(1 + np.exp(-input))

def softmax(input):
    ex = np.exp(input)
    return ex / np.sum(ex)

def crossEntropy(output, label):
    return -np.log(output[label])

def dev_sigmoid(output):
    return output*(1 - output)

def one_hot(label):
    ret = np.zeros((10, 1))
    ret[label][0] = 1
    return ret

def forward_propagation(input):
    global w1, w2, w3, b1, b2, b3

    input = np.atleast_2d(input).T

    # hidden layer1
    hl1_middle = np.dot(w1, input) + b1
    hl1 = sigmoid(hl1_middle)

    # hidden layer2
    hl2_middle = np.dot(w2, hl1) + b2
    hl2 = sigmoid(hl2_middle)
  
    #output layer
    output_middle = np.dot(w3, hl2) + b3
    output = softmax(output_middle)

    return hl1, hl2, output

def back_propagation(input, hl1, hl2, output, label):
    global w1, w2, w3, b1, b2, b3
   
    dl = output - one_hot(label)
    dw3 = np.dot(dl, np.atleast_2d(hl2).T)
    db3 = dl
    w3 += -learning_rate * dw3
    b3 += - learning_rate * db3

    dev_sig_h2 = dev_sigmoid(hl2)
    dg2 = np.dot(w3.T, dl)
    dh2 = dg2 * dev_sig_h2
    dw2 = np.dot(dh2, np.atleast_2d(hl1).T)
    w2 += -learning_rate * dw2
    db2 = dh2
    b2 += -learning_rate * db2

    dev_sig_h1 = dev_sigmoid(hl1)
    dg1 = np.dot(w2.T, dh2)
    dh1 = dev_sig_h1 * dg1
    input = np.atleast_2d(input)
    dw1 = np.dot(dh1, input)

    w1 += -learning_rate * dw1
    db1 = dh1
    b1 += -learning_rate * db1

def predict():
    a = []
    correct = 0
    for i in range(test_data.shape[0]):
        hl1, hl2, output = forward_propagation(test_data[i])
        index = np.argmax(output)
        if is_test and index == test_label[i]:
            correct += 1
        a.append(index)
    if is_test:
        print("accuracy is " + str(correct * 100.0/test_data.shape[0]) + "%")
    
    np.array(a).tofile('test_predictions.csv', sep='\n')

def train():
    print("start training...")
    cnt = 0
    # tot_data = train_data.shape[0]
    tot_data = 10000
    for epoch in range(epochs):
        error = 0
        global learning_rate
        # if epoch > 100:
        #     learning_rate = 0.05
        # if epoch > 200:
        #     learning_rate = 0.01
        for i in range(3 * tot_data, 4 * tot_data):
            input_data = train_data[i]
            input_label = train_label[i]
            hl1, hl2, output = forward_propagation(input_data)
            if is_test:
                err = crossEntropy(output, input_label)
                cnt += 1
                error += err
            back_propagation(input_data, hl1, hl2, output, input_label) 
        if is_test:
            print("error in epoch " + str(epoch) + " is " + str(error/tot_data) + ", with learning rate: " + str(learning_rate))
train()
predict()
if is_test:
    print("training finished in " + str((time.time() - starttime)/60) + " minutes")
