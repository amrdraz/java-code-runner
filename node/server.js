// a set of functions that are useful for the project
var cp = require('child_process');
var net = require('net');
var log = require('util').log;
var http = require('http');
var fs = require('fs');
var _ = require('lodash');
var observer = exports.observer = require('./observer');
var queue = require('./queue');
var config = require('./config');

var defaultPort = config.defaultPort; // default port picked it at random

var tryPort = config.defaultPort;
var compiled, compiling;

var servletReady = false; // flag if server is ready to reseave post requests
var startingServer = false; // flag if server is ready to reseave post requests
var serverExit = false; // flag if server is ready to reseave post requests
var servletPort;
var servlet = null;


var didExit = exports.didExit = function() {
    return serverExit;
};
var isReady = exports.isReady = function() {
    return servletReady;
};

var isStarting = exports.isStarting = function() {
    return startingServer;
};

exports.getPort = function() {
    return servletPort;
};


// get an empty port for the java server
function findPort(cb) {
    var port = tryPort;
    tryPort += 1;

    var server = net.createServer();
    server.listen(port, function(err) {
        server.once('close', function() {
            cb(port);
        });
        server.close();
    });
    server.on('error', function(err) {
        log("port " + tryPort + " is occupied");
        findPort(cb);
    });
}


/**
 * Exposed method that builds project with ant
 * @param  {Function} cb callback after cmd execution
 * @return {[type]}      [description]
 */
var recompile = exports.recompile = function recompile(cb) {
    var stoutBuffer = '',
        sterrBuffer = '';
    if (!compiled) {
        compiling = true;
        // can't have any server running while compiling
        killAllJava(function() {
            var proc = cp.spawn('ant', {
                cwd: config.rootDir
            });
            proc.stdout.on('data', function(data) {
                stoutBuffer += data;
            });
            proc.stderr.on('data', function(data) {
                sterrBuffer += data;
            });
            proc.on('close', function(code) {
                compiling = false;
                compiled = true;
                observer.emit('server.compiled', compiled);
                cb(compiled);
            });
        });
    } else {
        log("already compiled project in this process");
        cb(false);
    }
};

function resetFlags() {
        servlet = global._servlet = servletPort = global._servletPort = null;
        servletReady = startingServer = false;
        tryPort = defaultPort;
    }
    /**
     * Exposed method for stoping server
     * @param {Function|boolean} callback with parameter indicating success of stop, if force stop without possibility of restart
     * @return {[type]} [description]
     */
var stopServer = exports.stopServer = function(kill) {
    if (_.isFunction(kill)) {
        cb = kill;
        kill = true;
    } else {
        cb = _.noop;
    }
    if ((isStarting() || isReady() || kill) && servlet) {
        if (servlet) servlet.kill();
        servlet.on('exit',function () {
            cb(true);
        });
        resetFlags();
        log("Stoped Server");
        observer.emit('server.stoped', kill);
        return;
    }
    log("No Process to stop");
    cb(false);
};

var killAllJava = exports.killAllJava = function(cb) {
    cp.exec('killall java', function() {
        log("killed all java");
        cb();
    });
};

var restartServer = exports.restartServer = function(cb) {
    log("Restarting server");
    serverExit = false;
    stopServer(function(isRunning) {
        log(isRunning + " couldn't stop ");
        startServlet(cb);
    });
};


/**
 * Starts the servlet on an empty port default is 3678
 */
function startServlet(cb) {
    startingServer = true;
    servletReady = false;
        debugger;
    findPort(function(port) {
        servletPort = global._servletPort = '' + port;

        servlet = global._servlet = cp.spawn('java', ['-cp', '.:../lib/servlet-api-2.5.jar:../lib/jetty-all-7.0.2.v20100331.jar', 'RunnerServlet', servletPort], {
            cwd: config.rootDir + '/bin'
        });
        servlet.stdout.on('data', function(data) {
            console.log('OUT:' + data);
        });
        servlet.stderr.on('data', function(data) {
            console.log("" + data);
            if (~data.toString().indexOf(servletPort)) {
                servletReady = true;
                startingServer = false;
                // queue.checkQueues();
                observer.emit("server.running", port);
                if (cb) cb(port);
            }
        });

        
        servlet.on('exit', function(code, signal) {
            resetFlags();
            if (code === null || signal === null) {
                serverExit = true;
            }
            log('servlet exist with code ' + code + 'signal ' + signal);
            observer.emit('server.exit',code);
        });
        // make sure to close server after node process ends
        process.on('exit', function() {
            stopServer(true);
        });
    });
}


/**
 * Check if a server server is runing on port 3678 if so no need to start a new server
 * @param  {number} port port to check against default to defaultPort
 */
var checkIfServletIsAlreadyRunning = exports.startServer = function(port, cb) {
    if (!port) {
        port = defaultPort;
        cb = _.noop;
    } else if (_.isFunction(port)) {
        cb = port;
        port = defaultPort;
    }

    log("checking if server is running on port " + port);
    http.get("http://localhost:" + port + "/", function(res) {
        log("got response");
        if (res.statusCode === 200) {
            log("seems like it's fine");
            servletPort = global._servletPort = port;
            servletReady = true;
            startingServer = false;
            // queue.checkQueues();
            observer.emit("server.running", port);
            if (cb) cb(port);
        } else {
            log("there is a server running for I got a different status code ");
            log(res);
        }
    }).on('error', function(e) {
        log("recived an error when checking");
        if (compiling) {
            log("server is compiling, waiting till it's done");
        } else {
            if (!isStarting()) {
                if (servletPort) {
                    log("it is not but port is defined as " + servletPort);
                    log("We'll try restarting");
                    return restartServer();
                }
                log("there doesn't seem to be a server that's starting, staring our own");
                startServlet(cb);
            } else {
                log("server has not started yet waiting till it does...");
            }
        }
    });
};

observer.on("server.restart", function() {
    restartServer();
});

observer.on("server.checkup", function(port) {
    checkIfServletIsAlreadyRunning(port);
});
