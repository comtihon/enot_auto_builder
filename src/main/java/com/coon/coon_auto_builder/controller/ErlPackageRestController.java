package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.model.dto.BuildRequestDTO;
import com.coon.coon_auto_builder.model.dto.PackageDTO;
import com.coon.coon_auto_builder.domain.ErlPackage;
import com.coon.coon_auto_builder.jpa.service.ErlPackageServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ErlPackageRestController {

    @Autowired
    private ErlPackageServiceInterface packageService;

    @RequestMapping(path = "/search", method = RequestMethod.POST)
    public List<PackageDTO> listBySearch(@RequestBody PackageDTO request) throws IOException {
        List<ErlPackage> packages = packageService.findByValues(
                request.getName(), request.getNamespace(), request.getRef(), request.getErl());
        packages.forEach(PackageDTO::new);
        List<PackageDTO> response = new ArrayList<>(packages.size());
        for (ErlPackage erlPackage : packages)
            response.add(new PackageDTO(erlPackage));
        return response;
    }
}
