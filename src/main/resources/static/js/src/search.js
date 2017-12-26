
//class to represent erlang package
function ErlPackage(build_id, namespace, name, success, build_date, path, version, erl_version, git_path) {
    var self = this;
    self.build_id = ko.observable(build_id);
    self.namespace = ko.observable(namespace);
    self.name = ko.observable(name);
    self.success = ko.observable(success);
    self.build_date = ko.observable(build_date);
    self.path = ko.observable(path);
    self.version = ko.observable(version);
    self.erl_version = ko.observable(erl_version);
    self.git_path = ko.observable(git_path);

}

function PackageViewModel(parent) {
    var self = this;
    self.parent = parent;
    self.searchFor = ko.observable("");
    self.packages = ko.observableArray();
    self.visible = function() {
        return self.parent.lastAction() === self.parent.showModeEnum.SEARCH
    };
    self.cleanUp = function() {
        self.searchFor("");
//        self.visible(false);
        return true;
    }


    self.simpleSearch = function() {
    var userInput = self.searchFor().trim();

        if (userInput.length > 0) {
            self.parent.lastAction(self.parent.showModeEnum.SEARCH);

            $.get({
              url: "/search",
              data: {
                name: userInput
              },
              dataType: 'json',
              success: function(reply) {
                  if (reply.result) {
                    ko.mapping.fromJS(reply.response, {}, self.packages);
                  } else {
                    alert(reply.response);
                  }
              }
            });
        } else {
            alert("Input package name for the search.")
        }
    }
    self.complexSearch = function(namespace, name, version, erl_version) {
//            FIXME code duplication, extract base method
            jQuery.get({
              url: "/packages",
              data: {
                name: name.trim(),
                namespace: namespace.trim(),
                version: version.trim(),
                erl_version: erl_version.trim()
              },
              dataType: 'json',
              success: function(data) {
                  if (!data[0].result) {
                    ko.mapping.fromJS(data[0].response, self.packages);
                  } else {
                    self.err_msg(data[0].response);
                  }
              },
              error: function(xhr) {
                alert(xhr);
                //Do Something better to handle error
              }
            });
    }
}
