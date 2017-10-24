#세종 말뭉치로 단어 사전을 구축한다.
#빈도가 큰 단어 순으로 정리한다.
#
#그,244939
#수,230441
#있다,223017

import os
import re
import csv
import operator
import json

count = 0

#현재 wordDic.py가 있는 폴더의 경로로부터 데이터 파일의 경로를 가져온다
current_location = os.getcwd()
file_location = current_location + '/data'

#directory 에 있는 file의 제목을 모두 가져온다.
directory = os.listdir(file_location)

#작업 위치를 변경한다.
os.chdir(file_location)

dic = {}

for f in directory:

    extension = f.split('.')[-1]
    encodings = ['utf8', 'utf16', 'cp949']
    for encoding in encodings:
        try:
            with open(f, "r", encoding=encoding) as file:
                hangul = re.compile('[가-힣]+')
                for line in file:
                    result = hangul.findall(line)
                    # 거의 모든 경우 단어의 길이가 6 이하이므로 단어의 길이가 6이상인 경우 오류로 판단하여 예외처리해준다
                    if (len(result) > 6): continue
                    for word in result:
                        if word in dic.keys():
                            dic[word] += 1
                        else:
                            dic[word] = 1
                break
        except:
            if encoding == encodings[-1]:
                print(f + "opening error")
                continue



try:
    #빈도 수를 값에 따라서 내림차순으로 정렬하고, 리스트로 반환한다.

    with open('dic_.csv', 'w', newline="\n") as f:
        w = csv.writer(f)
        # list 에 들어있는 tuple을 작성한다.
        for key, value in dic.items():
            if(value > 110):
                w.writerow(key)

    print(len(dic))
    print(count)
    #newline = '\r\n 이 기본이기 때문에 \n 을 지정해주어야 한다.

except:
    with open('dic_.csv', 'w', newline="\n") as f:
        w = csv.writer(f)
        # list 에 들어있는 tuple을 작성한다.
        for key,value in dic.items():
            if(value > 100):
                w.writerow(key)


