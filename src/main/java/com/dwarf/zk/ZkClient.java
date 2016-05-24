package com.dwarf.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CuratorFrameworkFactory创建client两个方法：工厂方法newClient,fluent build
 * 启动和关闭：start、close
 * znode4种节点类型：PERSISTENT、PERSISTENT_SEQUENTIAL、EPHEMERAL、EPHEMERAL_SEQUENTIAL
 * todo:添加watcher
 * @author jiyu
 *
 */
public class ZkClient implements ZkOperations {
	
	private static Logger logger = LoggerFactory.getLogger(ZkClient.class);
	
	private CuratorFramework client;
	
	public ZkClient(String connectString){
		this(connectString, new ExponentialBackoffRetry(1000, 3));
	}
	
	/**
	 * 工厂方式创建client
	 * @param connectString
	 * @param retryPolicy
	 */
	public ZkClient(String connectString, RetryPolicy retryPolicy){
		client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
		client.start();
	}
	
	/**
	 * fluent创建client
	 * @param connectString
	 * @param retryPolicy
	 * @param connectionTimeoutMs
	 * @param sessionTimeoutMs
	 */
	public ZkClient(String connectString, RetryPolicy retryPolicy, int connectionTimeoutMs, int sessionTimeoutMs){
		client = CuratorFrameworkFactory.builder()
		            .connectString(connectString)
		            .retryPolicy(retryPolicy)
		            .connectionTimeoutMs(connectionTimeoutMs)
		            .sessionTimeoutMs(sessionTimeoutMs)
		            // etc. etc.
		            .build();
		client.start();
	}
	
	/**
	 * create znode with the given data
	 * @param path
	 * @param payload
	 */
	public void create(String path, byte[] payload){
		try {
			this.client.create().forPath(path, payload);
		} catch (Exception e) {
			logger.info("create znode with the given data exception!", e);
		}
	}
	
	/**
	 * create ephemeral znode with the given data
	 * @param client
	 * @param path
	 * @param payload
	 */
	public void createEphemeral(String path, byte[] payload){
		try {
			this.client.create().withMode(CreateMode.EPHEMERAL).forPath(path, payload);
		} catch (Exception e) {
			logger.info("create ephemeral znode with the given data exception!", e);
		}
	}
	
	/**
	 * create ephemeral sequential znode with the given data
	 * @param client
	 * @param path
	 * @param payload
	 */
	public void createEphemeralSequential(String path, byte[] payload){
		try {
			this.client.create().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, payload);
		} catch (Exception e) {
			logger.info("create ephemeral sequential znode with the given data exception!", e);
		}
	}
	
	/**
	 * delete znode
	 * @param path
	 */
	public void delete(String path){
		try {
			this.client.delete().forPath(path);
		} catch (Exception e) {
			logger.info("delete znode exception!", e);
		}
	}
	
	public void close(){
		CloseableUtils.closeQuietly(this.client);
		this.client.close();
	}
	
	public void addListener(String path, PathChildrenCacheListener listener){
		PathChildrenCache cache = new PathChildrenCache(this.client, path, true);
		try {
			cache.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		cache.getListenable().addListener(listener);
	}
	
	
	public static void main(String[] args) {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		ZkClient zkClient = new ZkClient("120.25.163.237:2181", retryPolicy);
		
		PathChildrenCacheListener listener = new PathChildrenCacheListener(){

			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				if(event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED){
					System.out.println("add event");
				}
			}
			
		};
		zkClient.addListener("/", listener);
		try {
			Thread.sleep(Integer.MAX_VALUE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		zkClient.close();
	}
	
	

}
