var exec = require("cordova/exec");

var cardreaderExport = {};

cardreaderExport.init = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "CardReader", "init");
};

cardreaderExport.addListener = function (callback, successCallback, errorCallback) {
    document.addEventListener("nfc_cardreader_tag", callback, false);
    exec(successCallback, errorCallback, "CardReader", "addListener");
};

cardreaderExport.removeListener = function (callback, successCallback, errorCallback) {
    document.removeEventListener("nfc_cardreader_tag", callback, false);
    exec(successCallback, errorCallback, "CardReader", "removeListener");
};


module.exports = cardreaderExport;
window.cardReader = cardreaderExport;