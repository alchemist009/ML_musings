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
spam_bloblist = []
ham_bloblist = []
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

for file in glob.glob(path_to_spam):
	file_words = codecs.open(file, "r", encoding='utf-8', errors='ignore' )
	for line in file_words:
		for word in line.split():
			if not word in stop_string and not word in splsym and not word.isdigit():
				words_list.append(word)
	blob = tb(' '.join(words_list))
	spam_bloblist.append(blob)
	words_list = []

#print(spam_bloblist[3] + "\n")

for file in glob.glob(path_to_ham):
	file_words = codecs.open(file, "r", encoding='utf-8', errors='ignore' )
	for line in file_words:
		for word in line.split():
			if not word in stop_string and not word in splsym and not word.isdigit():
				words_list.append(word)
	blob = tb(' '.join(words_list))
	ham_bloblist.append(blob)
	words_list = []


prior_spam_probability = (len(spam_bloblist)) / (len(spam_bloblist) + len(ham_bloblist))

prior_ham_probability = 1.0 - prior_spam_probability






# for i, blob in enumerate(bloblist):


#print(ham_bloblist[3])

# for i in words:
# 	if not i in words_dict.keys():
# 		count = blob.words.count(i)
# 		words_dict[i] = count

spam_tfidf_dict = {}

for i, blob in enumerate(spam_bloblist):
	#print("Top words in document {}".format(i+1))
	scores = {word: tfidf(word, blob, spam_bloblist) for word in blob.words}
	sorted_words = sorted(scores.items(), key=lambda x: x[1], reverse=True)
	for word, score in sorted_words[:]:
	 	#print("\tWord: {}, TF-IDF: {}".format(word, round(score, 3)))
	 	if word in spam_tfidf_dict:
	 		spam_tfidf_dict[word] += round(score,3)
	 	else:
	 		spam_tfidf_dict[word] = round(score,3) 


ham_tfidf_dict = {}

for i, blob in enumerate(ham_bloblist):
	print("Top words in document {}".format(i+1))
	scores = {word: tfidf(word, blob, ham_bloblist) for word in blob.words}
	sorted_words = sorted(scores.items(), key=lambda x: x[1], reverse=True)
	for word, score in sorted_words[:]:
	 	print("\tWord: {}, TF-IDF: {}".format(word, round(score, 3)))
	 	if word in ham_tfidf_dict:
	 		ham_tfidf_dict[word] += round(score,3)
	 	else:
	 		ham_tfidf_dict[word] = round(score,3) 




# for i in words_dict.keys():
# # 	if words_dict[i] > 15:
# 	print(i, words_dict[i])
