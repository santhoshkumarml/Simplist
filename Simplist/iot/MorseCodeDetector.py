import json

LUX = 'lux'
DEVICE_ID = 'DEVICEID'

mp = {
        'A': '.-',              'a': '.-',
        'B': '-...',            'b': '-...',
        'C': '-.-.',            'c': '-.-.',
        'D': '-..',             'd': '-..',
        'E': '.',               'e': '.',
        'F': '..-.',            'f': '..-.',
        'G': '--.',             'g': '--.',
        'H': '....',            'h': '....',
        'I': '..',              'i': '..',
        'J': '.---',            'j': '.---',
        'K': '-.-',             'k': '-.-',
        'L': '.-..',            'l': '.-..',
        'M': '--',              'm': '--',
        'N': '-.',              'n': '-.',
        'O': '---',             'o': '---',
        'P': '.--.',            'p': '.--.',
        'Q': '--.-',            'q': '--.-',
        'R': '.-.',             'r': '.-.',
        'S': '...',             's': '...',
        'T': '-',               't': '-',
        'U': '..-',             'u': '..-',
        'V': '...-',            'v': '...-',
        'W': '.--',             'w': '.--',
        'X': '-..-',            'x': '-..-',
        'Y': '-.--',            'y': '-.--',
        'Z': '--..',            'z': '--..',
        '0': '-----',		'.': '.-.-.-',
        '1': '.----',           
        '2': '..---',           
        '3': '...--',           
        '4': '....-',           
        '5': '.....',           
        '6': '-....',           
        '7': '--...',           
        '8': '---..',           
        '9': '----.',
}

rev_mp = dict((v,k) for (k,v) in mp.items())

def decode(i):
  if i in rev_mp: return rev_mp[i]
  return None

class MorseCodeDetector():
    def __init__(self, deviceId):
        self.deviceId = deviceId
        self.buffer = ''
        self.text = ''
        self.in_progress = False

    def handle_request(self, json_data):
      if not self.in_progress:
	self.in_progress = true
        info = json.load(json_data)
        sym = {
	    0xaaaa : '.',
	    0xffff : '_',
	    0x0e0f : ' '
	    0x0e10 : mp['.']
	  }.get(info[LUX], '')		
	
	if sym == mp['.']:
	  self.in_progress = False
	  self.text = self.text + mp['.']
	else:
	  self.in_progress = True
	  self.buffer = self.buffer + sym
	  
	  char = decode(self.buffer)
	  if char:
	    self.text = self.text + char
	    self.buffer = ''

	def get_in_progress():
	  return self.in_progress
		
	def get_text():
	  return self.text