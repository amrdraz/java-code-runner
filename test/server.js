/*globals before,after,beforeEach,afterEach,describe,it */
var server = require('../node/server.js');
var expect = require('chai').expect;
var Promise = require('bluebird');
var _ = require('lodash');
var request = require('supertest');

require('./compile');


describe('Java Server', function() {

    var url;
    var port;

    it('should start server', function (done) {
        server.startServer(3678, function(p) {
            port = p;
            url = 'http://localhost:' + p;
            console.log('sending request to ' + url);
            expect(p).to.equal(3678);
            done();
        });
    });

    it('should respond to GET request with 200', function(done) {
        request(url)
            .get("/")
            .expect(200)
            .end(done);
    });

    it('should run simple java HelloWorld program', function(done) {
        request(url)
            .post("/")
            .set('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')
            .send({
                name: 'Main',
                code: 'public class Main {public static void main (String [] args) { System.out.print("Hello World");}}'
            })
            .end(function(err, res) {
                if (err) return done(err);
                expect(res.body.stout).to.equal('Hello World');
                done();
            });
    });

    it('should run java programs with input stream given', function(done) {
        var input = "InputStream";
        request(url)
            .post("/")
            .set('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')
            .send({
                name: 'Main',
                code: 'import java.util.Scanner; public class Main {public static void main (String [] args) {Scanner sc = new Scanner(System.in); sc.nextLine();System.out.print(sc.nextLine());}}',
                input: "something to skip\n"+input
            })
            .end(function(err, res) {
                if (err) return done(err);
                expect(res.body.stout).to.equal(input);
                done();
            });
    });

    it('should run multiple simple java prgrams concurently', function(done) {
        this.timeout(20000);
        Promise.map(new Array(10), function(x, i) {
            return new Promise(function(resolve, reject) {
                // setTimeout(function() {
                request(url)
                    .post("/")
                    .set('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')
                    .send({
                        name: 'Main' + i,
                        code: 'public class Main' + i + ' {public static void main (String [] args) { System.out.print("Hello World' + i + '");}}'
                    })
                    .end(function(err, res) {
                        if (err) return reject(err);
                        expect(res.body.stout).to.equal('Hello World' + i);
                        resolve(true);
                    });
                // }, i * 40); //simulate trafic
            });
        }).then(function() {
            done();
        }).catch(done);
    });
    
    it('should run multiple java while providing input concurently', function(done) {
        this.timeout(20000);
        Promise.map(new Array(10), function(x, i) {
            return new Promise(function(resolve, reject) {
                // setTimeout(function() {
                var input = "InputStream"+i;
                request(url)
                    .post("/")
                    .set('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')
                    .send({
                        name: 'Main' + i,
                        code: 'import java.util.Scanner; public class Main'+i+' {public static void main (String [] args) {Scanner sc = new Scanner(System.in); System.out.print(sc.nextLine());}}',
                        input: input
                    })
                    .end(function(err, res) {
                        if (err) return reject(err);
                        expect(res.body.stout).to.equal(input);
                        resolve(true);
                    });
                // }, i * 40); //simulate trafic
            });
        }).then(function() {
            done();
        }).catch(done);
    });

    it('should throw runtime error', function(done) {
        var input = "InputStream";
        request(url)
            .post("/")
            .set('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')
            .send({
                name: 'Main',
                code: 'public class Main {public static void main(String [] args) throws Exception  {int a = 0; int b = 20; int c = b/a;}}',
            })
            .end(function(err, res) {
                if (err) return done(err);
                expect(res.body.sterr).to.exist;
                done();
            });
    });

    it('should throw error if no input stream given', function(done) {
        var input = "InputStream";
        request(url)
            .post("/")
            .set('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')
            .send({
                name: 'Main',
                code: 'import java.util.Scanner; public class Main {public static void main (String [] args) {Scanner sc = new Scanner(System.in); sc.nextLine();System.out.print(sc.nextLine());}}',
            })
            .end(function(err, res) {
                if (err) return done(err);
                expect(res.body.sterr).to.exist;
                done();
            });
    });

    it('should have access to Test.java', function(done) {
        request(url)
            .post("/")
            .set('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')
            .send({
                name: 'MainTest',
                code: 'public class MainTest {public static void main (String [] args) { System.out.print("Hello World"); Test t = new Test(); t.pass(); System.out.print(t.getTestOut().toString()); }}'
            })
            .end(function(err, res) {
                if (err) return done(err);
                expect(res.body.stout).to.contain('<[TestOut]>');
                done();
            });
    });

    it('should stop', function (done) {
        server.stopServer(function (stop) {
            expect(stop).to.be.true;
            done();
        });
    });
    it('should start again', function (done) {
        this.timeout(5000);
        server.startServer(function(p) {
            expect(p).to.equal(3678);
            done();
        });
    });

    it('should not restart if already running', function (done) {
        this.timeout(5000);
        server.startServer(function(p) {
            expect(p).to.equal(3678);
            done();
        });
    });

    it('should check server when respond event is sent', function (done) {
        server.observer.once("server.running", function (port) {
            done();
        });
        server.observer.emit("server.checkup");
    });


    it('should emit stop event when server stops', function (done) {
        server.observer.once("server.stoped", function (killed) {
            done();
        });
        server.stopServer(true);
    });
});