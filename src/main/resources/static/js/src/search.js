
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
        document.getElementById("search_error").style.display = "none";
        return true;
    };
    self.searchError = ko.observable("Error occurred");
    self.addRowClass = function(success) {
                          if(success) {
                              return "";
                          } else {
                              return "table-danger";
                          }
                      };
    self.addDownloadClass = function(success) {
                              if(success) {
                                  return "fas fa-cloud-download-alt";
                              } else {
                                  return "far fa-file-alt";
                              }
                            };
    self.getTitleText = function(success) {
                            if(success) {
                                  return "Package was built. Click to download.";
                            } else {
                              return "Build failed. Click to see log.";
                            }
                        };
    self.simpleSearch = function() {
        self.packages([]);
        var userInput = self.searchFor().trim();
        var data;
        if (userInput.includes("/")) {
            var splitted = userInput.split("/");
            data = {name: splitted[1], namespace: splitted[0]};
        } else {
            data = {name: userInput};
        }

        if (userInput.length > 0) {
            self.parent.lastAction(self.parent.showModeEnum.SEARCH);
            document.getElementById("search_error").style.display = "none";

            $.get({
              url: "/search",
              data: data,
              dataType: 'json',
              success: function(reply) {
                  if (reply.result) {
                    ko.mapping.fromJS(reply.response, {}, self.packages);
                  } else {
                    self.searchError(reply.response);
                    document.getElementById("search_error").style.display = "block";
                  }
              }
            });
        } else {
            self.searchError("Input package name for the search.")
            document.getElementById("search_error").style.display = "block";
        }
    }
}
