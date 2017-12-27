
function Build(name, git_path, package_versions, erl_versions) {
    var self = this;
    self.name = ko.observable(name);
    self.git_path = ko.observable(git_path);
    self.package_versions = ko.observable(package_versions);
    self.erl_versions = ko.observable(erl_versions);
    self.error = ko.observable("Error occurred");
}

function NewBuildModelView(parent) {
    var self = this;
    self.parent = parent
    self.build = new Build('', '', '', '');
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
        self.visible(true);
        document.getElementById("build_success").style.display = "none";
        document.getElementById("build_error").style.display = "none";
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
        document.getElementById("build_success").style.display = "none";
        document.getElementById("build_error").style.display = "block";
    };

//    function to process new build...
    self.addBuild = function() {
         if((self.build.name().trim() + self.build.git_path().trim()).length > 0) {
             $.post({
                      url: '/buildAsync',
                      data: JSON.stringify({
                                "full_name": self.build.name().trim(),
                                "clone_url": self.build.git_path().trim(),
                                "versions": self.constructVersions()
                            }),
                      contentType: 'application/json',
                      dataType: 'json',
                      success:
                          function(reply) {
                            document.getElementById("build_error").style.display = "none";
                            document.getElementById("build_success").style.display = "block";
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
}

