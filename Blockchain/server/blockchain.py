import time
from block import Block

class Blockchain:
    def __init__(self, difficulty):
        self.chain = []
        self.difficulty = difficulty

    def get_last_block(self):
        return self.chain[-1] if self.chain else None

    def create_block(self, data):
        timestamp = int(time.time())
        if not self.chain:
            new_block = Block(0, "0", timestamp, self.get_adjusted_difficulty(), data)
        else:
            previous = self.get_last_block()
            new_block = Block(previous.index + 1, previous.hash, timestamp, self.get_adjusted_difficulty(), data)
        return new_block

    def add_block(self, block):
        self.chain.append(block)

    def validate_block(self, block):
        current_time = int(time.time())
        if block.timestamp > current_time + 60:
            print(f"Block {block.index} is from the future")
            return False
        last = self.get_last_block()
        if last:
            if block.timestamp < last.timestamp - 60:
                print(f"Block {block.index} timestamp too old")
                return False
            if block.index != last.index + 1:
                print(f"Invalid index at block {block.index}")
                return False
            if block.previous_hash != last.hash:
                print(f"Invalid previous hash at block {block.index}")
                return False
        if block.create_hash(block.nonce) != block.hash:
            print(f"Invalid hash at block {block.index}")
            return False
        if not block.hash.startswith("0" * block.difficulty):
            print(f"Block {block.index} does not satisfy difficulty")
            return False
        return True

    def validate_chain(self):
        for i in range(1, len(self.chain)):
            current = self.chain[i]
            previous = self.chain[i-1]
            if current.previous_hash != previous.hash or current.index != previous.index + 1:
                return False
            if current.create_hash(current.nonce) != current.hash:
                return False
            if current.timestamp > int(time.time()) + 60 or current.timestamp < previous.timestamp - 60:
                return False
        return True