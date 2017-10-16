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
	return (float) (math.log(len(bloblist))) / (float)(1 + n_containing(word, bloblist))

def tfidf(word, blob, bloblist):
	return tf(word, blob) * idf(word, bloblist)



words_list = []
stop = []
words_dict = {}
splsym = ["-",",","!","#","%","^","&","*","(",")","!", ":",".","{","}", "[","]",">","<","?","/", "*","~", "@"]
spam_dict = {}
ham_dict = {}
ham_total_word_count = 0
spam_total_word_count = 0
ham_distinct_words = 0
spam_distinct_words = 0
distinct_vocab = {}

def get_file_list(path_to_spam, path_to_ham):
	
	spam_bloblist = []
	for file in glob.glob(path_to_spam):
		words_list[:] = []
		file_words = codecs.open(file, "r", encoding='utf-8', errors='ignore' )
		for line in file_words:
			for word in line.split():
				if not word in stop_string and not word in splsym and not word.isdigit():
					words_list.append(word.lower())
		blob = tb(' '.join(words_list))
		spam_bloblist.append(blob)
		
	ham_bloblist = []
	for file in glob.glob(path_to_ham):
		words_list[:] = []
		file_words = codecs.open(file, "r", encoding='utf-8', errors='ignore' )
		for line in file_words:
			for word in line.split():
				if not word in stop_string and not word in splsym and not word.isdigit():
					words_list.append(word.lower())
		blob = tb(' '.join(words_list))
		ham_bloblist.append(blob)

	return spam_bloblist, ham_bloblist




def find_likelihood(spam_dict, ham_dict):
	spam_total_word_count = 0

	for word in spam_dict:
		spam_total_word_count += spam_dict[word]


	ham_total_word_count = 0

	for word in ham_dict:
		ham_total_word_count += ham_dict[word]

	spam_distinct_words_count = len(spam_dict)
	ham_distinct_words_count = len(ham_dict)

	distinct_vocab = dict(spam_dict.items() + ham_dict.items())

	spam_likelihood = {}

	for word in distinct_vocab:
		if word in spam_dict:
			spam_lhood = (spam_dict[word] + 1.0) / (spam_total_word_count + len(distinct_vocab) + 1.0)
			spam_log_likelihood = round(math.log(spam_lhood), 3)
			spam_likelihood[word] = spam_log_likelihood

	ham_likelihood = {}

	for word in distinct_vocab:
		if word in ham_dict:
			ham_lhood = (ham_dict[word] + 1.0) / (ham_total_word_count + len(distinct_vocab) + 1.0)
			ham_log_likelihood = round(math.log(ham_lhood), 3)
			ham_likelihood[word] = ham_log_likelihood

	return spam_likelihood, ham_likelihood


def get_dict(spam_bloblist, ham_bloblist):

	for i, blob in enumerate(spam_bloblist):
		for word in blob.words:
			if word in spam_dict:
				spam_dict[word] += 1
			else:
				spam_dict[word] = 1

	
	for i, blob in enumerate(ham_bloblist):
		for word in blob.words:
			if word in ham_dict:
				ham_dict[word] += 1
			else:
				ham_dict[word] = 1

	return spam_dict, ham_dict



#Calculate posterior probability

def accuracy_check(blob):

	current_spam_probability = 0.0
	current_ham_probability = 0.0

	for word in blob.words:
		if word in h_likelihood:
			current_ham_probability += h_likelihood[word]
		else:
			current_ham_probability = current_ham_probability + math.log(1.0 / (ham_total_word_count + len(distinct_vocab) + 1.0))

		if word in s_likelihood:
			current_spam_probability += s_likelihood[word]
		else:
			current_spam_probability = current_spam_probability + math.log(1.0 / (spam_total_word_count + len(distinct_vocab) + 1.0))

	current_spam_probability = current_spam_probability + prior_spam_probability
	current_ham_probability = current_ham_probability + prior_ham_probability

	# print(current_ham_probability)
	# print(current_spam_probability)


	if(current_ham_probability > current_spam_probability):
		return 1 
	else:
		return 0


if __name__ == "__main__":
	path = raw_input("Please provide path to directory containing datasets: ")

	path_to_spam = path + "/train/spam/*.txt"
	path_to_ham = path + "/train/ham/*.txt"

	path_to_spam_test = path + "/test/spam/*.txt"
	path_to_ham_test = path + "/test/ham/*.txt"

	stop_words = open("Stop_words.txt").read()
	for line in stop_words:
		for word in line.split(' '):
			stop.append(word)

	stop_string = ''.join(stop)

	spam_bloblist, ham_bloblist = get_file_list(path_to_spam, path_to_ham)

	#print(spam_bloblist)

	spam_dict, ham_dict = get_dict(spam_bloblist, ham_bloblist)

	s_likelihood, h_likelihood = find_likelihood(spam_dict, ham_dict)

	prior_spam_probability = (float)(len(spam_bloblist) * 1.0) / (float)(len(spam_bloblist) + len(ham_bloblist))

	log_prior_spam_probability = math.log(prior_spam_probability)

	print(prior_spam_probability)

	prior_ham_probability = 1.0 - prior_spam_probability

	log_prior_ham_probability = math.log(prior_ham_probability)

	print(prior_ham_probability)

	ham_correct = 0.0
	nh = 0
	for i,blob in enumerate(ham_bloblist):
		nh = nh+1
		if accuracy_check(ham_bloblist[i]) == 1:
			ham_correct += 1.0


	spam_correct = 0.0
	ns = 0
	for i,blob in enumerate(spam_bloblist):
		ns = ns+1
		if accuracy_check(spam_bloblist[i]) == 0:
			spam_correct += 1.0

	#print(len(ham_bloblist))
	ham_accuracy = (ham_correct/nh) * 100

	spam_accuracy = (spam_correct/ns) * 100

	print("Ham accuracy is: " + str(ham_accuracy))

	print("Spam accuracy is: " + str(spam_accuracy))

	spam_bloblist[:] = []

	ham_bloblist[:] = []

	ham_accuracy = 0.0

	spam_accuracy = 0.0

	spam_bloblist, ham_bloblist = get_file_list(path_to_spam_test, path_to_ham_test)


	ham_correct = 0.0
	nh = 0
	for i,blob in enumerate(ham_bloblist):
		nh = nh+1
		if accuracy_check(ham_bloblist[i]) == 1:
			ham_correct += 1.0


	spam_correct = 0.0
	ns = 0
	for i,blob in enumerate(spam_bloblist):
		ns = ns+1
		if accuracy_check(spam_bloblist[i]) == 0:
			spam_correct += 1.0

	#print(len(ham_bloblist))
	ham_accuracy = (ham_correct/nh) * 100

	spam_accuracy = (spam_correct/ns) * 100

	print("Ham accuracy is: " + str(ham_accuracy))

	print("Spam accuracy is: " + str(spam_accuracy))


