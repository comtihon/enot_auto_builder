# Coon build and load server [![Build Status](https://travis-ci.org/comtihon/coon_auto_builder.svg?branch=master)](https://travis-ci.org/comtihon/coon_auto_builder)[![Docker Automated build](https://img.shields.io/docker/build/comtihon/coon_auto_builder.svg)](https://hub.docker.com/r/comtihon/coon_auto_builder)  
Automated service for building and storing Erlang packages. Can be treat as a maven/pip for Erlang.  

# Adding your package to Coon
Package can be built manually via [Coon](https://coon.justtech.blog) service.

# Running your own Coon build server
## Standalone
Requirements:
- postgres
- EMAIL_USER env var set up
- EMAIL_PASSWORD env var set up
- GIT_SECRET is Coon's git application secret
- kerl installed
- coon installed

## Monitoring
Monitoring is made via [collectd](https://collectd.org/) and `/metrics` endpoint. 

## Docker
You can run all services with docker-compose. See `docker-compose.yml` for details.

# Protocol
## Build
POST __/buildAsync__ - build request
BODY:

    {
        "full_name": <fullName>,
        "clone_url": <cloneUrl>,
        "versions": 
        [
            {
                "ref": <ref>,
                "erl_version": <erlVersion>
            }
        ]
    }
Where:  
`fullName` is a fullname of a repository, containing namespace and repo name. Ex. "comtihon/coon"  
`cloneUrl` is a url for repository clone.  
`ref` is a version to be built. Ex: "1.0.0".  
`erl_version` _(optional)_ is an Erlang version used for build. Ex: "19".    

POST __/rebuild__ - request a specific build to be rebuilt  
BODY:

    {
        "build_id" : <buildId>
    }
Where:
`buildId` is an id of a build for rebuild

POST __/callback__ - build request from Github.  
Header: `x-hub-signature` with request signature.  
BODY:

    {
        "repository": 
        {
            "full_name": <fullName>
            "clone_url": <cloneUrl>
        },
        "ref": <ref>,
        "ref_type": <refType>
    }
Where:  
`refType` is a type of reference. Only `tag` is supported.
## Search
GET __/search__ - search for packages.  
PARAMETERS:  
`name` - package name  
`namespace` - package's namespace (GitHub fork name)  
`version` - package's version  
`erl_version` - Erlang version  
RESPONSE:  

    {
        "result" : <result>,
        "response" : 
        [
            {
                "build_id" : <build_id>,
                "namespace" : <namespace>,
                "name" : <name>,
                "success" : <success>,
                "path" : <path>,
                "build_date" : <date>,
                "version" : <version>,
                "erl_version" : <erl_version>
             }
             ...
        ]
    }

POST __/builds__ - get a list of builds, available for download. Skip errored builds.    
BODY:

    {
        "full_name": <fullName>,
        "versions": 
        [
            {
                "ref": <ref>,
                "erl_version": <erlVersion>
            }
        ]
    }
Where:  
`ref` and `erlVersion` and `versions` are optional.  
RESPONSE:

    {
        "result": <boolean_result>,
        "response":
        [
            {"build_id" : <Id>, "result" : true, "message" : "", "artifact_path" : <Path>, "created_date" : <Date>}
            ...
        ]
    }
Where:  
`response` will be just string error message in case of `result` is not __true__.  

POST __/versions__ - get a list of versions, available for download. Skip versions without successful builds.  
BODY: same as `/builds`  
RESPONSE:

    {
        "result": <boolean_result>,
        "response":
        [
            {"versionId" : <Id>, "ref" : <ref>, "erl_version" : <Erl>}
            ...
        ]
    }

GET __/last_builds__ - get last n built packages.
PARAMETERS:
`n` - N packages to return
RESPONSE:

    {
        "result" : <result>,
        "response" :
        [
            {
                "build_id" : <build_id>,
                "namespace" : <namespace>,
                "name" : <name>,
                "success" : <success>,
                "path" : <path>,
                "build_date" : <date>,
                "version" : <version>,
                "erl_version" : <erl_version>
             }
             ...
        ]
    }
## Download

GET __/download/{id}__ - download artifact by id.  

POST __/get__ - try to download artifact by name, ref and version. If there is multiple - the last built
will be fetched.    
BODY:


    {
        "full_name": <fullName>,
        "versions": 
        [
            {
                "ref": <ref>,
                "erl_version": <erlVersion>
            }
        ]
    }
    
GET __/build_log/__ - get build's log.  
PARAMETERS:  
`build_id` - id of a build.  
RESPONSE:

    raw txt log
