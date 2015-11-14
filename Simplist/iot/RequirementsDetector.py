import json

MILK = 'lux'
FRUITS = 'pressure'
DEVICE_ID = 'DEVICEID'
class RequirementsDetector():
    def __init__(self, deviceId):
        self.deviceId = deviceId

    def handle_request(self, json_data):
        info = json.load(json_data)
        things_needed = []
        if self.deviceId == info[DEVICE_ID]:
            if MILK in info:
                things_needed.append('MILK')
            if FRUITS in info:
                things_needed.append('Fruits')
        return things_needed
