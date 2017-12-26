
function ServerInfo(serverVersion, coonVersion, kerlVersion) {
    var self = this;
    self.serverVersion = ko.observable(serverVersion);
    self.coonVersion = ko.observable(coonVersion);
    self.kerlVersion = ko.observable(kerlVersion);
}

function InfoViewModel(parent) {
    var self = this;
    self.parent = parent;
    self.serverInfo = new ServerInfo('not available','not available','not available');

    self.visible = function() {
        return self.parent.lastAction() === self.parent.showModeEnum.INFO
    };

    self.cleanUp = function() {
        self.serverInfo.serverVersion('not available');
        self.serverInfo.coonVersion('not available');
        self.serverInfo.kerlVersion('not available');
        self.parent.lastAction(self.parent.showModeEnum.INFO);
        return true;
    }

    self.checkTool = function(tool) {
        if (tool.hasOwnProperty('version')) {
            return tool.version;
        } else {
            return tool.error;
        }
    }

    self.fillTools = function(reply) {
        self.serverInfo.coonVersion(self.checkTool(reply.coon));
        self.serverInfo.kerlVersion(self.checkTool(reply.kerl));
    }

    self.loadInfo = function() {
        self.cleanUp();

        $.get({
                url: '/info', //TODO ability to get on 8081 port
                dataType: 'json',
                success:
                    function(reply) {
                        self.serverInfo.serverVersion(reply.build.version);
                    }
              });

        $.get({
                url: '/health',
                dataType: 'json',
                success: function(reply) {self.fillTools(reply);}
              }
              ).fail(function(jqXHR) {
                        reply = JSON.parse(jqXHR.responseText);
                        self.fillTools(reply);
                      }
                    );
    }
}
