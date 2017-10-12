import codecs
from collections import Counter
import os
import glob
from textblob import TextBlob as tb
import math

def tf(word, blob):
	return (float)(blob.words.count(word)) / (float)(len(blob.words))

def n_containing(word, bloblist):
	return sum(1 for blob in bloblist if word in blob.words)

def idf(word, bloblist):
	return (float)(math.log(len(bloblist)) / (float)(1 + n_containing(word, bloblist)))

def tfidf(word, blob, bloblist):
	return tf(word, blob) * idf(word, bloblist)

words_list = []
stop = []
bloblist = []
words_dict = {}
splsym = ["-",",","!","#","%","^","&","*","(",")","!", ":",".","{","}", "[","]",">","<","?","/", "*","~", "@"]

print(splsym)

stop_words = open("Stop_words.txt").read()
for line in stop_words:
	for word in line.split(' '):
		stop.append(word)

stop_string = ''.join(stop)

path_to_spam = "hw2_train/train/spam/*.txt"
path_to_ham = "hw2_train/train/ham/*.txt"

for file in glob.glob(path):
	file_words = codecs.open(file, "r", encoding='utf-8', errors='ignore' )
	for line in file_words:
		for word in line.split():
			if not word in stop_string and not word in splsym:
				words_list.append(word)
	blob = tb(' '.join(words_list))
	bloblist.append(blob)
	words_list = []

print(bloblist[3])
#blob = tb(' '.join(words))

# for i in words:
# 	if not i in words_dict.keys():
# 		count = blob.words.count(i)
# 		words_dict[i] = count

for i, blob in enumerate(bloblist):
	print("Top words in document {}".format(i+1))
	scores = {word: tfidf(word, blob, bloblist) for word in blob.words}
	sorted_words = sorted(scores.items(), key=lambda x: x[1], reverse=True)
	for word, score in sorted_words[:3]:
		print("\tWord: {}, TF-IDF: {}".format(word, round(score, 3)))


# for i in words_dict.keys():
# # 	if words_dict[i] > 15:
# 	print(i, words_dict[i])
