#!/usr/bin/env python
# coding: utf-8

# In[2]:

#pip install nltk



# In[26]:
import nltk
import nltk.classify.util
from nltk.classify import NaiveBayesClassifier
from nltk.corpus import movie_reviews
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
import os
import random


# In[27]:


class BayesFilter: 

	def __init__(self, ham_email_list, spam_email_list):
			self.spam_email_list=spam_email_list
			self.ham_email_list= ham_email_list

	

	def run(self):

		def create_word_features(words):
		    my_dict = dict( [ (word, True) for word in words] )
		    return my_dict
	
		
		ham_list = [] # list of dictionary
		spam_list = [] # list of dictionary

		

		for email in self.ham_email_list:
		    #words = word_tokenize(email)
		    words = email.split()
		    ham_list.append((create_word_features(words), "Not Spam"))


		

		for email in self.spam_email_list:
		    #words = word_tokenize(email)
		    words = email.split()
		    spam_list.append((create_word_features(words), "Spam"))



		spam_list

		ham_list

		combined_list = ham_list + spam_list
		# print(len(combined_list))

		random.shuffle(combined_list)

		combined_list



		# Create a test and train section.

		# 70% of the data is training. 30% is test

		#training_part = int(len(combined_list) * .7)

		# print(len(combined_list))

		#training_set = combined_list[:training_part]


		#test_set =  combined_list[training_part:]
		#print(test_set)

		# print (len(training_set))
		# print (len(test_set))


		# Create the Naive Bayes filter

		classifier = NaiveBayesClassifier.train(combined_list)


		# Find the accuracy, using the test data

		#accuracy = nltk.classify.util.accuracy(classifier, test_set)

		#print("Accuracy is: ", accuracy * 100)



		# #NEW TEST EMAILS
		# email_test = "i am ready for this to be labeled as ham"
		# #words = word_tokenize(msg1)
		# features = create_word_features(email_test.split())
		# print("Message 1 is :" ,classifier.classify(features)


		#classifier.show_most_informative_features(5)



		bool = True
		while(bool):


			print("What would you like to Test")
			title = str(input())
			if title == "close":
				bool = False
			features = create_word_features(title.split())
			print("Your Email is :" ,classifier.classify(features))


			

		print("yay")






