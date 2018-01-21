package com.coon.coon_auto_builder;

import com.coon.coon_auto_builder.service.build.Builder;
import com.coon.coon_auto_builder.service.git.ClonedRepo;
import com.coon.coon_auto_builder.service.git.GitService;
import com.coon.coon_auto_builder.service.loader.Loader;
import com.coon.coon_auto_builder.service.mail.MailSenderService;
import com.coon.coon_auto_builder.service.tool.Coon;
import com.coon.coon_auto_builder.service.tool.Erlang;
import com.coon.coon_auto_builder.service.tool.Kerl;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Matchers.any;

public abstract class IntegrationTest {

    @Value(value = "classpath:template.app.src")
    private Resource templateAppSrc;

    public void setUp(GitService gitService,
                      Coon coon,
                      Kerl kerl,
                      Loader loader,
                      String erlangVersion,
                      CountDownLatch startSearch,
                      MailSenderService mailSender) throws Exception {
        //mock repo clone
        Mockito.when(gitService.cloneRepo(
                any(),
                any(),
                any()))
                .thenReturn(new ClonedRepo("valerii.tikhonov@gmail.com", Paths.get("test/tmp/test")));
        Mockito.when(gitService.getClonedPaths(any(), any())).thenReturn(new ArrayList<>());
        //mock repo build
        Mockito.doNothing().when(coon).build(any(), any());
        //mock package loading
        Mockito.doAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            Builder repo = (Builder) args[0];
            return repo.getName() + "/" + repo.getErlang() + "/" + repo.getName() + ".cp";
        }).when(loader).loadArtifact(any());
        //mock email sending
        Mockito.doAnswer((Answer<Void>) invocation -> {
            startSearch.countDown();
            return null;
        }).when(mailSender).sendReport(any());
        //mock kerl
        Map<String, Erlang> erlInstallations = new HashMap<>();
        erlInstallations.put("18", new Erlang("18", "path/to/18", "/artifacts"));
        erlInstallations.put("19", new Erlang("19", "path/to/19", "/artifacts"));
        erlInstallations.put("20", new Erlang("20", "path/to/20", "/artifacts"));
        erlInstallations.put(erlangVersion, new Erlang(erlangVersion, "path/to/" + erlangVersion, "/artifacts"));
        Mockito.when(kerl.getErlInstallations()).thenReturn(erlInstallations);
    }

    // Create application with name/coonfig.json and name/ebin/name.app
    protected void writeApp(String pathStr, String name, String vsn, String conf) throws IOException {
        Paths.get(pathStr, name, "ebin").toFile().mkdirs();
        Path coonfig = Paths.get(pathStr, name, "coonfig.json");
        byte[] strToBytes = conf.getBytes();
        Files.write(coonfig, strToBytes);
        InputStream inputStream = templateAppSrc.getInputStream();
        String appFile = IOUtils.toString(inputStream, "UTF-8");
        appFile = appFile.replaceAll("#\\{name}", name);
        appFile = appFile.replaceAll("#\\{vsn}", vsn);
        Path appConf = Paths.get(pathStr, name, "ebin", name + ".app");
        strToBytes = appFile.getBytes();
        Files.write(appConf, strToBytes);
    }
}
