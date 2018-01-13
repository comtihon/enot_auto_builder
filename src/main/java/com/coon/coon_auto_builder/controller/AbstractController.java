package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.controller.dto.Renderable;
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

public abstract class AbstractController {

    public static final String BUILD_LOG = "/build_log";
    public static final String DOWNLOAD_ID = "/download/{id}";
    public static final String DOWNLOAD_ERTS = "/download_erts/{version}";
    //TODO all controllers to constants

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
            File file = new File(((Renderable) result.getResponse()).getArtifactPath());
            String mimeType = "application/gzip";

            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
            response.setContentLength((int) file.length());

            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } else {
            renderError(response, (String) result.getResponse());
        }
    }

    private void renderError(HttpServletResponse response, String error) throws IOException {
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(error.getBytes(Charset.forName("UTF-8")));
        outputStream.close();
    }
}
