import imaplib
import email
from email.header import decode_header
import webbrowser
import os




class EmailReader:

	

	def __init__(self, username, password):
		self.username = username
		self.password = password
		self.spam_list = []
		self.pastrami_list = []

	# # account credentials
	# username = "zfish@mail.yu.edu"
	# password = "hackYU2021"

	def clean(text):
	    # clean text for creating a folder
	    return "".join(c if c.isalnum() else "_" for c in text)

	def psak(self):

		print("How many emails would you like to classify")
		N = int(str(input()))


		# create an IMAP4 class with SSL 
		imap = imaplib.IMAP4_SSL("imap.gmail.com")
		# authenticate
		imap.login(self.username, self.password)


		status, messages = imap.select("INBOX")
		# number of top emails to fetch
		
		# total number of emails
		messages = int(messages[0])
		count =0

		print('If an email belongs in spam, type \"s\". If it does not belong in spam, type \"n\".')
	
		for i in range(messages, messages-N, -1):
		    # fetch the email message by ID
		    res, msg = imap.fetch(str(i), "(RFC822)")
		    for response in msg:
		        if isinstance(response, tuple):
		            # parse a bytes email into a message object
		            msg = email.message_from_bytes(response[1])
		            # decode the email subject
		            subject, encoding = decode_header(msg["Subject"])[0]
		            if isinstance(subject, bytes):
		                # if it's a bytes, decode to str
		                if encoding is not None:
		                	subject = subject.decode(encoding)      
		            count = count+1 


		            print('Subject:{}{}{}'.format(count, " " ,subject))
		            
		            while True:
		            	response=str(input())
		            	if response =="s":
		            		self.spam_list.append(subject)
		            		break
		            	elif response =="n":
		            		self.pastrami_list.append(subject)
		            		break
		        
		            	else:
		            		print('Invalid response: If an email belongs in spam, type \"s\". If it does not belong in spam, type \"n\".')
                    


		imap.close()
		imap.logout()

		#print(self.spam_list)
		#print(self.pastrami_list)















