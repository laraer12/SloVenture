import time
from Blockchain.common.block import Block

class Blockchain:
    BLOCK_INTERVAL = 10
    DIFF_INTERVAL = 10

    def __init__(self, difficulty=1):
        self.chain = []
        self.difficulty = difficulty

    def get_last_block(self):
        return self.chain[-1] if self.chain else None

    def create_block(self, data):
        timestamp = int(time.time())
        if not self.chain:
            new_block = Block(0, "0", timestamp,  difficulty=1, data=data)
        else:
            previous = self.get_last_block()
            new_block = Block(previous.index, previous.hash, timestamp,  difficulty=1, data=data)
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
                print(f"Invalid previous hash at block {block.index} \nprevious hash: {block.previous_hash} \n last hash: {last.hash}")
                return False
        if block.create_hash(block.nonce) != block.hash:
            print(f"Invalid hash at block {block.index}") #
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
    
    def cumulative_difficulty(self):
        return sum(2 ** b.difficulty for b in self.chain)
    
    def choose_chain(local, remote):
        if remote.cumulative_difficulty() > local.cumulative_difficulty():
            return remote
        return local
    
    def get_adjusted_difficulty(self):
        if len(self.chain) < self.DIFF_INTERVAL:
            return self.difficulty

        last = self.chain[-1]
        adjust_block = self.chain[-self.DIFF_INTERVAL]

        expected_time = self.BLOCK_INTERVAL * self.DIFF_INTERVAL
        actual_time = last.timestamp - adjust_block.timestamp

        if actual_time < expected_time / 2:
            return adjust_block.difficulty + 1
        elif actual_time > expected_time * 2:
            return max(1, adjust_block.difficulty - 1)
        else:
            return adjust_block.difficulty