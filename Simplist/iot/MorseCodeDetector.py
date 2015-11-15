import json

LUX = 'lux'
DEVICE_ID = 'DEVICEID'
END_OF_COMMUNICATION = ''
class MorseCodeDetector():
    def __init__(self, deviceId):
        self.deviceId = deviceId
		self.text = ''
		self.in_progress = false

    def handle_request(self, json_data):
		if not self.in_progress:
			self.in_progress = true
        info = json.load(json_data)
        val = char(info[LUX])
		if val == END_OF_COMMUNICATION:
			self.in_progress = false
		else:
			self.text = self.text + val
	
	def get_in_progress():
		return self.in_progress
		
	def get_text():
		return self.text
		
		
