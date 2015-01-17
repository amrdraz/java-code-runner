/*globals before,after,beforeEach,afterEach,describe,it */
var server = require('../node/server.js');
var expect = require('chai').expect;

describe('Java Server', function() {

    var url;
    var port;

    it('should compile', function (done) {
        this.timeout(5000);
        server.recompile(function (compiled) {
            expect(compiled).to.be.true;
            done();
        });
    });
    
    it('should not compile again in the same process', function (done) {
        server.recompile(function(compiled) {
            expect(compiled).to.be.false;
            done();
        });
    });
});