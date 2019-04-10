package io.gridgo.boot.data.test.data;

import java.util.List;

import org.joo.promise4j.Promise;

import io.gridgo.boot.data.support.annotations.DataAccessInject;
import io.gridgo.boot.support.annotations.Component;
import io.gridgo.framework.support.Message;
import lombok.Setter;

@Setter
@Component
public class UserDomainService {

    @DataAccessInject
    private UserDAO userDAO;

    public Promise<Message, Exception> createAndSaveUser() {
        return userDAO.dropTable() //
                      .then(r -> userDAO.createTable()) //
                      .then(r -> userDAO.add(1, "hello")) //
                      .then(r -> userDAO.find(1)) //
                      .map(this::transform);
    }

    private Message transform(List<User> r) {
        return Message.ofAny((r.isEmpty()) ? null : r.get(0));
    }
}
