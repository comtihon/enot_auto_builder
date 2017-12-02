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

function PackageViewModel() {
    var self = this;
    self.searchFor = ko.observable("");
    console.log(self.searchFor);
    self.searchDone = ko.observable(false);
    console.log(self.searchDone);

    self.packages = ko.observableArray([]);
    self.err_msg = function(msg) {
        alert(msg);
    }
    self.toggleTableVisible = function() {
        if (!self.searchDone()) {
            self.searchDone(!self.searchDone());
        }
    }
    
    self.simpleSearch = function() {
        self.toggleTableVisible();
        jQuery.get({
          url: "/search",
          data: {
            name: self.searchFor().trim()
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
