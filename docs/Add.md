# Howto add package to EnotHub.
## Manual
* Visit [EnotHub](https://enot.justtech.blog) and push `Add build`
button.
* Use your git user name or project's namespace as `namespace` and your
repo name as `name`.
* Fill in your project's git url.
* Select tags to build. By default last tag will be built.
* Select Erlang version to be used for build  _(optional)_.
`default_erlang` from `application.conf` will be used by default.
* Push `Add` button.

## Automatic (github)
* Install [enot](https://github.com/apps/enot) github application for your
repo
* push new git tag to your repo

## Email notification notice
Notification email is sent to the last commit's author email. If you
don't receive one - check if last email is yours.

## How to add deps of my package?
All dependencies of your package, mentioned in `enot_config.json` will be built after you package's build succeed.  
__Important__: Enot will build dependencies of dependencies only if they contain `enot_config.json`. Otherwise only first
level of dependencies will be built.

## Enot Badge
To add [badges](shields.io) for your service use `https://enot.justtech.blog/badge?full_name=<YOUR_PACKAGES_FULL_NAME>`
url.  
Example:

    https://enot.justtech.blog/badge?full_name=comtihon/mongodb-erlang
Will display your package's version from enot.