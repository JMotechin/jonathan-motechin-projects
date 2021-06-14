from EmailReader import EmailReader
from BayesFilter import BayesFilter



def main():


	print("What is your email?")
	email = str(input())
	print("please enter your password")
	password = str(input())


	account = EmailReader(email, password)
	#account = EmailReader("HackYUExample@gmail.com", "hackyu2021")
	account.psak()
	spam = account.spam_list
	notSpam = account.pastrami_list

	filter = BayesFilter(notSpam, spam)
	filter.run()



	# title = str(input())
	# features = filter.create_word_features(title.split())
	# print("Your Email is :" ,classifier.classify(features))




main()




