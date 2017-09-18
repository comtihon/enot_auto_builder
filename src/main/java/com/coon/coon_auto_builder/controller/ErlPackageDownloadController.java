package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.data.dao.BuildDAOService;
import com.coon.coon_auto_builder.data.dto.PackageDTO;
import com.coon.coon_auto_builder.data.model.BuildBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Optional;

@Controller
public class ErlPackageDownloadController {

    @Autowired
    BuildDAOService buildDao;

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public void downloadBySearch(HttpServletResponse response, @RequestBody PackageDTO request) throws IOException {
        Optional<BuildBO> maybeResult = buildDao.findByValues(
                request.getName(), request.getNamespace(), request.getRef(), request.getErl());
        if (!maybeResult.isPresent()) {
            String errorMessage = "No package for id " + request;
            System.out.println(errorMessage);
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
            outputStream.close();
            return;
        }
        renderPackage(maybeResult.get(), response);
    }

    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    public void downloadById(HttpServletResponse response, @PathVariable String id) throws IOException {
        Optional<BuildBO> maybeResult = buildDao.find(id);
        if (!maybeResult.isPresent()) {
            String errorMessage = "No result for id " + id;
            System.out.println(errorMessage);
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
            outputStream.close();
            return;
        }
        renderPackage(maybeResult.get(), response);
    }

    private void renderPackage(BuildBO result, HttpServletResponse response) throws IOException {
        File file = new File(result.getArtifactPath());
        String mimeType = "application/gzip";

        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
        response.setContentLength((int) file.length());

        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        FileCopyUtils.copy(inputStream, response.getOutputStream());
    }
}
