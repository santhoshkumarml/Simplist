import json

LUX = 'lux'
DEVICE_ID = 'DEVICEID'

morseAlphabet ={
   "A" : ".-",
   "B" : "-...",
   "C" : "-.-.",
   "D" : "-..",
   "E" : ".",
   "F" : "..-.",
   "G" : "--.",
   "H" : "....",
   "I" : "..",
   "J" : ".---",
   "K" : "-.-",
   "L" : ".-..",
   "M" : "--",
   "N" : "-.",
   "O" : "---",
   "P" : ".--.",
   "Q" : "--.-",
   "R" : ".-.",
   "S" : "...",
   "T" : "-",
   "U" : "..-",
   "V" : "...-",
   "W" : ".--",
   "X" : "-..-",
   "Y" : "-.--",
   "Z" : "--..",
   " " : "/"
}

inverseMorseAlphabet=dict((v,k) for (k,v) in morseAlphabet.items())


testCode = ".... . .-.. .-.. --- / -.. .- .. .-.. -.-- / .--. .-. --- --. .-. .- -- -- . .-. / --. --- --- -.. / .-.. ..- -.-. -.- / --- -. / - .... . / -.-. .... .- .-.. .-.. . -. --. . ... / - --- -.. .- -.-- "

# parse a morse code string positionInString is the starting point for decoding
def decodeMorse(code, positionInString = 0):   
   if positionInString < len(code):
       morseLetter = ""
       for key,char in enumerate(code[positionInString:]):
           if char == " ":
               positionInString = key + positionInString + 1
               letter = inverseMorseAlphabet[morseLetter]
               return letter + decodeMorse(code, positionInString)
           
           else:
               morseLetter += char
   else:
       return ""
   
#encode a message in morse code, spaces between words are represented by '/'
def encodeToMorse(message):
   encodedMessage = ""
   for char in message[:]:
       encodedMessage += morseAlphabet[char.upper()] + " "
           
   return encodedMessage

print decodeMorse(testCode)

dev_mp = {
	  0x000F : '-',
	  0x00F0 : '.',
	  0x0F00 : '/',
	  0xF000 : ' '
	 }

rev_dev_mp = dict((v,k) for (k,v) in dev_mp.items())

class MorseCodeDetector(object):
    def __init__(self):
        self.text = ''
        self.in_progress = False

    def handle_request(self, json_data):
      info = json.loads(json_data)
      for k in dev_mp.keys():
	if k & info[LUX]:
	  self.text = self.text + dev_mp[info[LUX]]

    def get_in_progress(self):
      return self.in_progress
    
    def get_text(self):
      return decodeMorse(self.text)

mcd = MorseCodeDetector()
for char in testCode:
  mcd.handle_request(json.dumps({LUX : rev_dev_mp[char]}))
mcd.get_text()