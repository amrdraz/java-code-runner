var observer = require("./node/observer");
var server = require("./node/server");

server.startServer();

setInterval(function () {
    // console.log("watching "+ server.didExit());
    if(server.didExit()) {
        server.restartServer();
    }
},1000);
