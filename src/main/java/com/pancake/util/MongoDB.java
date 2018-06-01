package com.pancake.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.pancake.entity.util.NetAddress;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chao on 2018/6/1.
 */
public class MongoDB {
    private final static Logger logger = LoggerFactory.getLogger(MongoDB.class);
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoDB() {

    }

    public MongoDB(String database) {
        mongoClient = new MongoClient("localhost", 27017);
        MongoClientOptions.Builder options = new MongoClientOptions.Builder();
        // options.autoConnectRetry(true);// 自动重连true
        // options.maxAutoConnectRetryTime(10); // the maximum auto connect retry time
        options.connectionsPerHost(300);// 连接池设置为300个连接,默认为100
        options.connectTimeout(15000);// 连接超时，推荐>3000毫秒
        options.maxWaitTime(5000); //
        options.socketTimeout(0);// 套接字超时时间，0无限制
        options.threadsAllowedToBlockForConnectionMultiplier(5000);// 线程队列数，如果连接线程排满了队列就会抛出“Out of semaphores to get db”错误。
        options.writeConcern(WriteConcern.ACKNOWLEDGED);//
        options.build();
        mongoDatabase = mongoClient.getDatabase(database);
    }

    public MongoDB(NetAddress mongoAddr, String database) {
        mongoClient = new MongoClient(mongoAddr.getIp(), mongoAddr.getPort());
        MongoClientOptions.Builder options = new MongoClientOptions.Builder();
        // options.autoConnectRetry(true);// 自动重连true
        // options.maxAutoConnectRetryTime(10); // the maximum auto connect retry time
        options.connectionsPerHost(300);// 连接池设置为300个连接,默认为100
        options.connectTimeout(15000);// 连接超时，推荐>3000毫秒
        options.maxWaitTime(5000); //
        options.socketTimeout(0);// 套接字超时时间，0无限制
        options.threadsAllowedToBlockForConnectionMultiplier(5000);// 线程队列数，如果连接线程排满了队列就会抛出“Out of semaphores to get db”错误。
        options.writeConcern(WriteConcern.ACKNOWLEDGED);//
        options.build();
        mongoDatabase = mongoClient.getDatabase(database);
    }

    /**
     * 获取集合 collectionName 中的记录数
     *
     * @param collectionName
     * @return
     */
    public long countRecords(String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        return collection.count();
    }

    /**
     * 根据 key 获取所有对应的value
     *
     * @param key
     * @param collectionName
     * @return
     */
    @SuppressWarnings("Duplicates")
    public Set<String> findValuesByKey(String key, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        MongoCursor<String> mongoCursor = collection.distinct(key, String.class).iterator();
        Set<String> blockIdSet = new HashSet<String>();
        while (mongoCursor.hasNext()) {
            blockIdSet.add(mongoCursor.next());
        }
        return blockIdSet;
    }

    /**
     * 根据 key 统计去重统计所有value的个数
     *
     * @param key
     * @param collectionName
     * @return
     */
    public int countValuesByKey(String key, String collectionName) {
        return this.findValuesByKey(key, collectionName).size();
    }

}
