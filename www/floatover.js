// Empty constructor
function FloatOver() {}

// The function that passes work along to native shells
// Title and Message is a string
FloatOver.prototype.show = function(title, message, successCallback, errorCallback) {
  var options = {};
  options.title = title;
  options.message = message;
  cordova.exec(successCallback, errorCallback, 'FloatOver', 'show', [options]);
}

// Installation constructor that binds FloatOver to window
FloatOver.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.floatOver = new FloatOver();
  return window.plugins.floatOver;
};
cordova.addConstructor(FloatOver.install);