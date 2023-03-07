package com.mz;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.io.IOException;

/**
 * @author aiden
 * @data 07/03/2023
 * @description
 */
public class ConnectToMacTest {

    @Test
    public void testConnection() throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 2000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {

            }
        });
        ZooKeeper.States state = zooKeeper.getState();
        System.out.println(state);

        Stat exists = zooKeeper.exists("/services", false);
        zooKeeper.delete("/services", exists.getVersion());



    }
}
