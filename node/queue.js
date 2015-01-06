var log = require('util').log;
var _ = require('lodash');
var observer = require('./observer');
var config = require('./config');
var Request = require('./request');

var waitingQueue = [];
var runningQueue = [];

var Queue = module.exports = {
    canRun: function() {
        return runningQueue.length < config.runningLimit;
    },
    wait: function(req) {
        req = Request(req);
        waitingQueue.push(req);
    },
    run: function() {
        while(waitingQueue.length > 0 && this.canRun()) {
            var req = waitingQueue.shift();
            req.setToRunning();
            runningQueue.push(req);
            // log("moving from waiting to running");
            // this.checkQueues();
            observer.emit("runner.run", req);
        }
    },
    clearDone: function() {
        while(runningQueue.length > 0 && runningQueue[0].isDone()) {
            // log("removing from running");
            runningQueue.shift();
            // this.checkQueues();
            // log("popped from running");
            this.run();
        }
    },
    getWaiting: function() {
        return waitingQueue;
    },
    getRunning: function() {
        return runningQueue;
    },
    checkQueues: function() {
        log("waitingQueue:" + waitingQueue.length + ", runningQueue:" + runningQueue.length);
    },
    rollBack: function() {
        if (runningQueue.length > 0) {
            while (runningQueue.length  > 0) {
                var req = runningQueue.pop();
                if(!req.isDone()) {
                    req.setToWaiting();
                    waitingQueue.unshift(req);
                }
            }
            // waitingQueue = runningQueue.concat(waitingQueue);
            // runningQueue = [];
        }
    },
    pushBack: function (req) {
        if(!req.isDone()) {
            req.setToWaiting();
            waitingQueue.unshift(req);
        }  
    },
    dropRunning: function() {
        while (runningQueue.length  > 0) {
            var req = runningQueue.shift();
            if(!req.isDone()) req.cb(new Error("Your request has been dropped due to server restart, Submit again"));
        }
    },
    dropWaiting: function() {
        while (waitingQueue.length  > 0) {
            var req = waitingQueue.shift();
            if(!req.isDone()) req.cb(new Error("Your request has been dropped due to server restart, Submit again"));
        }
    }
};

observer.on("runner.finished", function(req) {
    // log("finished with one");
    // Queue.checkQueues();
    req.setToDone();
    Queue.clearDone();
});
observer.on("runner.post_error", function (req) {
    if (req.isWaiting()) {
        // log("pushed back req");
        Queue.pushBack(req);
        // Queue.checkQueues();
    }
});
observer.on("server.running", function() {
    // log("Queue starts running again");
    // Queue.checkQueues();
    Queue.run();
});
observer.on("server.stoped", function(kill) {
    if(kill) {
        log("dropping");
        Queue.checkQueues();
        Queue.dropRunning();
        Queue.dropWaiting();
    } else {
        log("rollling back");
        Queue.rollBack();
    }
});
observer.on("server.exit", function (code) {
    log("recived server exit in Queue " +code);
    if (waitingQueue.length>0 || code===null) {
        log("should restart");
        observer.emit("server.restart");
    }
});
