package com.mz.services.distributed;

import com.mz.services.constant.ZookeeperConstant;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.mz.services.constant.ZookeeperConstant.ZOOKEEPER_TIME_OUT;

/**
 * 分布式锁资源获取对象
 *
 * @author Wang
 */
public class LockClients implements Runnable {
    static ZooKeeper zooKeeper = null;

    CountDownLatch server = new CountDownLatch(1);

    public Integer id;

    public LockClients(Integer id) {
        this.id = id;
    }

    public void getConnection() throws IOException {
        zooKeeper = new ZooKeeper(ZookeeperConstant.ZOOKEEPER_URL, 99999999, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                try {
                    go();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (KeeperException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void registerToZookeeper(Integer path) throws InterruptedException, KeeperException {
        String currentPath = zooKeeper.create("/locks/seq_" + path, String.valueOf(path).getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println("节点" + "\t" + currentPath + "注册成功");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    @Override
    public void run() {
        try {
            getConnection();
            registerToZookeeper(this.id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getResource() throws InterruptedException {
        server.countDown();
        System.out.println("ID" + id + "\t 获取到锁对象， 开始执行");
        TimeUnit.SECONDS.sleep(1);
        System.out.println("ID" + id + "\t ， 执行完毕");
    }


    public void go() throws InterruptedException, KeeperException {

        List<String> children = zooKeeper.getChildren("/locks", true);
        List<Integer> idList = new ArrayList<>();
        for (String child : children) {
            String substring = child.substring(4, child.length());
            idList.add(Integer.valueOf(substring));
        }
        idList.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        // 如果当前是最小节点 则获得资源
        if (idList.get(0).equals(this.id)) {
            getResource();
            deleteNode(this.id);
        } else {
            // 如果不是则监视上一个节点
            int i = idList.indexOf(id);
            zooKeeper.getChildren("/locks/seq_" + idList.get(i - 1), true);
            server.await();
        }

    }

    public void deleteNode(Integer path) throws InterruptedException, KeeperException {
        System.out.println("当前正在删除的ID" + id);
        Stat exists = zooKeeper.exists("/locks/seq_" + id, false);
        if (null != exists) {
            zooKeeper.delete("/locks/seq_" + id, exists.getVersion());
        } else {
            System.out.println("删除失败节点不存在");
        }
        System.out.println("ID" + id + "删除完毕");
        System.out.println("当前线程" + Thread.currentThread().getName());
        System.out.println("当前线程" + Thread.currentThread().getState());
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        LockClients lockClients1 = new LockClients(1);
        Thread thread = new Thread(lockClients1);
        thread.start();

        LockClients lockClients2 = new LockClients(2);
        Thread thread1 = new Thread(lockClients2);
        thread1.start();
//        LockClients lockClients3 = new LockClients(3);
//        Thread thread2 = new Thread(lockClients3);
//        thread2.start();
//        LockClients lockClients4 = new LockClients(4);
//        Thread thread3 = new Thread(lockClients4);
//        thread3.start();
//        LockClients lockClients5 = new LockClients(5);
//        Thread thread4 = new Thread(lockClients5);
//        thread4.start();

        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }

}
