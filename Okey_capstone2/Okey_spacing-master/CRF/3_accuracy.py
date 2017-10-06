# -*- coding: utf-8 -*-

from __future__ import unicode_literals     #unicode 문제 해결
from io import open             #encoding='utf-8'을 하기 위해서
import pycrfsuite                           #crf


def feature_function(sentence):        #특징함수
    feature_string=[]       # [[나,3:는],[는,2:나,3:먹],[먹,1:나,2:는,3:다],[는,1:는,2:먹,3:다],[다,1:먹,2:는]]

    for i in range(len(sentence)):
        syllable_feature = []           # [먹,1:나,2:는,3:다]
        syllable_feature.append(sentence[i])

        if i > 1:
            syllable_feature.append('1:' + sentence[i - 2])
        if i > 0:
            syllable_feature.append('2:' + sentence[i - 1])
        if i < len(sentence) - 1:
            syllable_feature.append('3:' + sentence[i + 1])

        feature_string.append(syllable_feature)
    return feature_string


def result_function(sentence, tag):     #출력스트링
    result = ''
    for i in range(len(sentence)):
        if tag[i] == '1':
            result += (' ' + sentence[i])
        else:
            result += sentence[i]

    if result[0] == ' ':  # 결과의 처음이 띄어져있을 때
        result = result[1:]
    return result


def answer_tag_function(answer_file):       #정답 스트링에 태그만을 다는 함수
    answer_tag_list = []
    first_flag = False  # 첫 문장 공백

    for sentence in answer_file:
        if first_flag:
            sentence = sentence.lstrip()

            if not (len(sentence) == 1 or len(sentence) == 0):  # 빈 줄 제외
                tag = []

                for i in range(len(sentence)):
                    if i == 0:
                        tag.append(str('1'))
                    elif i == (len(sentence) - 1):  # 맨 뒤 unicode인식
                        pass
                    elif sentence[i] != ' ' and sentence[i - 1] == ' ':  # 첫 음절일때
                        tag.append(str('1'))
                    elif sentence[i] != ' ' and sentence[i - 1] != ' ':  # 아닐 때
                        tag.append(str('0'))
                answer_tag_list.append(tag)
        first_flag = True
    return answer_tag_list


def syllable_accuracy_function(result_tag_list,answer_tag_list):    #음절 정확도
    cnt=0
    all=0
    for result,answer in zip(result_tag_list,answer_tag_list):
        for index in range(len(result)):
            if result[index]==answer[index]:
                cnt+=1
            all+=1
    return cnt/float(all)*100


test_file = open('test.txt','r',encoding='utf-8')         #파일 전체 읽기
result_file = open('result.txt','w',encoding='utf-8')

crf_tagger = pycrfsuite.Tagger()  # Taggger 클래스는 crf모델을 사용하여 입력 시퀀스의 레이블 시퀀스를 예측하는 기능을 제공한다.
crf_tagger.open('crf.crfsuite')  # 모델 파일을 연다

result_tag_list=[]
first_flag = False
for sentence in test_file:
    if first_flag:
        sentence = sentence.strip()
        feature_string = feature_function(sentence)     #특징함수적용
        tag = crf_tagger.tag(feature_string)    # 훈련된 모델의 항목 순서에 대한 레이블 순서를 예측
        result_tag_list.append(tag)         #결과태그리스트

        result = result_function(sentence, tag)     #결과도출
        result_file.write(result + '\n')
    first_flag=True


#정답과 비교
answer_file = open('answer.txt', 'r', encoding='utf-8')
answer_tag_list = answer_tag_function(answer_file)      #정답태그리스트

print 'syllable accuracy :',syllable_accuracy_function(result_tag_list,answer_tag_list)

crf_tagger.close()  # 모델 닫기
test_file.close()
result_file.close()
answer_file.close()
#test_file, answer_file은 첫줄공백