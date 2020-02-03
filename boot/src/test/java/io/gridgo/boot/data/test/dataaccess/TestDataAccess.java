package io.gridgo.boot.data.test.dataaccess;

import org.joo.promise4j.PromiseException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import io.gridgo.bean.BArray;
import io.gridgo.boot.GridgoApplication;
import io.gridgo.boot.data.test.dataaccess.data.User;
import io.gridgo.boot.data.test.dataaccess.data.UserDomainService;
import io.gridgo.boot.support.annotations.EnableComponentScan;
import io.gridgo.boot.support.annotations.RegistryInitializer;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Registry;

@EnableComponentScan
public class TestDataAccess {

    @Test
    public void testRocksDB() throws Throwable {
        doTest("test_rocksdb", this::assertSingle);
    }

    @Test
    public void testJdbc() throws Throwable {
        doTest("test_jdbc", this::assertSingle);
    }

    @Test
    public void testJdbcBatch() throws Throwable {
        doTest("test_jdbc_batch", this::assertBatch);
    }

    private void assertSingle(Message r) {
        User user = r.body().asReference().getReference();
        Assert.assertEquals(1, user.getId());
        Assert.assertEquals("hello", user.getName());
    }

    private void assertBatch(Message r) {
        BArray users = r.body().asArray();
        Assert.assertEquals(3, users.size());

        User user = users.getReference(0).getReference();
        Assert.assertEquals(1, user.getId());
        Assert.assertEquals("one", user.getName());

        user = users.getReference(1).getReference();
        Assert.assertEquals(2, user.getId());
        Assert.assertEquals("two", user.getName());

        user = users.getReference(2).getReference();
        Assert.assertEquals(3, user.getId());
        Assert.assertEquals("three", user.getName());
    }

    @Test
    public void testDataSource() throws PromiseException, InterruptedException {
        var app = GridgoApplication.run(TestDataAccess.class);
        var service = app.getRegistry().lookup(UserDomainService.class.getName(), UserDomainService.class);
        var msg = service.createAndSaveWithDataSource().get();
        app.stop();
        Assert.assertEquals("test", msg.body().asValue().getString());
    }

    @Test
    public void testAlias() throws PromiseException, InterruptedException {
        var app = GridgoApplication.run(TestDataAccess.class);
        var service = app.getRegistry().lookup(UserDomainService.class.getName(), UserDomainService.class);
        var user = service.createAndSaveUserWithAliasJdbc().get();
        app.stop();
        Assert.assertEquals(1, user.getUserId());
    }

    protected void doTest(String gateway, Consumer<Message> consumer) throws Throwable {
        var app = GridgoApplication.run(TestDataAccess.class);
        var latch = new CountDownLatch(1);
        var exRef = new AtomicReference<Throwable>(null);

        app.getContext() //
           .findGatewayMandatory(gateway) //
           .push(Message.ofAny(null)) //
           .always((s, r, e) -> {
               try {
                   if (e != null) {
                       throw e;
                   } else {
                       consumer.accept(r);
                   }
               } catch (Throwable ex) {
                   exRef.set(ex);
               }

               latch.countDown();
           });

        latch.await();

        if (exRef.get() != null) {
            throw exRef.get();
        }

        app.stop();
    }

    @RegistryInitializer
    public static void initializeRegistry(Registry registry) {
        registry.register("transformer", "nop");
    }
}
