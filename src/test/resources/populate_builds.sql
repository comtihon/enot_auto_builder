insert into repository (url, name, namespace) values('url1', 'name1', 'namespace1');
insert into repository (url, name, namespace) values('url2', 'name2', 'namespace2');

insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id1', 'url1', '1.0.0', '18');
insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id2', 'url1', '1.0.0', '20');
insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id3', 'url1', '1.0.1', '18');
insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id4', 'url2', '1.0.0', '18');

insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id1', 'version_id1', 'true', 'artifact', 'path1', GETDATE());
insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id2', 'version_id1', 'true', 'artifact', 'path2', GETDATE());
insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id3', 'version_id2', 'true', 'artifact', 'path3', GETDATE());
insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id4', 'version_id2', 'true', 'artifact', 'path4', GETDATE());
insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id5', 'version_id3', 'true', 'artifact', 'path5', GETDATE());
insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id6', 'version_id4', 'true', 'artifact', 'path6', GETDATE());