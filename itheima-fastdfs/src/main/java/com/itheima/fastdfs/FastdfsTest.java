package com.itheima.fastdfs;

import org.csource.fastdfs.*;
import org.junit.Test;

import java.io.IOException;

public class FastdfsTest {
    @Test
    public void test() throws Exception {
        //设置全局的配置
        String conf_filename = ClassLoader.getSystemResource("fastdfs/tracker.conf").getPath();
        ClientGlobal.init(conf_filename);
        //创建追踪服务器客户端
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        //创建storageServer 可以为空
        StorageServer storageServer = null;
        //创建存储服务器客户端
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        /**
         * //上传文件
         * /**
         * * 参数 1：文件
         * * 参数 2：文件的后缀
         * * 参数 3：文件的属性信息
         * * 返回结果：形如：
         * * group1
         * M00/00/00/wKgMqFmfUHiAcpaMAABw0se6LsY441.jpg
         * */
        String[] upload_file = storageClient.upload_file("C:\\Users\\Mr-wang\\Desktop\\pictures\\74b58PICzpW_1024.jpg", ".jpg", null);
        if (upload_file!=null && upload_file.length>0){
            for (String s : upload_file) {
                System.out.println(s);
            }
        }
        //获取存储服务器地址
        String groupName = upload_file[0];
        String fileName = upload_file[1];
        ServerInfo[] serverInfos = trackerClient.getFetchStorages(trackerServer, groupName, fileName);
        for (ServerInfo serverInfo : serverInfos) {
            System.out.println("ip="+serverInfo.getIpAddr()+";port="+serverInfo.getPort());
        }
        //组合可以访问的路径
        String url = "http://"+serverInfos[0].getIpAddr()+"/"+groupName+"/"+fileName;
        System.out.println(url);
    }
}
