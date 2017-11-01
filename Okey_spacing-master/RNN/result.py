# -*- coding: utf-8 -*-
import tensorflow as tf
import numpy as np
import csv

def string_one_hot(string):  #한글 벡터화
    x_data = []

    for j in string:
        try:
            x_data.append(syllabe_dic[j])
        except:  # 사전에 없을 때
            x_data.append(0)

    return [x_data] #-> [[[1,0,0],[1,0,0]]], shape(1,2,3) -> batch_size, sequence_length, input_dimension

def spacing_result_function(x_result,y_result):     #최종결과
    for i in range(len(x_result)):
        string = ''
        for j in range(len(x_result[i])):
            if y_result[i][j]==1:
                string+=(' '+x_result[i][j])
            elif y_result[i][j]==0:
                string+=x_result[i][j]
    string = string.strip()
    return string

#variable
syllabe_csv = open('syllabe.csv','r')
csv_reader = csv.reader(syllabe_csv)
syllabe_list = list(csv_reader)
syllabe_list=np.squeeze(syllabe_list)

syllabe_dic = {n: i for i, n in enumerate(syllabe_list)}
syllabe_dic_len = len(syllabe_dic)  # 사전 크기
syllabe_csv.close()

hidden_size = 2
layers = 2
input_dim = syllabe_dic_len  # one-hot size

# 입력값
X = tf.placeholder(tf.int32, [1, None])  # X one-hot, [batch_size,seqeunce_length]
Y = tf.placeholder(tf.int32, [1, None])  # Y label, [batch_size, seqeuence_length]
length = tf.placeholder(tf.int32)

#one-hot encoding
X_one_hot = tf.one_hot(X,input_dim)

# RNN 구축
cell = tf.contrib.rnn.BasicLSTMCell(num_units=hidden_size, state_is_tuple=True)  # num_units=출력사이즈
cell = tf.contrib.rnn.DropoutWrapper(cell,output_keep_prob=1.0)
cell = tf.contrib.rnn.MultiRNNCell([cell]*layers, state_is_tuple=True)
initial_state = cell.zero_state(1, tf.float32)  # 초기 스테이트
outputs, _states = tf.nn.dynamic_rnn(cell, X_one_hot, initial_state=initial_state, dtype=tf.float32)

#softmax
X_for_softmax = tf.reshape(outputs,[-1,hidden_size])    #펼쳐진것을 하나로 합친다.
softmax_w = tf.get_variable("softmax_w",[hidden_size,hidden_size])
softmax_b = tf.get_variable("softmax_b",[hidden_size])
outputs = tf.matmul(X_for_softmax,softmax_w)+softmax_b      #softmax outputs
outputs = tf.reshape(outputs,[1,length,hidden_size])  #하나로 합친것을 다시 펼친다

prediction = tf.argmax(outputs,axis=2)
saver = tf.train.Saver()

#session 실행
sess = tf.Session()
#saver.restore(sess, tf.train.latest_checkpoint('./ckpt/'))
saver.restore(sess, "./ckpt/my-model-14")

#resutling
while True:
    string = input()
    string = string.replace(' ','')      #공백제거
    x_vec = string_one_hot(string)
    y_result = sess.run(prediction, feed_dict={X: x_vec, length:len(string)})
    result = spacing_result_function([string], y_result)
    print(result)
