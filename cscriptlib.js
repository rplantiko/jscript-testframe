// Wiederverwendare Funktionen in der Automation (cscript //e:jscript)

// Einen Befehl ausführen (mittels "exec")
// stdin, path und environment params können übergeben werden
// Rückgabe: stdout
function executeCommand( opt ) {

  var sh = WScript.CreateObject("Wscript.Shell");
  var stdout = "", stderr = "";

  if (opt.path) sh.CurrentDirectory = opt.path;

  if (opt.param) {
    setProcessEnv(opt.param);
    }

// java or javac: Shut off HP Quality Center's environment pollution
  if (opt.cmd.match(/^java/)) {
    if (typeof JAVA_HOME == "string") opt.cmd = '"' + opt.cmd.replace(/^java\b/,JAVA_HOME) + '"';
    setProcessEnv( getParamsToShutOffQualityCenterIntrusion() );
    }

  var process = sh.exec( opt.cmd );
  
  if (opt.input) {
    process.StdIn.Write(opt.input);
    process.StdIn.Close();
    }

  do {
    WScript.sleep( 100 );
    stdout += readFromTo( process.StdOut );
    stderr += readFromTo( process.StdErr );
    } while (!process.status);
    stdout += readFromTo( process.StdOut );
    stderr += readFromTo( process.StdErr );

  if (process.ExitCode != 0 || stderr.match(/\S/)) {
    throw "Fehler bei Ausführung von Kommando:\n" +
          opt.cmd + "\n" +
          "Abgebrochen mit Returncode " + process.ExitCode + "\n" +
          "und/oder nichtleerem stderr: " + stderr ;
    }

  return stdout.replace(/\s*$/,"");;

  function readFromTo( channel ) {
    return channel.AtEndOfStream ? "" : channel.ReadAll();
  }

  function setProcessEnv( param ) {
    var v = sh.Environment("PROCESS"), name;
    for (name in param) {
// This is possible only for host objects: A function's return value as lvalue
      v(name) = param[name];
      }
    }

  function getProcessEnv( name ) {
    var v = sh.Environment("PROCESS");
    return v(name);
    }


  function getParamsToShutOffQualityCenterIntrusion() {
    var params = {
            JAVA_TOOL_OPTIONS:'',
            _JAVA_OPTIONS:''
           };
    if (typeof JAVA_HOME == "string") params.JAVA_HOME = JAVA_HOME;
    return params;
    }

  }

// Umgebungsvariable ermitteln
var getenv = (function() { 
  var sh = WScript.CreateObject("Wscript.Shell");
  var v = sh.Environment("PROCESS");
  return function(name) {
    return v(name);
  }  
})();

// Read a file completely into a string
function readFile( filename  ) {
  var fso = new ActiveXObject("Scripting.FileSystemObject");
  var fs = fso.OpenTextFile( filename, 1 );
  return fs.ReadAll();
  }
