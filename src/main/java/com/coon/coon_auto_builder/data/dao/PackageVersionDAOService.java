package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.entity.PackageVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
public class PackageVersionDAOService {
    @Autowired
    private PackageVersionDAO dao;

    public void save(PackageVersion pack) {
        PackageVersion saved = dao.save(pack);
        if (saved != null) {
            pack.setVersionId(saved.getVersionId());
        }
    }

    @Transactional
    public void saveIfNotExists(PackageVersion pack) {
        Optional<String> found = findIdByRefAndErlVersionAndRepository(pack);
        if (found.isPresent()) {
            pack.setVersionId(found.get());
        } else {
            save(pack);
        }
    }

    public Optional<PackageVersion> find(String resId) {
        return Optional.ofNullable(dao.findOne(resId));
    }

    public Collection<PackageVersion> getAll() {
        Iterable<PackageVersion> itr = dao.findAll();
        return (Collection<PackageVersion>) itr;
    }

    public Optional<String> findIdByRefAndErlVersionAndRepository(PackageVersion versionBO) {
        return Optional.ofNullable(dao.findByRefAndErlVersionAndRepository(
                versionBO.getRef(),
                versionBO.getErlVersion(),
                versionBO.getRepository().getUrl()));
    }
}
