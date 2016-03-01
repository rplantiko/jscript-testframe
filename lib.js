// --- each() Array-Iteration mit each()
// Die Callbackfunktion erhaelt den Wert des Array-Elements als "this"
// Als Argument wird der aktuelle Array-Index uebergeben
// Kompatibel zu "Prototype"
if (!this.$break) { $break = {};  } // Spezielle Exception, um aus each() auszubrechen
Array.prototype.each = function(f,context) {
  var i,n=this.length;
  try {
    for (i=0;i<n;++i) {
      f.call(context||this[i],this[i],i);
      }
    }
    catch(e) {
      if (e!=$break) {throw e; }
      }
  };

// --- subset(filter)
// Die Teilmenge aller Arrayelemente bilden, fuer die die filter-Funktion true ergibt
Array.prototype.subset = function(filter) {
  var result = [];
  this.each( function() {
    if (filter.call(this)) { result.push(this); }
    });
  return result;
  };
  
// --- Browseruebergreifender HTTP-Request
// callback,data,action,headerFields sind optional
function doRequest(url,callback,data,action,headerFields) {
  var field,value,
      theData   = data || null,
      requestor =  getRequestor();
  requestor.open(action||"GET",url,!!callback);  // Synchron <=> kein Callback
  if (callback) { requestor.onreadystatechange = function() {
     if (requestor.readyState ==4) {
       callback.call(requestor);  // Mit requestor = this aufrufen
       }
     };
    }
  if (headerFields) {
    for (field in headerFields) {
       requestor.setRequestHeader(field,headerFields[field]);
       }
    }
  requestor.send(theData);
  return callback ? requestor : requestor.responseText;
}

// --- Ein  XMLHTTPRequest-Objekt beschaffen
function getRequestor() {
  return new ActiveXObject("Msxml2.XMLHttp.6.0");
  }  


// Production steps of ECMA-262, Edition 5, 15.4.4.17
// Reference: http://es5.github.io/#x15.4.4.17
if (!Array.prototype.some) {
  Array.prototype.some = function(fun/*, thisArg*/) {
    'use strict';

    if (this == null) {
      throw new TypeError('Array.prototype.some called on null or undefined');
    }

    if (typeof fun !== 'function') {
      throw new TypeError();
    }

    var t = Object(this);
    var len = t.length >>> 0;

    var thisArg = arguments.length >= 2 ? arguments[1] : void 0;
    for (var i = 0; i < len; i++) {
      if (i in t && fun.call(thisArg, t[i], i, t)) {
        return true;
      }
    }

    return false;
  };
}