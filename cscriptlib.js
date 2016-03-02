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

  if (process.ExitCode != 0 || !opt.ignoreStdErr && stderr.match(/\S/)) {
    var message = "Fehler bei Ausführung von Kommando:\n" +
          opt.cmd + "\n";
    if (process.ExitCode != 0) {
      message +=
        "Abgebrochen mit Returncode <> 0 (" + process.ExitCode + ")\n";
    }
    else  {
      message +=
        "Abgebrochen mit nichtleerem stderr\n"
    }
    message += "stderr = '\n" + stderr + "'\n";
    throw  message;
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

// SendMail
// I tried to use the built-in ActiveX object CDO.Message, but couldn't get rid of the erroe
//   CDO.Message.1: Der "SendUsing"-Konfigurationswert ist ungültig.
// This message came even when I passed the Configuration.Fields parameters as specified
// Finally, I used this simple tool, http://caspian.dotconf.net/menu/Software/SendEmail/
// which immediately worked as expected
function sendMail(opts) {

  function MailOptions() {
    this.options = [];
    }

  MailOptions.prototype.addOptionArray = function(optArray) {
    var options = this;
    optArray.each( function() {
      if (this[1] !== undefined) {
        options.add(this[0],this[1]);
      }
      });
    };

  MailOptions.prototype.add = function( option, value ) {
    this.options.push({ option:option, value:value });
    };

  MailOptions.prototype.get = function() {
    var s = [];
    this.options.each( function() {
      s.push(this.option + ' ' + this.value);
      });
    return s.join(' ');
    };

  var options = new MailOptions();
  options.addOptionArray([
        [ '-f', opts.from],
        [ '-t', opts.to],
        [ '-s', opts.server],
        [ '-u', opts.subject]]);

  var exe = 'sendEmail.exe';
  if (opts.exePath) {
    exe = opts.exePath + exe;
  }

  var result = executeCommand({
    cmd:'"'+exe+'" ' + options.get( ),
    input: opts.body
  })

  return result;

}