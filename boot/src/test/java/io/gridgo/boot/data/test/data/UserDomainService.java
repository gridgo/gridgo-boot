package io.gridgo.boot.data.test.data;

import org.joo.promise4j.Promise;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import io.gridgo.bean.BObject;
import io.gridgo.boot.data.support.annotations.DataAccessInject;
import io.gridgo.boot.support.annotations.Component;
import io.gridgo.boot.support.annotations.GatewayInject;
import io.gridgo.boot.support.annotations.PostConstruct;
import io.gridgo.connector.DataSourceProvider;
import io.gridgo.framework.support.Message;
import lombok.Getter;
import lombok.Setter;

@Setter
@Component
public class UserDomainService {

    @DataAccessInject
    private UserDAO userDAO;

    @DataAccessInject
    private UserKVDAO userKVDAO;

    @GatewayInject(value = "rocksdb")
    private DataSourceProvider<RocksDB> dataSource;

    @Getter
    private String postConstructVerifier;

    private Promise<Message, Exception> jdbcInitPromise;

    @PostConstruct
    public void init() {
        jdbcInitPromise = userDAO.dropTable() //
                                 .then(r -> userDAO.createTable());
    }

    public Promise<Message, Exception> createAndSaveWithDataSource() {
        try {
            var ds = dataSource.getDataSource().orElseThrow();
            ds.put("test_ds".getBytes(), "test".getBytes());
            var result = new String(ds.get("test_ds".getBytes()));
            return Promise.of(Message.ofAny(result));
        } catch (RocksDBException e) {
            return Promise.ofCause(e);
        }
    }

    public Promise<Message, Exception> createAndSaveUserJdbc() {
        return jdbcInitPromise.then(r -> userDAO.add(1, "hello")) //
                              .then(r -> userDAO.findSingle(2)) //
                              .then(r -> userDAO.findSingle(1)) //
                              .map(Message::ofAny);
    }

    public Promise<ModifiedUser, Exception> createAndSaveUserWithAliasJdbc() {
        return jdbcInitPromise.then(r -> userDAO.add(1, "hello")) //
                              .then(r -> userDAO.findWithAlias(1));
    }

    public Promise<Message, Exception> createAndSaveUserRocksDB() {
        return userKVDAO.add("1", BObject.ofPojo(new User(1, "hello"))) //
                        .then(r -> userKVDAO.find("1")) //
                        .map(Message::ofAny);
    }
}
