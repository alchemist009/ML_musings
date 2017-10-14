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


prior_spam_probability = (float)(len(spam_bloblist)) / (float)(len(spam_bloblist) + len(ham_bloblist))

print(prior_spam_probability)

prior_ham_probability = 1.0 - prior_spam_probability

print(prior_ham_probability)

spam_dict = {}

for i, blob in enumerate(spam_bloblist):
	for word in blob.words:
		if word in spam_dict:
			spam_dict[word] += 1
		else:
			spam_dict[word] = 1

ham_dict = {}

for i, blob in enumerate(ham_bloblist):
	for word in blob.words:
		if word in ham_dict:
			ham_dict[word] += 1
		else:
			ham_dict[word] = 1



spam_total_word_count = 0

for word in spam_dict:
	spam_total_word_count += spam_dict[word]


ham_total_word_count = 0

for word in ham_dict:
	ham_total_word_count += ham_dict[word]

print(spam_total_word_count)
print(len(spam_dict))

spam_likelihood = {}

for word in spam_dict:
	if word in spam_dict:
		spam_likelihood = (spam_dict[word] + 1.0) / (spam_total_word_count + len(spam_dict) + 1.0)
		spam_l_likelihood = round(math.log(spam_likelihood), 3)
		spam_likelihood[word] = spam_l_likelihood

