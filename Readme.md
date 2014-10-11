This is a module for runinng Java from String inside a node server

to run tests

    npm install
    npm test

The java server can run independently

I use Java 1.8

to run the server

Compile JavaRunner

    javac JavaRunner.java

Compile Server

    javac -cp .:servlet-api-2.5.jar:jetty-all-7.0.2.v20100331.jar RunnerServlet.java

Run server

    java -cp .:servlet-api-2.5.jar:jetty-all-7.0.2.v20100331.jar RunnerServlet

The default port is `8080`

the server will return a statusCode 200 for GET request to `'/'`
the server will return a JSON object `{stout:String, sterr:String}` for POST request to `'/'`
the POST body should be {name:[nameOfClass], code:[ClassContent]}

example in nodejs, you can build a similar request using [postman](https://chrome.google.com/webstore/detail/postman-rest-client/fdmmgilgnpjigdojojpjoooidkmcomcm?hl=en)

    var post_data = querystring.stringify({
        'name': 'Main',
        'code': 'public class Main {public static void main (String [] args) { System.out.println("Hello World");}}'
    });
    // An object of options to indicate where to post to
    http.request({
        host: '127.0.0.1',
        port: 8080,
        path: '',
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Content-Length': post_data.length // don't need to fill this in postman
        }
    }, function (res) {...});

    post_req.write(post_data);
    post_req.end();
