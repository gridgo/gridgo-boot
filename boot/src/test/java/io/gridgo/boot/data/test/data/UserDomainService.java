package io.gridgo.boot.data.test.data;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BObject;
import io.gridgo.boot.data.support.annotations.DataAccessInject;
import io.gridgo.boot.support.annotations.Component;
import io.gridgo.framework.support.Message;
import lombok.Setter;

@Setter
@Component
public class UserDomainService {

    @DataAccessInject
    private UserDAO userDAO;

    @DataAccessInject
    private UserKVDAO userKVDAO;

    public Promise<Message, Exception> createAndSaveUserJdbc() {
        return userDAO.dropTable() //
                      .then(r -> userDAO.createTable()) //
                      .then(r -> userDAO.add(1, "hello")) //
                      .then(r -> userDAO.findSingle(2)) //
                      .then(r -> userDAO.findSingle(1)) //
                      .map(Message::ofAny);
    }

    public Promise<Message, Exception> createAndSaveUserRocksDB() {
        return userKVDAO.add("1", BObject.ofPojo(new User(1, "hello"))) //
                        .then(r -> userKVDAO.find("1")) //
                        .map(Message::ofAny);
    }
}
