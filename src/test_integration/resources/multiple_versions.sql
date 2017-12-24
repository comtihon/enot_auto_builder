insert into repository (url, name, namespace) values('url1', 'name1', 'namespace1');

insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id1', 'url1', '1.0.0', '18');
insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id2', 'url1', '1.0.1', '18');
insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id3', 'url1', '1.0.2', '18');
insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id4', 'url1', '1.0.3', '18');

insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id1', 'version_id1', 'false', 'something is wrong...', '1.0.0/path', '2017-12-22');
insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id2', 'version_id2', 'true', 'artifact', '1.0.1/path', '2017-12-23');
insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id3', 'version_id3', 'true', 'artifact', '1.0.2/path', '2017-12-24');
insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id4', 'version_id4', 'true', 'artifact', '1.0.3/path', '2017-12-25');
