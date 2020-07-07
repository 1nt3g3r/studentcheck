package com.intgroup.htmlcheck;

import com.intgroup.htmlcheck.feature.taskcheck.TaskCheckService;
import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.RemoteCheckServerService;
import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.RemoteCheckServers;
import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.RemoteServerCacheFilesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class RemoteServerCacheFilesServiceTest {
    @Autowired
    private RemoteCheckServers remoteCheckServers;

    @Test
    public void testDownload() throws IOException, InterruptedException {
      boolean taskExists = remoteCheckServers.isValidTaskId("js-demo-1");
      System.out.println(taskExists);
        //
    }
}
