(function($) {
//class to represent erlang package row in searсh package grid
function ErlPackage(build_id, namespace, name, success, build_date, path, version, erl_version) {
    var self = this;
    self.build_id = ko.observable(build_id);
    self.namespace = ko.observable(namespace);
    self.name = ko.observable(name);
    self.success = ko.observable(success);
    self.build_date = ko.observable(build_date);
    self.path = ko.observable(path);
    self.version = ko.observable(version);
    self.erl_version = ko.observable(erl_version);
}

function checkTool(tool) {
    if (tool.hasOwnProperty('version')) {
        return tool.version;
    } else if (tool.status == 'UP') {
        return 'UP';
    } else {
        return tool.error;
    }
}

function fillTools(reply, self) {
    self.coon(checkTool(reply.coon));
    self.kerl(checkTool(reply.kerl));
    self.db(checkTool(reply.db));
}

function PackageViewModel() {
    var self = this;
    self.searchFor = ko.observable("");
    console.log(self.searchFor);
    self.searchDone = ko.observable(false);
    console.log(self.searchDone);

    self.packages = ko.observableArray();
    self.err_msg = function(msg) {
        alert(msg);
    }
    self.toggleTableVisible = function() {
        if (!self.searchDone()) {
            self.searchDone(!self.searchDone());
        }
    }
    self.cleanUp = function() {
        self.searchFor("");
        self.searchDone(false);
        return true;
    }
//FIXME move it out into statistic MV
    self.version = ko.observable('not available');
    self.coon = ko.observable('not available');
    self.kerl = ko.observable('not available');
    self.db = ko.observable('not available');
        $.get({
                url: '/info', //TODO ability to get on 8081 port
                dataType: 'json',
                success:
                    function(reply) {
                                        self.version(reply.build.version);
                                    }
              });
        $.get({
                url: '/health',
                dataType: 'json',
                success: function(reply) {fillTools(reply, self);}
              }).fail(function(jqXHR) {
                                        reply = JSON.parse(jqXHR.responseText);
                                        fillTools(reply, self);
                                     });
    
    self.simpleSearch = function() {
    var userInput = self.searchFor().trim();

        if (userInput.length>0) {

        self.toggleTableVisible();
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
                self.err_msg(reply.response);
              }
          }
        });
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
                self.err_msg(xhr);
                //Do Something better to handle error
              }
            });
    }
}


// Activates knockout.js
ko.applyBindings(new PackageViewModel());

})(jQuery)
