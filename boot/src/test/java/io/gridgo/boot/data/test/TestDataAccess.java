package io.gridgo.boot.data.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.boot.GridgoApplication;
import io.gridgo.boot.data.test.data.User;
import io.gridgo.boot.support.annotations.EnableComponentScan;
import io.gridgo.boot.support.annotations.RegistryInitializer;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Registry;

@EnableComponentScan
public class TestDataAccess {

    @Test
    public void testRocksDB() throws InterruptedException {
        doTest("test_rocksdb");
    }

    @Test
    public void testJdbc() throws InterruptedException {
        doTest("test_jdbc");
    }

    protected void doTest(String gateway) throws InterruptedException {
        var app = GridgoApplication.run(TestDataAccess.class);
        var latch = new CountDownLatch(1);
        var exRef = new AtomicReference<Exception>(null);

        app.getContext() //
           .findGatewayMandatory(gateway) //
           .push(Message.ofAny(null)) //
           .always((s, r, e) -> {
               try {
                   if (e != null) {
                       throw e;
                   } else {
                       User user = r.body().asReference().getReference();
                       Assert.assertEquals(1, user.getId());
                       Assert.assertEquals("hello", user.getName());
                   }
               } catch (Exception ex) {
                   exRef.set(ex);
               }

               latch.countDown();
           });

        latch.await();

        if (exRef.get() != null) {
            exRef.get().printStackTrace();
        }

        Assert.assertNull(exRef.get());

        app.stop();
    }

    @RegistryInitializer
    public static void initializeRegistry(Registry registry) {
        registry.register("transformer", "nop");
    }
}
