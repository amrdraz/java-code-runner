This is a module for runinng Java from String inside a node server built for another project called kodr

to run tests

    npm install
    npm test

The java server can run independently

I use Java 1.8

to run the server

Compile using 'ant'

Run server in bin

    java -cp .:../lib/servlet-api-2.5.jar:../lib/jetty-all-7.0.2.v20100331.jar RunnerServlet

The default port is `3678`

The server will return a statusCode 200 for GET request to `'/'`.

The server will return a JSON object `{stout:String, sterr:String}` for POST request to `'/'`.

The POST body should be {name:[nameOfClass], code:[classContent]}
you can also post an optional __input__ parameter for inpout stream and a __timeLimit__ long for setting a timelimit on running the program

example in nodejs, you can build a similar request using [postman](https://chrome.google.com/webstore/detail/postman-rest-client/fdmmgilgnpjigdojojpjoooidkmcomcm?hl=en)

    var post_data = querystring.stringify({
        'name': 'Main',
        'code': 'public class Main {public static void main (String [] args) { System.out.println("Hello World");}}',
        input:"input stream"
    });
    // An object of options to indicate where to post to
    http.request({
        host: '127.0.0.1',
        port: 3678,
        path: '',
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Content-Length': post_data.length // don't need to fill this in postman
        }
    }, function (res) {...});

    post_req.write(post_data);
    post_req.end();

java dependencies are in the .java-dependency file

This code is designed for UNIX system it may be incompatible with Windows, eg. I use the ':' to speerate classpath


###node wraper
The module's index.js file exposes 6 methods
- `run`
- `test`
- `runClass`
- `stopServer`
- `runServer`
- `recompile` which prgramatically compiles only once per process

see tests for how they work.

###Things left to do

Look into runing the java server as a docker container for security
