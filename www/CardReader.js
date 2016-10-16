var cardReaderExports = {
    init: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CardReader", "init");
    },
    addListener: function (callback, successCallback, errorCallback) {
        document.addEventListener("nfc_cardreader_tag", callback, false);
        cordova.exec(successCallback, errorCallback, "CardReader", "addListener");
    },
    removeListener: function (callback, successCallback, errorCallback) {
        document.removeEventListener("nfc_cardreader_tag", callback, false);
        cordova.exec(successCallback, errorCallback, "CardReader", "removeListener");
    }
};

window.cardReader = cardReaderExports;