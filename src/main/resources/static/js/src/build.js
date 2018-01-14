
function Build(name, git_path, package_versions, erl_versions, notify_email, build_sync) {
    var self = this;
    self.name = ko.observable(name);
    self.git_path = ko.observable(git_path);
    self.package_versions = ko.observable(package_versions);
    self.erl_versions = ko.observable(erl_versions);
    self.error = ko.observable("Error occurred");
    self.is_notify_email = ko.observable(notify_email);
    self.is_build_sync = ko.observable(build_sync);
}

function NewBuildModelView(parent) {
    var self = this;
    self.parent = parent
    self.build = new Build('', '', '', '', true, true);
    self.visible = function() {
        return self.parent.lastAction() === self.parent.showModeEnum.BUILD
    };

//    function to open a form for add new build.
    self.cleanUp = function() {
        self.parent.lastAction(self.parent.showModeEnum.BUILD);
        self.build.name("");
        self.build.git_path("");
        self.build.package_versions("");
        self.build.erl_versions("");
        self.build.package_versions("");
        self.build.erl_versions("");
        self.build.notify_email(true);
        self.build.build_sync(true);
        self.visible(true);
        self.hideAllElements();
        return true;
    };

//    function to construct version tuples
    self.constructVersions = function() {
        var result = [];
        versions = self.build.package_versions().split(',');
        erl_versions = self.build.erl_versions().split(',');
        if (versions.length == 0) {
            for(var e in erl_versions) {
                result.push({"erl_version": erl_versions[e]});
            }
        } else if (erl_versions.length == 0) {
            for(var v in versions) {
                result.push({"ref": versions[v]});
            }
        } else {
            for(var v in versions) {
                for(var e in erl_versions) {
                    result.push({"ref": versions[v], "erl_version": erl_versions[e]});
                }
            }
        }
        return result;
    };

    self.renderError = function(text) {
        self.build.error(text);
        document.getElementById("build_queued").style.display = "none";
        document.getElementById("build_success").style.display = "none";
        document.getElementById("build_error").style.display = "block";
        self.hideLoader();
    };
    self.renderSuccess = function() {
        if(self.build.is_build_sync()) {
            document.getElementById("build_queued").style.display = "none";
            document.getElementById("build_success").style.display = "block";
        } else {
            document.getElementById("build_queued").style.display = "block";
            document.getElementById("build_success").style.display = "none";
        }
        document.getElementById("build_error").style.display = "none";
        self.hideLoader();
    };

    self.remove_git_ending = function(url) {
        if(url.endsWith('.git')) {
            return url.slice(0, -4);
        }
        return url;
    };

//    function to process new build...
    self.addBuild = function() {
        self.hideAllElements();
         if((self.build.name().trim() + self.build.git_path().trim()).length > 0) {
            self.showLoader();
             $.post({
                      url: self.getController(),
                      data: JSON.stringify({
                                "full_name": self.build.name().trim(),
                                "clone_url": self.remove_git_ending(self.build.git_path().trim()),
                                "versions": self.constructVersions(),
                                "notify_email": self.build.is_notify_email()
                            }),
                      contentType: 'application/json',
                      dataType: 'json',
                      success:
                          function(reply) {
                            self.renderSuccess();
                          }
                    }
             ).fail(function(jqXHR) {
                        self.renderError(JSON.parse(jqXHR.responseText).response);
                    }
                    );
        } else {
            self.renderError("Input name or/and git path to add new build.");
        }
     }
    self.getController = function() {
        if (self.build.is_build_sync())
            return '/buildSync';
        else
            return '/buildAsync'
    };
    self.showLoader = function() {
        if(self.build.is_build_sync())
            document.getElementById("build_loader").style.display = "block";
    };
    self.hideLoader = function() {
        if(self.build.is_build_sync())
            document.getElementById("build_loader").style.display = "none";
    };
    self.hideAllElements = function() {
        document.getElementById("build_queued").style.display = "none";
        document.getElementById("build_success").style.display = "none";
        document.getElementById("build_error").style.display = "none";
        document.getElementById("build_loader").style.display = "none";
    }
}

