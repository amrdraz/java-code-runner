/**
 * A request to run
 */

var Request = module.exports = function(request) {
    request._runner = {
        status: "waiting"
    };
    request.isWaiting = function() {
        return this._runner.status === "waiting";
    };
    request.setToWaiting = function() {
        this._runner.status = "waiting";
    };

    request.isRunning = function() {
        return this._runner.status === "running";
    };
    request.setToRunning = function() {
        this._runner.status = "running";
    };

    request.isDone = function() {
        return this._runner.status === "done";
    };

    request.setToDone = function() {
        this._runner.status = "done";
    };

    request.print = function () {
        return equest._runner.status;
    };

    return request;
};
