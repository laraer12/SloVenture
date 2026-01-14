import hashlib

class Block:
    def __init__(self, index, previous_hash, timestamp, difficulty, data, nonce=0, hash=""):
        self.index = index
        self.previous_hash = previous_hash
        self.timestamp = timestamp
        self.difficulty = difficulty
        self.data = data
        self.nonce = nonce
        self.hash = hash

    def create_hash(self, nonce):
        block_string = f"{self.index}{self.previous_hash}{self.data.to_string()}{self.timestamp}{self.difficulty}{nonce}"
        return hashlib.sha256(block_string.encode()).hexdigest()

    def to_dict(self):
        return {
            "index": self.index,
            "previous_hash": self.previous_hash,
            "timestamp": self.timestamp,
            "difficulty": self.difficulty,
            "data": self.data.to_dict(),
            "nonce": self.nonce,
            "hash": self.hash
        }

    @staticmethod
    def from_dict(d):
        from blockdata import BlockData
        data = BlockData.from_dict(d["data"])
        return Block(d["index"], d["previous_hash"], d["timestamp"], d["difficulty"], data, d["nonce"], d["hash"])