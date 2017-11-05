insert into repository (url, name, namespace) values('url1', 'foo', 'namespace1');
insert into repository (url, name, namespace) values('url2', 'bar', 'namespace2');
insert into repository (url, name, namespace) values('url3', 'baz', 'namespace3');
insert into repository (url, name, namespace) values('url4', 'foo', 'namespace4');
insert into repository (url, name, namespace) values('url5', 'bar', 'namespace5');

insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id1', 'url1', '1.0.0', '18');
insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id2', 'url2', '1.0.0', '18');
insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id3', 'url3', '1.0.0', '18');
insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id4', 'url4', '1.0.0', '18');
insert into package_versions (version_id, repository_url, ref, erl_version) values('version_id5', 'url5', '1.0.0', '18');

insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id1', 'version_id1', 'true', 'artifact', 'path1', now());
 insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id2', 'version_id2', 'true', 'artifact', 'path1', now());
 insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id3', 'version_id3', 'true', 'artifact', 'path1', now());
 insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id4', 'version_id4', 'true', 'artifact', 'path1', now());
 insert into builds (build_id, package_version_id, result, message, artifact_path, created_date)
 values('build_id5', 'version_id5', 'true', 'artifact', 'path1', now());