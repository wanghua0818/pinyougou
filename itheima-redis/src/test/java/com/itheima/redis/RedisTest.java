package com.itheima.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Set;

@ContextConfiguration(value = {"classpath:spring/applicationContext-redis.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    //string
    @Test
    public void stringTest(){
        redisTemplate.boundValueOps("string_key").set("安之若素");
        Object string_key = redisTemplate.boundValueOps("string_key").get();
        System.out.println(string_key);
    }
    //hash
    @Test
    public void hashTest(){
        redisTemplate.boundHashOps("hash_key").put("h_k1","a");
        redisTemplate.boundHashOps("hash_key").put("h_k2","b");
        redisTemplate.boundHashOps("hash_key").put("h_k3","c");
        List list = redisTemplate.boundHashOps("hash_key").values();
        System.out.println(list);
    }
    //list
    @Test
    public void listTest(){
        redisTemplate.boundListOps("list_key").leftPush("b");
        redisTemplate.boundListOps("list_key").leftPush("a");
        redisTemplate.boundListOps("list_key").rightPush("c");
        List list_key = redisTemplate.boundListOps("list_key").range(0, -1);
        System.out.println(list_key);
    }
    //set
    @Test
    public void setTest(){
        redisTemplate.boundSetOps("set_key").add("a");
        redisTemplate.boundSetOps("set_key").add("b");
        redisTemplate.boundSetOps("set_key").add("c");
        Set set_key = redisTemplate.boundSetOps("set_key").members();
        System.out.println(set_key);
    }
    //sortedSet
    @Test
    public void sortedSetTest(){
        redisTemplate.boundZSetOps("zset_key").add("a",10);
        redisTemplate.boundZSetOps("zset_key").add("b",20);
        redisTemplate.boundZSetOps("zset_key").add("c",30);
        Set zset_key = redisTemplate.boundZSetOps("zset_key").range(0, -1);
        System.out.println(zset_key);
    }

}
