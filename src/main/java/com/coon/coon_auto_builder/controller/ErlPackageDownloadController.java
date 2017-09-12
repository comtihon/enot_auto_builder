package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.model.dto.PackageDTO;
import com.coon.coon_auto_builder.domain.ErlPackage;
import com.coon.coon_auto_builder.jpa.service.ErlPackageServiceInterface;
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
    private ErlPackageServiceInterface packageService;

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public void downloadBySearch(HttpServletResponse response, @RequestBody PackageDTO request) throws IOException {
        ErlPackage pack = packageService.getByValues(
                request.getName(), request.getNamespace(), request.getRef(), request.getErl());
        if (pack == null) {
            String errorMessage = "No package for id " + request;
            System.out.println(errorMessage);
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
            outputStream.close();
            return;
        }
        renderPackage(pack, response);
    }

    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    public void downloadById(HttpServletResponse response, @PathVariable String packId) throws IOException {
        Optional<ErlPackage> maybePackage = packageService.findPackage(packId);
        if (!maybePackage.isPresent()) {
            String errorMessage = "No package for id " + packId;
            System.out.println(errorMessage);
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
            outputStream.close();
            return;
        }
        renderPackage(maybePackage.get(), response);
    }

    private void renderPackage(ErlPackage pack, HttpServletResponse response) throws IOException {
        File file = new File(pack.getPath());
        String mimeType = "application/gzip";

        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
        response.setContentLength((int) file.length());

        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        FileCopyUtils.copy(inputStream, response.getOutputStream());
    }
}
