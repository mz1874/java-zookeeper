package com.mz.services;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author aiden
 * @data 07/03/2023
 * @description
 */
public class ZkServerWatch {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        List<String> children = null;

        List<String> finalChildren = children;
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 2000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                Event.EventType type = event.getType();
                // current path
                System.out.println(type);
                String path = event.getPath();
                if (type == Event.EventType.NodeChildrenChanged){
                    System.out.println("节点" + path + "上线了");
                }
                if (null != finalChildren){

                }
            }
        });
        Stat exists = zooKeeper.exists("/services", false);
        if (null == exists){
            String path = zooKeeper.create("/services", "storage services".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println(path + "\t 创建成功");
        }

        children = zooKeeper.getChildren("/services", true);
        for (String node : children) {
            System.out.println(node);
        }
        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }
}

class Client {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 2000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {

            }
        });
        Random random = new Random(10000);
        int number = random.nextInt();
        String path = zooKeeper.create("/services/id_" + 4, String.valueOf(number).getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        zooKeeper.getChildren("/services", true);
        TimeUnit.SECONDS.sleep(10);
    }
}
