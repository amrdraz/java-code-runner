/*globals before,after,beforeEach,afterEach,describe,it */
var runner = require('../index.js');
var expect = require('chai').expect;
var Promise = require('bluebird');
var _ = require('lodash');

require('./compile');


describe('Java runner', function() {

    before(function (done) {
        runner.server.startServer(function () {
            done();
        });
    });

    after(function (done) {
        runner.server.stopServer(function () {
            done();
        });
    });
    
    describe('index#runInClass', function () {
        it('should run a java class', function (done) {
            runner.runClass('public class MainClass { public static void print() {System.out.println("Test");} public static void main(String[] args) {print();}}', function(err, stout, sterr) {
                console.log(err,stout, sterr);
                if (err) {
                    return done(err);
                }
                expect(stout).to.equal("Test\n");
                done();
            });
        });
    });

    describe('index#run', function() {
        it('should run java', function(done) {
            runner.run('System.out.print("Hello");', function(err, stout, sterr) {
                // stout && console.log(stout);
                // sterr && console.error(sterr);
                if (err) return done(err);
                expect(stout).to.equal('Hello');
                done();
            });
        });

        it('should output sterr for compile errors', function(done) {
            runner.run('System.out.print("Hello")', function(err, stout, sterr) {
                // sterr && console.error(sterr);
                expect(sterr).length.gt(0);
                done();
            });
        });

        it('should output sterr for compile errors', function(done) {
            runner.run('system.out.print("Hello");', function(err, stout, sterr) {
                // sterr && console.error(sterr);
                expect(sterr).length.gt(0);
                done();
            });
        });

        it('should output sterr for runtime errors', function(done) {
            runner.run('int a = 10/0;', function(err, stout, sterr) {
                // sterr && console.error(sterr);
                expect(sterr).to.exist;
                done();
            });
        });

        it('should send input sterr for runtime errors', function(done) {
            var input = "Hello InputStream";
            runner.run('Scanner sc = new Scanner(System.in);System.out.print(sc.nextLine());', {di:'java.util.Scanner',input:input}, function(err, stout, sterr) {
                if (err) return done(err);
                // stout && console.log(stout);
                // sterr && console.error(sterr);
                expect(stout).to.equal(input);
                done();
            });
        });

        it('should run multiple simple java prgrams', function(done) {
            this.timeout(20000);
            Promise.map(new Array(10), function(x, i) {
                return new Promise(function(resolve, reject) {
                    // setTimeout(function() {
                        // var start = new Date().getTime();
                        runner.run('System.out.print("Hello");System.out.print("World");', {
                            debug_number: i
                        }, function(err, stout, sterr) {
                            if (err) {
                                // console.error(err + '\n==========================\n');
                                reject(err);
                            }
                            // sterr && console.error(sterr);
                            expect(stout).to.equal('HelloWorld');
                            // var end = new Date().getTime();
                            // var time = end - start;
                            // console.log('ran in ' + time + 'ms');
                            resolve();
                        });
                    // }, i * 40); //simulate trafic
                });
            }).then(function() {
                done();
            }).catch(done);
        });

        it('shoudl run source code I submit but will fail because it will run last Main.java', function(done) {
            runner.run('System.out.print("Hello there ");System.out.print("World");', function(err, stout, sterr) {
                if (err) {
                    // console.error(err + '\n==========================\n');
                    reject(err);
                }
                // stout && console.log(stout);
                // sterr && console.error(sterr);
                expect(stout).to.equal("Hello there World");
                done();
            });
        });

        it('shoudl Run correctly because I changed class name', function(done) {
            runner.run('System.out.print("Hello there ");System.out.print("World");', {
                name: 'Main2'
            }, function(err, stout, sterr) {
                if (err) {
                    // console.error(err + '\n==========================\n');
                    reject(err);
                }
                // stout && console.log(stout);
                // sterr && console.error(sterr);
                expect(stout).to.equal("Hello there World");
                done();
            });
        });

        it('shoudl now not run correctly because I am calling a different program with that name', function(done) {
            runner.run('System.out.print("Hello there");', {
                name: 'Main2'
            }, function(err, stout, sterr) {
                if (err) {
                    // console.error(err + '\n==========================\n');
                    reject(err);
                }
                // stout && console.log(stout);
                // sterr && console.error(sterr);
                expect(stout).to.equal("Hello there");
                done();
            });
        });
        it('should run java in bash', function(done) {
            runner.run('System.out.print("Hello");', {runInCMD:true},function(err, stout, sterr) {
                // stout && console.log(stout);
                // sterr && console.error(sterr);
                if (err) return done(err);
                expect(stout).to.equal('Hello');
                done();
            });
        });

    });

    describe('index#test', function() {
        var code = 'char c =\'a\'; \n int a = 40, b = 20; System.out.print("a - b = " + (a - b));';
        var test = '$main();\n$test.expect($userOut.toString(),"a - b = 20");';

        it('should run code against test', function(done) {
            runner.test(code, test, {
                name: 'TestMain',
                exp: 1
            }, function(err, report, stout, sterr) {
                if (err) {
                    return done(err);
                }
                expect(report.passed).to.be.true;
                done();
            });
        });

         it('should run code against test in bash', function(done) {
            runner.test(code, test, {
                name: 'TestMain',
                exp: 1,
                runInCMD:true
            }, function(err, report, stout, sterr) {
                if (err) {
                    return done(err);
                }
                // console.log(report, stout, sterr);
                expect(report.passed).to.be.true;
                done();
            });
        });

        it('should run multiple tests concurently', function(done) {
            this.timeout(20000);
            Promise.map(new Array(10), function(x, i) {
                return new Promise(function(resolve, reject) {
                    // setTimeout(function() {
                    runner.test(code, test, {
                        name: 'TestMain',
                        exp: 1,
                        debug_number: i
                    }, function(err, report, stout, sterr) {
                        // console.log('out',stout);
                        // console.log('err',sterr);
                        // console.log('report',report);
                        if (err) {
                            return reject(err);
                        }
                        expect(report.passed).to.be.true;
                        resolve();
                    });
                    // }, i * 40); //simulate trafic
                });
            }).then(function() {
                done();
            }).catch(done);
        });

        it('should run $main with inputs', function(done) {
            runner.test("System.out.println(a+b);", '$main(1,2); $test.expect($userOut.toString(),3+"\\n");', {
                name: 'TestAB',
                exp: 1,
                inputs:['int a', 'int b']
            }, function(err, report, stout, sterr) {
                if (err) {
                    return done(err);
                }
                expect(report.passed).to.be.true;
                done();
            });
        });

        it('should run $main with input and fail', function(done) {
            runner.test("System.out.print(a+b);", '$main(1,2); $test.expect($userOut.toString(),3+"\\n");', {
                name: 'TestAB',
                exp: 1,
                inputs:['int a', 'int b']
            }, function(err, report, stout, sterr) {
                if (err) {
                    return done(err);
                }
                expect(report.passed).to.be.false;
                // console.log(report);
                expect(report.failures.length).to.equal(1);
                done();
            });
        });

        it('should run $main with input and return multiple tests', function(done) {
            runner.test("System.out.println(x+1);", 'int runs = 5; for(int i =0;i<runs;i++) {$userOut.reset(); int t = (int)(Math.random()*1000); $main(t); $test.expect($userOut.toString(), (t+1)+"\\n", 2);  }', {
                name: 'TestMultiple',
                exp: 10,
                inputs:['int x']
            }, function(err, report, stout, sterr) {
                if (err) {
                    return done(err);
                }
                // console.log(report);
                expect(report.passed).to.be.true;
                expect(report.passes.length).to.equal(5);
                expect(report.score).to.equal(10);
                done();
            });
        });
        
        it('should be able to print in test stout', function(done) {
            runner.test(code, '$test.print("Hello");', {
                name: 'TestMain',
                exp: 1
            }, function(err, report, stout, sterr) {
                if (err) {
                    return done(err);
                }
                expect(stout).to.equal("Hello");
                done();
            });
        });

        it('should have access to getCode', function(done) {
            runner.test(code, '$test.print($test.getCode());', {
                name: 'TestMain',
                exp: 1
            }, function(err, report, stout, sterr) {
                if (err) {
                    return done(err);
                }
                expect(stout).to.equal(code);
                done();
            });
        });

        it('should be able to test using $test.contains', function(done) {
            runner.test(code, '$test.contains($test.getCode(), "char c =");', {
                name: 'TestMain',
                exp: 1
            }, function(err, report, stout, sterr) {
                if (err) {
                    return done(err);
                }
                expect(report.passed).to.be.true;
                done();
            });
        });

        it('should output escaped regex in message when $test.match fail', function(done) {
            runner.test(code, '$test.matches($test.getCode(), "char \\\\sc =");', {
                name: 'TestMain',
                exp: 1
            }, function(err, report, stout, sterr) {
                if (err) {
                    return done(err);
                }
                expect(report.passed).to.be.false;
                done();
            });
        });

        it('$test.pass should retrn true', function(done) {
            runner.test(code, 'System.err.print(""+$test.pass());', {
                name: 'TestMain',
                exp: 1
            }, function(err, report, stout, sterr) {
                if (err) {
                    return done(err);
                }
                expect(sterr).to.equal('true');
                done();
            });
        });
        it('$test.fail should retrn false', function(done) {
            runner.test(code, 'System.err.print(""+$test.fail());', {
                name: 'TestMain',
                exp: 1
            }, function(err, report, stout, sterr) {
                if (err) {
                    return done(err);
                }
                expect(sterr).to.equal('false');
                done();
            });
        });

        it('should return true if $test.contains passes', function(done) {
            runner.test(code, 'if($test.contains($test.getCode(), "char c =")) { System.err.print(""+true);} else { System.err.print(""+false); }', {
                name: 'TestMain',
                exp: 1
            }, function(err, report, stout, sterr) {
                // console.log(err, report, stout, sterr);
                if (err) {
                    return done(err);
                }
                expect(sterr).to.equal('true');
                done();
            });
        });

        it('should timout if more then timelimit', function(done) {
            runner.run('long i = 100000000; while(i>0){if(i==30000){i+=3000;}i--;} System.out.print(i);', {
                timeLimit: 800
            }, function(err, stout, sterr) {
                if (err) {
                    // console.error(err + '\n==========================\n');
                    return done(err);
                }
                // stout && console.log(stout);
                expect(sterr).to.equal("TimeoutException: Your program ran for more than 800ms");
                done();
            });
        });
    });
});
