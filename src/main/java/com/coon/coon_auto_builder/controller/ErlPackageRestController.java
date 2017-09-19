package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.data.dao.BuildDAOService;
import com.coon.coon_auto_builder.data.dto.PackageDTO;
import com.coon.coon_auto_builder.data.model.BuildBO;
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
    BuildDAOService buildDao;

    @RequestMapping(path = "/search", method = RequestMethod.POST)
    public List<PackageDTO> listBySearch(@RequestBody PackageDTO request) throws IOException {
        List<BuildBO> builds = buildDao.fetchByValues(
                request.getName(), request.getNamespace(), request.getRef(), request.getErl());
        List<PackageDTO> response = new ArrayList<>(builds.size());
        for (BuildBO build : builds)
            response.add(build.toDTO());
        return response;
    }
}
