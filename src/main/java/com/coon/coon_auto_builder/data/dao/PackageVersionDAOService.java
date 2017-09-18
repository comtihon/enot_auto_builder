package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.model.PackageVersionBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
public class PackageVersionDAOService {
    @Autowired
    PackageVersionDAO dao;

    @Autowired
    RepositoryDAOService repositoryDAOService;

    public void save(PackageVersionBO pack) {
        pack.setRepository(repositoryDAOService.saveIfNotExists(pack.getRepository()));
        PackageVersionBO saved = dao.save(pack);
        if (saved != null) {
            pack.setVersionId(saved.getVersionId());
        }
    }

    @Transactional
    public void saveIfNotExists(PackageVersionBO pack) {
        Optional<PackageVersionBO> found = findByRefAndErlVersionAndRepository(pack);
        if(found.isPresent()) {
            pack.setVersionId(found.get().getVersionId());
        } else {
            save(pack);
        }
    }

    public Optional<PackageVersionBO> find(String resId) {
        return Optional.ofNullable(dao.findOne(resId));
    }

    public Collection<PackageVersionBO> getAll() {
        Iterable<PackageVersionBO> itr = dao.findAll();
        return (Collection<PackageVersionBO>) itr;
    }

    public Optional<PackageVersionBO> findByRefAndErlVersionAndRepository(PackageVersionBO versionBO) {
        return Optional.ofNullable(dao.findByRefAndErlVersionAndRepository(
                versionBO.getRef(),
                versionBO.getErlVersion(),
                versionBO.getRepository().getUrl()));
    }
}
