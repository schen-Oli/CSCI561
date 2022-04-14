import numpy as np
import time

starttime = time.time()
pixel_num = 784
first_layer_size = 64
second_layer_size = 32
num_of_class = 10
learning_rate = 0.1
epochs = 100

print("import testing data...")
test_data = np.genfromtxt("test_image.csv", delimiter=",")/255
test_label = np.genfromtxt("test_label.csv", dtype=np.int8)

print("import training data...")
train_data = np.genfromtxt("train_image.csv", delimiter=",")/255
train_label = np.genfromtxt("train_label.csv", dtype=np.int8)

# train_data = np.genfromtxt("dev/dev_img.csv", dtype=np.float64, delimiter=",")/255
# train_label = np.genfromtxt("dev/dev_label.csv", dtype=np.int8)
# test_data = np.genfromtxt("dev/dev_img.csv", dtype=np.float64, delimiter=",")/255
# test_label = np.genfromtxt("dev/dev_label.csv", dtype=np.int8)

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

def forward_propagation(input, label):
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

    #cost 
    err = crossEntropy(output, label)

    return hl1, hl2, output, err

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

def test(epo):
    a = np.array([], dtype=np.int32)
    correct = 0
    for i in range(test_data.shape[0]):
        hl1, hl2, output, err = forward_propagation(test_data[i], test_label[i])
        index = np.argmax(output)
        if(index == test_label[i]):
            correct += 1
        np.append(a, int(index))
    print("accuracy in epo " + str(epo) + " is " + str(correct * 100.0/test_data.shape[0]) + "%")
    np.savetxt("output.csv", a, fmt='%i')

def train():
    print("start training...")
    cnt = 0
    tot_data = train_data.shape[0]
    for epoch in range(epochs):
        error = 0
        global learning_rate
        if epoch > 50:
            learning_rate = 0.05
        if epoch > 80:
            learning_rate = 0.01
        if epoch > 90:
            learning_rate = 0.005
        if epoch > 95:
            learning_rate = 0.001
        for i in range(tot_data):
            input_data = train_data[i]
            input_label = train_label[i]
            hl1, hl2, output, err = forward_propagation(input_data, input_label)
            cnt += 1
            error += err
            back_propagation(input_data, hl1, hl2, output, input_label) 
        print("error in epoch " + str(epoch) + " is " + str(error/tot_data) + ", with learning rate: " + str(learning_rate))
train()
test(epochs)
print("training finished in " + str((time.time() - starttime)/60) + " minutes")
