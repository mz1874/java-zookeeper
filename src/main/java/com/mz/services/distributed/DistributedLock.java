package com.mz.services.distributed;

import com.mz.services.constant.ZookeeperConstant;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.mz.services.constant.ZookeeperConstant.ZOOKEEPER_TIME_OUT;

/**
 * @author 23391
 */
public class DistributedLock {

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new ZooKeeper(ZookeeperConstant.ZOOKEEPER_URL, ZOOKEEPER_TIME_OUT, new Watcher() {
            @Override
            public void process(WatchedEvent event) {

            }
        });
        Stat exists = zooKeeper.exists("/locks", false);
        // create node locks
        if (null == exists) {
            zooKeeper.create("/locks", "lock".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }
}
