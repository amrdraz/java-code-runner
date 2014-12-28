var cp = require('child_process');
var log = require('util').log;
var net = require('net');
var path = require('path');
var querystring = require('querystring');
var http = require('http');
var fs = require('fs');
var _ = require('lodash');

var running = 0; // workaround to reduce event loop blocking when running in terminal
var timeLimit = 5000;
var compiled = false;
var compiling = false;
var servletReady = false; // flag if server is ready to reseave post requests
var startingServer = false; // flag if server is ready to reseave post requests
var defaultPort = 3678; // default port picked it at random
var tryPort = 3678; // default port picked it at random

var servletPort;
var servlet = null;

// get an empty port for the java server
function getPort(cb) {
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
        log("port "+tryPort+" is occupied");
        getPort(cb);
    });
}

/**
 * Exposed method that builds project with ant
 * @param  {Function} cb callback after cmd execution
 * @return {[type]}      [description]
 */
var recompile = exports.recompile = function recompile(cb) {
    if(!compiled) {
        compiling = true;
        cp.exec('ant', {
            cwd: __dirname
        }, function (err, stout, sterr) {
            compiled = true;
            compiling = false;
            cb(compiled);
        });
    } else {
        log("already compiled project in this process");
        cb(false);
    }
};

/**
 * Exposed method for stoping server
 * @param {Function} callback fwith parameter indicating success to stop
 * @return {[type]} [description]
 */
var stopServer = exports.stopServer = function (kill) {
    var cb = _.isFunction(kill)?kill:_.noop;
    if ((startingServer || servletReady || kill) && servlet) {
        servlet.kill();
        servlet = global._servlet = servletPort = global._servletPort = null;
        servletReady = startingServer = false;
        tryPort = defaultPort;
        log("Stoped Server");
        return cb(true);
    }
    log("No Process to stop");
    cb(false);
};
/**
 * Starts the servlet on an empty port default is 3678
 */
function startServlet(cb) {
    getPort(function(port) {
        servletPort = global._servletPort = '' + port;

        servlet = global._servlet = cp.spawn('java', ['-cp', '.:../lib/servlet-api-2.5.jar:../lib/jetty-all-7.0.2.v20100331.jar', 'RunnerServlet', servletPort], {
            cwd: __dirname + '/bin',
            env:process.env
        });

        servlet.stdout.on('data', function(data) {
            log('OUT:' + data);
        });
        servlet.stderr.on('data', function(data) {
            console.log("" + data);
            if (~data.toString().indexOf(servletPort)) {
                log('calling cb with data '+ data)
                servletReady = true;
                startingServer = false;
                cb && cb(port);
            }
        });
        servlet.on('exit', function(code) {
            log('servlet exist with code ' + code);
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
var checkIfServletIsAlreadyRunning = exports.runServer = function(port, cb) {
    if (!port) {
        port = defaultPort;
        cb = _.noop;
    } else if (_.isFunction(port)) {
        cb = port;
        port = defaultPort;
    }
    log("checking if server is running");
    http.get("http://localhost:" + port + "/", function(res) {
        if (res.statusCode === 200) {
            servletPort = global._servletPort = port;
            servletReady = true;
            if (cb) {
                cb(port);
            }
            startingServer = false;
        } else {
            log(res);
        }
    }).on('error', function(e) {
        if (!startingServer) {
            if(servletPort) {
                log("it is not but port is defined");
                return cb(+servletPort);
            }
            startingServer = true;
            log('No server running starting our own');
            startServlet(cb);
        } else {
            log('a server is starting waiting till it does');
            _.delay(function() {
                checkIfServletIsAlreadyRunning(cb);
            }, 1000);
        }
    });
};

/**
 * Spawn a java process and return callback
 * @param  {Array}   args  arguments to pass to java proc
 * @param  {Function} cb   callback to be called with err, stout, sterr
 */
function runProc(args, cb) {
    var stoutBuffer = '',
        sterrBuffer = '';
    var proc = cp.spawn('java', args, {
        cwd: __dirname + '/bin'
    });
    proc.stdout.on('data', function(data) {
        stoutBuffer += data;
    });
    proc.stderr.on('data', function(data) {
        sterrBuffer += data;
    });
    proc.on('close', function(code) {
        if (code === null) {
            cb(code);
        } else {
            cb(null, stoutBuffer, sterrBuffer);
        }
        running--;
    });
}

/**
 * Run java program in TerminalRunner
 * @param  {Object} options an object containing all prgram needed configuration
 *                    - {String}   name    name of class
 *                    - {String}   program source code of JavaClass with public class [name]
 *                    - {String}   input   The program's input stream if needed
 * @param  {Function} cb      callback when complete
 */
function runCMD(options, cb) {
    var args = ["-cp", ".", "-XX:+TieredCompilation", "-XX:TieredStopAtLevel=1", "TerminalRunner"];
    args.push(options.name);
    args.push(options.program);
    args.push(options.timeLimit||5000);
    args.push(options.input||'');

    // delaying request if more then one hit so that the event loop has time to compute
    if (running < 1) {
        running++;
        runProc(args, cb);
    } else {
        running++;
        // log(running);
        _.delay(function() {
            runClass(options.program, options, cb);
        }, running * 1000);
    }
}


/**
 * Run java program in server, which is singnificantly faster then CMD
 * @param  {Object} options an object containing all prgram needed configuration
 *                    - {String}   name    name of class
 *                    - {String}   program source code of JavaClass with public class [name]
 *                    - {String}   input   The program's input stream if needed
 * @param  {Function} cb      callback when complete
 */
function runInServlet(options, cb) {
    var timer;
    // program to run
    var post_data = querystring.stringify({
        'name': options.name,
        'code': options.program,
        'input': options.input||'',
        'timeLimit':options.timeLimit||5000
    });
    // An object of options to indicate where to post to
    var post_options = {
        host: '127.0.0.1',
        port: servletPort,
        path: '',
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Content-Length': post_data.length
        }
    };

    var post_req = http.request(post_options, function(res) {
        res.setEncoding('utf8');

        var responseString = '';

        res.on('data', function(data) {
            // clearTimeout(timer);
            data = JSON.parse(data);
            cb(null, data.stout, data.sterr);
        });

        // res.on('end', function() {
        //     log('::-----end-----::');
        //     var data = JSON.parse(responseString);
        //     log(responseString);
        //     log('::-----end-----::');
        //     cb(null, data.stout, data.sterr);
        // });
    });

    post_req.on('error', function(e) {
        cb(e);
    });
    // timer = setTimeout(function () {
    //     cb(new Error("TimeoutException: Your program ran for more than "+timeLimit));
    // },timeLimit);
    post_req.write(post_data);
    post_req.end();
}


/**
 * run java inside main method using the java built in dynamic compiler
 * this method will run using TerminalRunenr until Server is runing
 * @param  {String}   code    Code to run inside main
 * @param  {Object}   options additional configuration options
 *                            name  name of class (assumed to be correct)
 *                            runInCMD  force run in CMD
 * @param  {Function} cb      return callback
 */
var runClass = exports.runClass = function(code, options, cb) {
     if (_.isFunction(options)) {
        cb = options;
        options = {};
    } else {
        options = options || {};
    }
    options.name = options.name || code.match(/public\s+class\s+(\w+)/).pop();
    options.program = code;
    // log("attempt to run code");
    // if servlet is not ready run code from TerminalRunner
    if (servletReady && !options.runInCMD) {
        // log("ran code in server with port "+servletPort);
        runInServlet(options, cb);
    } else {
        if (options.runInCMD) {
            runCMD(options, cb);
        } else {
        checkIfServletIsAlreadyRunning();
            log("warning delaying execution while server is restarting");
             _.delay(function() {
                runClass(code, options, cb);
            }, 1000);
        }
    }
};

/**
 * run java inside main method using the java built in dynamic compiler
 * this method will run using TerminalRunenr until Server is runing
 * @param  {String}   code    Code to run inside main
 * @param  {Object}   options additional configuration options
 *                            di classes to import
 *                            preCode  code to run before given code
 *                            postCode code to run after given code
 * @param  {Function} cb      return callback
 */
var run = exports.run = function(code, options, cb) {

    if (typeof options === 'function') {
        cb = options;
        options = {};
    } else {
        options = options || {};
    }

    var preClass = '';
    // <comma-separated-default-imports>        (default: none)
    if (options.di) {
        preClass += _.reduce(options.di.split(','), function(s, i) {
            return s + 'import ' + i + ';';
        }, '');
    }
    options.inputs = options.inputs || [];

    var inp = options.inputs.length>0?options.inputs.join(";")+';':'';
    var pre = (options.preCode || '').replace(/\/\/.*$/gm, '').replace(/\r?\n|\r/g, ' ');
    var name = options.name = classCase(options.name || "Main") + (options.debug_number || '');

    // code to run
    code = inp + '\n' + code + '\n';

    var program = "";
    program += preClass;
    program += "public class "+name+" {";
    program += "  public static void main(String args[]) throws Exception {";
    program += "    " + code;
    // program += '  } catch (Exception e) {System.err.print("Exception line "+(e.getStackTrace()[0].getLineNumber())+" "+e);}}' +
    program += "  }";
    program += "}";

    runClass(program, options, cb);
};

/**
 * Test Java code using somple test framework
 * @param  {String}   code    [description]
 * @param  {String}   test    [description]
 * @param  {Object}   options [description]
 * @param  {Function} cb      [description]
 */
var test = exports.test = function(code, test, options, cb) {
    if (_.isEmpty(code)) cb(new Error('code can not be undefined'));
    if (!test) cb(new Error('test can not be undefined'));
    if(options.timeLimit && options.timeLimit<0) cb(new Error("TimeLimit can not be negative")); 
    if (!options.exp) cb(new Error('challange must have exp'));
    var hash = _.random(0, 200000000);
    options.runInCMD = options.runInCMD || !servletReady;
    options.inputs = options.inputs || [];

    var pre = ''+
        'final ByteArrayOutputStream $userOut = new ByteArrayOutputStream();\n' +
        'final ByteArrayOutputStream $userErr = new ByteArrayOutputStream();\n';
    var post = '';
    //capture sys streams and set uniq test hash
    // make sure to acomidate for both threaded and none
    if(options.runInCMD) {
         pre = pre +
            'PrintStream _out = System.out;\n'+
            'System.setOut(new PrintStream($userOut));\n' +
            'Test $test = new Test("' + hash + '", ' + JSON.stringify(code) + ');\n';
        post = '\n' +
            'System.setOut(_out);' + '\n' +
            'System.out.print($test.getTestOut().toString());\n';

    } else {
        pre = pre +
            'PrintStream _out = ((ThreadPrintStream)System.out).getThreadOut();'+
            '((ThreadPrintStream)System.out).setThreadOut(new PrintStream($userOut));\n' +
            'Test $test = new Test("' + hash + '", ' + JSON.stringify(code) + ');\n' ;
        post = '\n' +
            '((ThreadPrintStream)System.out).setThreadOut(_out);\n' +
            'System.out.print($test.getTestOut().toString());\n';
    }
    var name = options.name = classCase(options.name || "Main") + (options.debug_number || '');

    var program = "";
    program += "import java.io.ByteArrayOutputStream;import java.io.PrintStream;";
    program += "public class "+name+" {";
    program += "  public static void $main("+ options.inputs.join(",")+") throws Exception {\n";
    program += "    "+code;
    program += "  \n}";
    program += "  public static void main(String args[]) throws Exception {";
    program += "    " + pre;
    // program += "    try {";
    program += "    " + test;
    // program += "    } catch (Exception e) {";
    // program += '      _err.println(e);';
    // program += '      System.err.println("RuntimeError: "+e.getCause()+"\\n\\tat "+e.getCause().getStackTrace()[0].toString());';
    // program += "    } finally {";
    program += "      " + post;
    // program += '      _out.println("an error occured");';
    // program += "    }";
    program += "  }";
    program += "}";

    runClass(program, options, function(err, stout, sterr) {
        if (err && !sterr) return cb(err);
        sterr && log(sterr);

        var report = {
            passed: false,
            score: 0,
            passes: [],
            failures: [],
            tests: []
        };
        var reg = new RegExp("<\\[" + hash + "\\]>((?!<\\["+hash+"\\]>)[\\s\\S])+<\\[" + hash + "\\]>", "g");
        var tests = stout.match(reg) || [];
        if (!_.isEmpty(tests)) {
            tests = ('[' + tests.join(',') + ']');
            tests = tests.replace(new RegExp("<\\[" + hash + "\\]>", 'g'), '');
            tests = report.tests = JSON.parse(tests);
            report.passes = _.filter(tests, 'pass');
            report.failures = _.reject(tests, 'pass');
            report.score = _.reduce(tests, function(sum, t) {
                return sum + t.score;
            }, 0);
            report.score = Math.max(0, Math.min(report.score, options.exp));
            report.passed = report.passes.length === tests.length;
        }
        cb(null, report, stout, sterr);
    });
};


/**
 * Turn a sting to Java class style camel Case striping - and space chracters
 * @param  {[type]} input [description]
 * @return {[type]}       [description]
 */
function classCase(input) {
    return input.toUpperCase().replace(/[\-\s](.)/g, function(match, group1) {
        return group1.toUpperCase();
    });
}
