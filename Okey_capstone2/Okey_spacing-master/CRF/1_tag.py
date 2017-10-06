# -*- coding: utf-8 -*-
from __future__ import unicode_literals         #unicode 문제 해결
from io import open                             #encoding='utf-8'을 하기 위해서

infile = open('crawling_text.txt','r',encoding='utf-8')         #파일 전체 읽기
outfile = open('tag_text.txt', 'w',encoding='utf-8')           #태그달아서 쓸 파일 열기

first_flag=False        #첫 문장 공백

for sentence in infile:
    if first_flag:
        sentence = sentence.lstrip()

        if not (len(sentence)==1 or len(sentence)==0):    #빈 줄 제외
            string=''

            for i in range(len(sentence)):
                if i == 0:
                    string += (sentence[i]+'/1 ')
                elif i==(len(sentence)-1):              #맨 뒤 unicode인식
                    pass
                elif sentence[i] != ' ' and sentence[i - 1]==' ':       #첫 음절일때
                    string += (sentence[i]+'/1 ')
                elif sentence[i] != ' ' and sentence[i - 1]!=' ':       #아닐 때
                    string += (sentence[i]+'/0 ')

            outfile.write(string + '\n')
    first_flag=True

infile.close()
outfile.close()
#crawling_text는 첫줄은 공백을 넣어준다, utf-8형식으로 저장