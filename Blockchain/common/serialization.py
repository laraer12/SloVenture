import json
from .block import Block
from .blockdata import BlockData

def block_to_json(block):
    return json.dumps(block.to_dict())

def block_from_json(block_json):
    return Block.from_dict(json.loads(block_json))

def blockdata_to_json(data):
    return json.dumps(data.to_dict())

def blockdata_from_json(data_json):
    return BlockData.from_dict(json.loads(data_json))