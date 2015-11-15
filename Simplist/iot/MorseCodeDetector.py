import json

LUX = 'lux'
DEVICE_ID = 'DEVICEID'
END_OF_COMMUNICATION = ''


class MorseCodeDetector:
    def __init__(self, deviceId):
        self.deviceId = deviceId
        self.in_progress = False
        self.text = ''

    def handle_request(self, json_data):
        if not self.in_progress:
            self.in_progress = True
        info = json.load(json_data)
        val = chr(info[LUX])
        if val == END_OF_COMMUNICATION:
            self.in_progress = True
        else:
            self.text += val

    def get_in_progress(self):
        return self.in_progress

    def get_text(self):
        return self.text