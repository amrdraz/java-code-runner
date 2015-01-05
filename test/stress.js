/*globals before,after,beforeEach,afterEach,describe,it */
var runner = require('../index.js');
var expect = require('chai').expect;
var Promise = require('bluebird');
var _ = require('lodash');

require('./compile');

describe('Server Stressing', function() {
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
    

    var code = 'char c =\'a\'; \n int a = 40, b = 20; System.out.print("a - b = " + (a - b));';
    var test = '$main();\n$test.expect($userOut.toString(),"a - b = 20");';

    it('should run and queue anything above runLimit slowly dequeing when a slot is available concurently', function(done) {
        this.timeout(25000);
        Promise.map(new Array(1000), function(x, i) {
            return new Promise(function(resolve, reject) {
                // setTimeout(function() {
                var start = new Date().getTime();
                runner.run('System.out.print("Hello");System.out.print("World");', {
                    debug_number: i
                }, function(err, stout, sterr) {
                    if (err) {
                        // console.error(err + '\n==========================\n');
                        reject(err);
                    }
                    // sterr && console.error(sterr);
                    expect(stout).to.equal('HelloWorld');
                    var end = new Date().getTime();
                    var time = end - start;
                    // console.log('ran in ' + time + 'ms');
                    resolve(time);
                });
                // }, i * 1); //simulate trafic
            });
        }).then(function(times) {
            console.log("responded on average in " + (times.reduce(function(sum, t) {
                return sum + t;
            }, 0) / times.length));
            console.log("maximum awiting time waited " + (times.reduce(function(max, t) {
                return max < t ? t : max;
            }, 0)));
            done();
        }).catch(done);
    });

    it('should be able to handle runinng code while resarting runner.server', function(done) {
        this.timeout(20000);
        var timer, i = 0;
        timer = setInterval(function() {
            _.each(new Array(20), function() {
                i++;
                runner.run(code, function(err, stout, sterr) {
                    if(sterr) console.log(sterr);
                    i--;
                    //expect(stout).to.equal('a - b = 20');
                });
            }); 
        }, 1000); //simulate trafic
        runner.test(code, test, {
            name: 'TestMain',
            exp: 1,
            debug_number: i++
        }, function(err, report, stout, sterr) {
            expect(report.passed).to.be.true;
            // console.log("runner.server ran one test case in stress.js test");
            runner.server.stopServer(function(stoped) {
                runner.run(code, function(err, stout, sterr) {
                    expect(stout).to.equal('a - b = 20');
                    clearInterval(timer);
                    // console.log("waiting on other runs to finish");
                    timer = setInterval(function () {
                        if(i>0) {
                            clearInterval(timer);
                            done();
                        }
                    });
                });
            });
        });

    });

    it('should manage high trafic and be able to restart when crashed', function(done) {
        this.timeout(30000);
        var timer = setTimeout(function() {
            console.log("===================stop");
            runner.server.stopServer();
        }, 5000);
        // finished = 0;
        Promise.map(new Array(900), function(x, i) {
            return new Promise(function(resolve, reject) {
                // setTimeout(function() {
                    var start = new Date().getTime();
                    runner.run('System.out.print("Hello");System.out.print("World");', {
                        debug_number: i
                    }, function(err, stout, sterr) {
                        if (err) {
                            // console.error(err + '\n==========================\n');
                            reject(err);
                        }
                        // finished++;
                        // sterr && console.error(sterr);
                        expect(stout).to.equal('HelloWorld');
                        var end = new Date().getTime();
                        var time = end - start;
                        // console.log('ran in ' + time + 'ms');
                        // console.log("finished "+finished);
                        resolve(time);
                    });
                // }, i * 10); //simulate trafic
            });
        }).then(function(times) {
            clearInterval(timer);
            console.log("responded on average in " + (times.reduce(function(sum, t) {
                return sum + t;
            }, 0) / times.length));
            console.log("maximum awiting time waited " + (times.reduce(function(max, t) {
                return max < t ? t : max;
            }, 0)));
            done();
        }).catch(done);
    });

});
