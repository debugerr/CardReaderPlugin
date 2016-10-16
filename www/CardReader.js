var cardReaderExports = {
    init: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CardReaderPlugin", "init", []);
    },
    addListener: function (callback, errorCallback) {
        //document.addEventListener("nfc_cardreader_tag", callback, false);
        cordova.exec(callback, errorCallback, "CardReaderPlugin", "addListener", []);
    },
    removeListener: function (callback, errorCallback) {
        //document.removeEventListener("nfc_cardreader_tag", callback, false);
        cordova.exec(callback, errorCallback, "CardReaderPlugin", "removeListener", []);
    }
};

window.cardReader = cardReaderExports;