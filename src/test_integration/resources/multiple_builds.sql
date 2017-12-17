insert into repository (url, name, namespace) values('url1', 'name1', 'namespace1');

insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id1', 'url1', '1.0.0', '18');

insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id1', 'version_id1', 'false', 'artifact', 'no_artifact', '2017-12-22');
insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id2', 'version_id1', 'true', 'artifact', 'path', '2017-12-23');
insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id3', 'version_id1', 'true', 'artifact', 'path', '2017-12-24');
insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id4', 'version_id1', 'true', 'artifact', 'path', '2017-12-25');
