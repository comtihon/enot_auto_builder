(function($) {

var bossViewModel;

function Boss() {
    var self = this;
    self.searchVM;
    self.infoVM;
    self.buildVM;
}

function BossViewModel() {
    var self = this;
    self.boss = new Boss();
    self.showModeEnum = Object.freeze({INFO: 0, SEARCH: 1, BUILD: 2});
    self.lastAction = ko.observable(self.showModeEnum.INFO);
}


$(document).ready(function() {
    var applyBoss = false;
    if ($.isEmptyObject(bossViewModel)) {
        bossViewModel = new BossViewModel();
        applyBoss = true;
    }

    if ($.isEmptyObject(bossViewModel.boss.infoVM)) {
        bossViewModel.boss.infoVM = new InfoViewModel(bossViewModel);
        ko.applyBindings(bossViewModel.boss.infoVM, document.getElementById("infoContainer"));
        bossViewModel.boss.infoVM.loadInfo();
    }

    if ($.isEmptyObject(bossViewModel.boss.searchVM)) {
        bossViewModel.boss.searchVM = new PackageViewModel(bossViewModel);
        ko.applyBindings(bossViewModel.boss.searchVM, document.getElementById("searchContainer"));
        ko.applyBindings(bossViewModel.boss.searchVM, document.getElementById("search_error"));
    }
    if ($.isEmptyObject(bossViewModel.boss.buildVM)) {
        bossViewModel.boss.buildVM = new NewBuildModelView(bossViewModel);
        ko.applyBindings(bossViewModel.boss.buildVM, document.getElementById("newBuildContainer"));
    }

    if(applyBoss) {
        ko.applyBindings(bossViewModel, document.getElementById("navbar"));
        applyBoss = false;
    }
    document.getElementById("main").style.visibility = "visible";
});




})(jQuery)