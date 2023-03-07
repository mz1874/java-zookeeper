package com.mz.services;

import cn.hutool.core.lang.UUID;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.mz.services.ZkServerWatch.URL;

/**
 * @author aiden
 * @data 07/03/2023
 * @description
 */
public class ZkServerWatch {

    final static String URL = "linux1:2181,linux2:2181,linux3:2181";
    static ZooKeeper zooKeeper;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
         zooKeeper = new ZooKeeper(URL, 2000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                try {
                    List<String> zooKeeperChildren = zooKeeper.getChildren("/services", true);
                    System.out.println("当前存在的服务器节点");
                    for (String zooKeeperChild : zooKeeperChildren) {
                        System.out.println(zooKeeperChild);
                    }
                    System.out.println("---------------------------------------------------");
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        Stat exists = zooKeeper.exists("/services", false);
        if (null == exists){
            String path = zooKeeper.create("/services", "storage services".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println(path + "\t 创建成功");
        }
        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }
}

class Client {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new ZooKeeper(URL, 2000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {

            }
        });
        UUID uuid = UUID.fastUUID();
        zooKeeper.create("/services/id_" + uuid, uuid.toString().getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        List<String> children = zooKeeper.getChildren("/services", true);
        System.out.println("当前存在的服务器节点");
        for (String zooKeeperChild : children) {
            System.out.println(zooKeeperChild);
        }
        System.out.println("---------------------------------------------------");
        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }
}
