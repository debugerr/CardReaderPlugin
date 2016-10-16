var cardReaderExports = {
    init: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CardReaderPlugin", "init", []);
    },
    addListener: function (callback, successCallback, errorCallback) {
        document.addEventListener("nfc_cardreader_tag", callback, false);
        cordova.exec(successCallback, errorCallback, "CardReaderPlugin", "addListener", []);
    },
    removeListener: function (callback, successCallback, errorCallback) {
        document.removeEventListener("nfc_cardreader_tag", callback, false);
        cordova.exec(successCallback, errorCallback, "CardReaderPlugin", "removeListener", []);
    }
};

window.cardReader = cardReaderExports;