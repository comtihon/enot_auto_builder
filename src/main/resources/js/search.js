function search() {
    var fullName = document.getElementById('package_search').value;
    $.ajax({
            type: "POST",
            url: "search",
            data: getName(fullName),
            success: function (result) {
                // do something.
            },
            error: function (result) {
                // do something.
            }
        });
}

function getName(var fullName) {
   if(fullName.includes("/")) {
       var splitted = fullName.split("/")
       var namespace = splitted[0];
       var name = splitted[1];
       return {
       name: name,
       namespace : namespace
       }
   } else {
       return {
       name : fullName
       }
   }
}
