
function Build(name, git_path, package_versions, erl_versions) {
    var self = this;
    self.name = ko.observable(name);
    self.git_path = ko.observable(git_path);
    self.package_versions = ko.observable(package_versions);
    self.erl_versions = ko.observable(erl_versions);
}

function NewBuildModelView(parent) {
    var self = this;
    self.parent = parent
    self.build = new Build('', '', '', '');
    self.visible = function() {
        return self.parent.lastAction() === self.parent.showModeEnum.BUILD
    };

    var width = document.getElementById('url').offsetWidth;
    var height = document.getElementById('url').offsetHeight;
    $(function() {
          $('#package_versions').tagsInput({
          'defaultText':'add git tag',
          'width': width,
          'height':height
          });
          $('#erl_versions').tagsInput({
          'defaultText':'add erl vsn',
          'width': width,
          'height':height
          });
        });


//    function to open a form for add new build.
    self.cleanUp = function() {
        self.parent.lastAction(self.parent.showModeEnum.BUILD);
        self.build.name("");
        self.build.git_path("");
        self.build.package_versions("");
        self.build.erl_versions("");
        self.visible(true);
        return true;
    }
//    function to process new build...
    self.addBuild = function() {
         if((self.build.name().trim() + self.build.git_path().trim()).length > 0) {
             $.post({
                      url: '/buildAsync',
                      data: {
                                "full_name": self.build.name().trim(),
                                "clone_url": self.build.git_path().trim()
                            },
                      dataType: 'json',
                      success:
                          function(reply) {
                                 alert(reply);
                          }
                    }
             );
        } else {
            alert("Input name or/and git path to add new build.");
        }
     }
}

