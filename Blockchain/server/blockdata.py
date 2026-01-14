class BlockData:
    def __init__(self, numOfPeople, timestamp, longitude, latitude):
        self.numOfPeople = numOfPeople
        self.timestamp = timestamp
        self.longitude = longitude
        self.latitude = latitude

    def to_string(self):
        return f"{self.numOfPeople}{self.timestamp}{self.longitude}{self.latitude}"

    def to_dict(self):
        return {
            "numOfPeople": self.numOfPeople,
            "timestamp": self.timestamp,
            "longitude": self.longitude,
            "latitude": self.latitude
        }

    @staticmethod
    def from_dict(d):
        return BlockData(d["numOfPeople"], d["timestamp"], d["longitude"], d["latitude"])