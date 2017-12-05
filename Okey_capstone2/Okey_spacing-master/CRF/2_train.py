# -*- coding: utf-8 -*-

from __future__ import unicode_literals         #unicode 문제 해결
from io import open                             #encoding='utf-8'을 하기 위해서
import pycrfsuite                               #crf

def pair_function(sentence):   #음절,태그 쌍만들기
    pairs = []              # [[안,1],[녕,0]]

    sentence = sentence.strip()  # 끝 개행 인식 제거
    sentence = sentence.split(' ')  # 공백기준 리스트만들기

    for i in sentence:
        pair = i.split('/')  # [안,1]
        pairs.append(pair)

    return pairs


def feature_function(pairs):        #특징함수
    features = []               # [[나,3:는],[는,2:나,3:먹],[먹,1:나,2:는,3:다],[는,1:는,2:먹,3:다],[다,1:먹,2:는]]

    for i in range(len(pairs)):
        syllable_feature = []  # [먹,1:나,2:는,3:다]
        syllable_feature.append(pairs[i][0])

        if i > 1:
            syllable_feature.append('1:' + pairs[i - 2][0])
        if i > 0:
            syllable_feature.append('2:' + pairs[i - 1][0])
        if i < len(pairs) - 1:
            syllable_feature.append('3:' + pairs[i + 1][0])

        features.append(syllable_feature)

    return features


def tag_function(pairs):
    tags=[]         # [1,0,1,0,1]

    for pair in pairs:
        tags.append(pair[1])         #태그

    return tags

#main
crf_trainer = pycrfsuite.Trainer()                  #훈련용 데이터 세트를 유지 관리 - 훈련객체

infile = open('tag_text.txt','r', encoding='utf-8')
for sentence in infile:
    sentence=sentence.strip()

    pair = pair_function(sentence)        # [[안,1],[녕,0]]
    feature = feature_function(pair)      # [[나,3:는],[는,2:나,3:먹],[먹,1:나,2:는,3:다],[는,1:는,2:먹,3:다],[다,1:먹,2:는]]
    tag = tag_function(pair)                # [1,0,1,0,1]

    crf_trainer.append(feature, tag)  # 데이터 세트에 (특징항목 / 레이블 순서)를 추가한다. 학습을 위한 데이터 세트 구축

#crf_trainer.train('crf.crfsuite')
infile.close()
