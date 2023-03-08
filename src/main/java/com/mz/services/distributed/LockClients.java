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
public class LockClients {
    static ZooKeeper zooKeeper = null;

    private CountDownLatch server = new CountDownLatch(1);

    //ZooKeeper 连接
    private CountDownLatch connectLatch = new CountDownLatch(1);
    public Integer id;

    public LockClients(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public void lock() throws InterruptedException, KeeperException, IOException {
        System.out.println(Thread.currentThread().getName() +"\t"+ id);
        zooKeeper = new ZooKeeper(ZookeeperConstant.ZOOKEEPER_URL_MAC, 99999999, new Watcher() {
            @Override
            public void process(WatchedEvent event) {

                // connectLatch  如果连接上zk  可以释放
                if (event.getState() == Event.KeeperState.SyncConnected){
                    connectLatch.countDown();
                }
                if (event.getType() == Event.EventType.NodeDeleted) {
                    System.out.println(Thread.currentThread().getName() + event.getPath() + "删除事件释放锁");
                    server.countDown();
                }
            }
        });
        connectLatch.await();

        String currentPath = zooKeeper.create("/locks/seq_" + id, String.valueOf(id).getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println(Thread.currentThread().getName() + "节点" + "\t" + currentPath + "注册成功");
        List<String> children = zooKeeper.getChildren("/locks", false);
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
            return;
        } else {
            // 如果不是则监视上一个节点
            int i = idList.indexOf(id);
            zooKeeper.getChildren("/locks/seq_" + idList.get(i - 1), true);
            System.out.println(Thread.currentThread().getName() + "PATH " + id + "等待");
            server.await();
        }

    }

    public void unlock() throws InterruptedException, KeeperException {
        System.out.println(Thread.currentThread().getName() + id + "删除");
        Stat exists = zooKeeper.exists("/locks/seq_" + id, true);
        zooKeeper.delete("/locks/seq_" + id, exists.getVersion());
    }

    public static void getAllChilds() throws InterruptedException, KeeperException, IOException {
        zooKeeper = new ZooKeeper(ZookeeperConstant.ZOOKEEPER_URL_MAC, 99999999, new Watcher() {
            @Override
            public void process(WatchedEvent event) {

            }
        });

        List<String> children = zooKeeper.getChildren("/locks", false);
        System.out.println("--------------");
        children.forEach(e->{
            System.out.println(e);
        });
        System.out.println("--------------");

    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

        getAllChilds();

        LockClients lockClients1 = new LockClients(1);


        LockClients lockClients2 = new LockClients(2);


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lockClients1.lock();
                    TimeUnit.SECONDS.sleep(2);
                    lockClients1.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lockClients2.lock();
                    TimeUnit.SECONDS.sleep(2);
                    lockClients2.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
