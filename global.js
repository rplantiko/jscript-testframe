// Die meisten Aufgaben, die man mit FSO so macht, sind zustandslos
var fso = new ActiveXObject("Scripting.FileSystemObject");
	
// Log
function Log(filename) {
  this.logfile =  fso.OpenTextFile(filename,8,0); //eslint-disable-line new-cap
  this.lines   = [];
  this.atests  = {};
  this.stests  = {};
  this.utests  = {};
  }
Log.prototype.add = function( text ) {
  this.lines.push( text );
	this.logfile.WriteLine( text );  //eslint-disable-line new-cap
  }
Log.prototype.close = function() {
  this.logfile.close();
  }  

var log = new Log("log.txt"); //eslint-disable-line new-cap


