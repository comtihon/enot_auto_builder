# Howto add package to coonhub.
## Manual
* Visit [CoonHub](https://coon.justtech.blog) and push `Add build`
button.
* Use your git user name or project's namespace as `namespace` and your
repo name as `name`.
* Fill in your project's git url.
* Select tags to build. By default last tag will be built.
* Select Erlang version to be used for build  _(optional)_.
`default_erlang` from `application.conf` will be used by default.
* Push `Add` button.

## Automatic (github)
* Install [coon](https://github.com/apps/coon) github application for your
repo
* push new git tag to your repo

## Email notification notice
Notification email is sent to the last commit's author email. If you
don't receive one - check if last email is yours.

## How to add deps of my package?
All dependencies of your package, mentioned in `coonfig.json` will be built after you package's build succeed.  
__Important__: Coon will build dependencies of dependencies only if they contain `coonfig.json`. Otherwise only first
level of dependencies will be built.
