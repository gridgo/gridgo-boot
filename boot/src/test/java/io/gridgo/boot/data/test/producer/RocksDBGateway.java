package io.gridgo.boot.data.test.producer;

import io.gridgo.boot.support.annotations.Connector;
import io.gridgo.boot.support.annotations.Gateway;

@Gateway("rocksdb")
@Connector("rocksdb://.test")
public class RocksDBGateway {
    
}
