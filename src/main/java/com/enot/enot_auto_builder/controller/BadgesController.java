package com.enot.enot_auto_builder.controller;

import com.enot.enot_auto_builder.controller.dto.PackageDTO;
import com.enot.enot_auto_builder.controller.dto.ResponseDTO;
import com.enot.enot_auto_builder.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.concurrent.CompletableFuture;

@Controller
public class BadgesController {
    @Autowired
    private SearchService searchService;
    @Value("${service.host}")
    private String serviceHost;

    /**
     * @param fullName git package namespace + nameEx. comtihon/enot
     * @return url  https://img.shields.io/badge/enot-<VSN>-green.svg?link=<Link-to-enothub>
     */
    @GetMapping(path = "/badge")
    public CompletableFuture<RedirectView> badgeForService(
            @RequestParam(name = "full_name") String fullName) {
        CompletableFuture<ResponseDTO> build = searchService.fetchLastSuccessfulVersion(fullName);
        return build.thenApply(responseDTO -> {
            String url;
            if (responseDTO.isResult()) {
                PackageDTO data = (PackageDTO) responseDTO.getResponse();
                url = "https://img.shields.io/badge/enot-" + data.getVersion()
                        + "-green.svg"
                        + "?link=" + serviceHost + data.getPath();
            } else
                url = "https://img.shields.io/badge/enot-unknown-red.svg";
            return new RedirectView(url);
        });

    }
}
