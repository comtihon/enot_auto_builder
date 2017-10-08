package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

abstract class AbstractController {

    private static final MediaType CONTENT_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8);

    ResponseEntity<ResponseDTO<?>> returnResult(ResponseDTO<?> result) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(CONTENT_TYPE);
        if (result.isResult())
            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        return new ResponseEntity<>(result, headers, HttpStatus.BAD_REQUEST);
    }

    void renderPackage(ResponseDTO result, HttpServletResponse response) throws IOException {
        if (result.isResult()) {
            File file = new File(((BuildDTO) result.getResponse()).getArtifactPath());
            String mimeType = "application/gzip";

            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
            response.setContentLength((int) file.length());

            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } else {
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(((String) result.getResponse()).getBytes(Charset.forName("UTF-8")));
            outputStream.close();
        }
    }
}
