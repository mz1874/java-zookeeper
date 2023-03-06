package com.mz;

import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class ConnectZookeeperTest {

    Log log = LogFactory.get();
    ZooKeeper zooKeeper;

    @Before
    public void connect() throws IOException {
        zooKeeper = new ZooKeeper("linux1:2181,linux2:2181,linux3:2181", 2000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("被触发");
            }
        });
    }


    @Test
    public void createNode() throws InterruptedException, KeeperException {
        String result = zooKeeper.create("/mz", "hello zookeeper".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        log.info(result);
    }

    @Test
    public void deleteNode() throws InterruptedException, KeeperException {

        /**
         * 节点路径
         * 和节点版本号
         */
        zooKeeper.delete("/mz", 0);
    }

    @Test
    public void getData() throws InterruptedException, KeeperException {
        ZooKeeper.States state = zooKeeper.getState();
        byte[] data = zooKeeper.getData("/mz", false, new Stat());
        System.out.println(new String(data));
    }


    @Test
    public void testWatch() throws InterruptedException, KeeperException {
        // 设置监听的时候会触发 原有的监听器 一次生效
        List<String> children = zooKeeper.getChildren("/", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("出发");
            }
        });
//        // 创建节点
//        String s = zooKeeper.create("/sssd", "haha".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Test
    public void testNodeExits() throws InterruptedException, KeeperException {
        Stat exists = zooKeeper.exists("/mz", false);
        System.out.println(JSONUtil.toJsonStr(exists));
    }

    /**
     * before update the Node， use exists function to get the Stat which can be used by update Version
     * @throws InterruptedException
     * @throws KeeperException
     */
    @Test
    public void testSetData() throws InterruptedException, KeeperException {
        Stat exists = zooKeeper.exists("/mz", false);
        if (null != exists){
            Stat resultStat = zooKeeper.setData("/mz", "山本我日你仙人".getBytes(StandardCharsets.UTF_8), exists.getVersion());
            System.out.println(JSONUtil.toJsonStr(resultStat));
        }
    }


}
