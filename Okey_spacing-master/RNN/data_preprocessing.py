# -*- coding: utf-8 -*-
import csv

def tag_function(sentence):     #태그함수
    SPACE_CHARACTER = " "
    string=':'      #csv입력시 오류방지
    tag='1 '

    if len(sentence)==0:
        return string,SPACE_CHARACTER

    string+=sentence[0]

    for i in range(1,len(sentence)):
        if sentence[i] != SPACE_CHARACTER:
            if sentence[i - 1] == SPACE_CHARACTER:
                string += sentence[i]
                tag += ('1'+SPACE_CHARACTER)
            else:
                string += sentence[i]
                tag += ('0'+SPACE_CHARACTER)

    return string,tag

def open_csv(num):  #전처리데이터 만들기
    csv_file = open('t'+str(num)+'.csv', 'w', newline='')
    csv_writer = csv.writer(csv_file)
    return csv_writer,0

num=1
csv_writer, cnt = open_csv(num)
text_file = open('raw_text1.txt','r',encoding='utf-8')      #text1,text2,text3

for sentence in text_file:
    sentences = sentence.split('.')
    for i in sentences:
        i=i.strip()

        if len(i)>=4 and len(i)<=100:         #훈련 때 메모리방지
            if not '<' in i:        #<>태그있는거 제외
                string,tag=tag_function(i)   #태그달기
                try:
                    csv_writer.writerow([string,tag])   #csv입력
                    cnt += 1
                    if cnt == 1000000:  # 백만기준으로 자르기
                        num += 1
                        csv_writer, cnt = open_csv(num)
                except:
                    pass

text_file.close()
