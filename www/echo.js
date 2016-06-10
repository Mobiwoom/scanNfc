var exec = require('cordova/exec');

module.exports = {

    echo: function(param) {

        /* Aligning with w3c spec */
        //exec(null, null, "Echo", "echo", [param]);
        cordova.exec(function() {console.log("Plugin sucess arg0= " + arguments[0]);}, function() {console.log("Plugin error "+arguments[0]);}, "Echo", "echo", [param]);             

        return true;
    },
    setNFC: function(ff,param) {

        /* Aligning with w3c spec */
        //exec(null, null, "Echo", "echo", [param]);
        
        console.log("Plugin sucess ff= " ); 
        //cordova.exec(function() {console.log("Plugin sucess arg0= " + arguments[0]); }, function() {console.log("Plugin error "+arguments[0]);}, "Echo", "setNFC", [param]);   
        cordova.exec(ff, function() {console.log("Plugin error "+arguments[0]);}, "Echo", "setNFC", [param]);             

        return true;
    }
};
      