# Coon build and load server [![Build Status](https://travis-ci.org/comtihon/coon_auto_builder.svg?branch=master)](https://travis-ci.org/comtihon/coon_auto_builder)
Automated service for building and storing Erlang packages. Can be treat as a maven/pip for Erlang.  
This service is a replacement for deprecated [octocoon](https://github.com/comtihon/octocoon).  

# Adding your package to Coon
(TODO)

# Running your own Coon build server
(TODO)  
Requirements:
- postgres
- EMAIL_USER env var set up
- EMAIL_PASSWORD env var set up
- kerl installed (TODO make build server install kerl automatically)
- coon installed (TODO make build server install coon automatically)

# Protocol
## Build
POST __/buildAsync__ - build request
BODY:

    {
        repository: 
        {
            full_name: <fullName>,
            clone_url: <cloneUrl>,
            versions: 
            [
                {
                    ref: <ref>,
                    erl_version: <erlVersion>
                }
            ]
        }
    }
Where:  
`fullName` is a fullname of a repository, containing namespace and repo name. Ex. "comtihon/coon"  
`cloneUrl` is a url for repository clone.  
`ref` is a version to be built. Ex: "1.0.0".  
`erl_version` _(optional)_ is an Erlang version used for build. Ex: "19".    

POST __/rebuild__ - request a specific build to be rebuilt  
BODY:

    {
        build_id : <buildId>
    }
Where:
`buildId` is an id of a build for rebuild

POST __/callback__ - build request from Github.  
Header: `x-hub-signature` with request signature.  
BODY:

    {
        repository: 
        {
            full_name: <fullName>
            clone_url: <cloneUrl>
        },
        ref: <ref>,
        ref_type: <refType>
    }
Where:  
`refType` is a type of reference. Only `tag` is supported.
## Download
POST __/search__ - get a list of builds, available for download.  
BODY:

    {
        repository: 
        {
            full_name: <fullName>,
            versions: 
            [
                {
                    ref: <ref>,
                    erl_version: <erlVersion>
                }
            ]
        }
    }
Where:  
`ref` and `erlVersion` and `versions` are optional.  
GET __/download/{id}__ - download artifact by id.  

POST __/get__ - try to download artifact by name, ref and version. If there is multiple - the last built
will be fetched.    
BODY:


    {
        repository: 
        {
            full_name: <fullName>,
            versions: 
            [
                {
                    ref: <ref>,
                    erl_version: <erlVersion>
                }
            ]
        }
    }