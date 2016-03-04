/* Zentrales Ausf�hrungsscript f�r Tests */

/* global fso, log, executeCommand, sendMail, 
   DIR_TESTS, DIR_PORTABLES, SUPPRESS_SEND_TO_SAPDEV */

addTimeStampToErrorLog();

var results = doAllTests();

if (!SUPPRESS_SEND_TO_SAPDEV) {
  sendToSapdev( results );
  sendErrorMails( results );
  }
else {
  appendToErrorLog( JSON.stringify( results ) );
}

logStatistics( results );

// ------------------------------------------------------------------------
// Ab hier Funktionen
// ------------------------------------------------------------------------
function doAllTests() {

  try {

// Indexdatei aller Tests einlesen
    var index = readFile( "tests/index.json" );
    var tests = JSON.parse( index );

// Schleife �ber alle Tests (durch key/subkey definiert)
    var results = {};
    var key, subkey;
    for (key in tests) {
      var group = tests[key];
      results[key] = {};
      for (subkey in group) {
        results[key][subkey] = doTest( group[subkey] );
      }
    }
  } catch (e) {
// Wenn uns der ganze Scriptrahmen um die Ohren fliegt:    
    results = {
      "GENERAL": {
        "SYSTEM": {
          passed:false,
          messages:e.description
          }
        }
      };
  }

  return results;

}

function doTest( test ) {

  var result;

  log.add("< starting " + test.file );

// Hier sind Verzweigungen f�r verschiedene Testarten m�glich
  if (test.file.match(/\.groovy$/)) {
   result = doGroovyTest(test.file);
  }

  log.add("> finished " + test.file );
  
  if (test.author) result.author = test.author;  

  return result;

}

function doGroovyTest( file ) {

// Testen ist pessimistisch: By default geht gar nichts
  var result = { passed:false, messages:"" };
  
// Relativer Pfadname bezieht sich auf DIR_TESTS  
  if (!file.match(/^(?:[A-Z]:|\\)/i)) {
    file = DIR_TESTS + file;
  }

// Pfade in die bei MS �blichen Backslashes �bersetzen
  file = file.replace(/\//g,"\\");

// Kommando f�r Groovy aufbauen
  var cmd = '"'+DIR_PORTABLES+'groovy.bat" ' + file;
  
  log.add("cmd: " + cmd );

  try {
    result.messages = executeCommand({
      cmd:cmd
      }).replace(/Closing Browser session...[\r\n]*/g,"");
    log.add( result.messages );
    result.passed = true;
    // War es ein JUnit-Test?
    var m = result.messages.match(/^JUnit.*?Failures:\s*(\d+)/m);
    if (m && m[1] > 0) result.passed = false;
    }
    catch (e) {
// cscriptlib's executeCommand benutzt einen String als Error-Objekt
// n�mlich den Inhalt von stderr, falls dieser Kanal nach Ausf�hrung nichtleer ist
// Die Ausnahme kommt auch, wenn das Kommando mit Exit-Code ungleich 0 endet      
       log.add( "Fehler bei Groovy-Ausf�hrung: " + (e.description || e) );
       result.messages += "\n" + (e.description || e);
       }

  return result;

}

function sendToSapdev( results ) {
  
  try {

    var response = prepareResponse( results );
  
    // Ergebnis an den Server sapdev.mits.ch senden
    var responseFromSendToServer = executeCommand({
      cmd: '"' + DIR_PORTABLES + 'curl.exe" ' +
           '-X POST ' +
           '-H "Content-Type: application/json" ' +
           '-d ' +
           '@- ' +
           '--silent ' +
           'http://sapdev.mits.ch/bin/put_test_results_ci',
      input:response
      });
  
    log.add( 
      "Results sent to sapdev.mits.ch - response: " 
      + responseFromSendToServer );

  } catch (e) {
    appendToErrorLog( 
      "Failed to send to http://sapdev.mits.ch\n  Exception: "
       + (e.description || e) );
  }


  function prepareResponse( results ) {

    var response = {};
    response.results = results;

    var now = new Date();
    response.date = now.getDate( )+"."+(1+now.getMonth( ))+"."+now.getYear( );

    return JSON.stringify(response);

  }

}

function sendErrorMails( results ) {
  for (var group in results) {
    for (var key in results[group]) {
      var result = results[group][key];
      if (!result.passed && result.author.match(/@/)) {
        try {
          appendToErrorLog( 
            "Sending error mail for " + group + "." + key + 
            " to " + result.author + " ...");
          var body = "Hallo,\n\nder Test " + group + "." + key + 
            " war nicht erfolgreich.\nFolgende Fehler sind aufgetreten\n\n" + 
            result.messages;
          sendMail({
            to:result.author,
            from:"RetailCockpit@mgb.ch",
            server:"mxswitch.mgb.ch",
            subject:"CI-Test " + group + "." + key + " ist schlug fehl",
            body:body,
            exePath:DIR_PORTABLES
            });
          appendToErrorLog( "...done" );
        } catch (e) {
          appendToErrorLog( "Failed to send mail: " + (e.description || e) + "\n");
        }
      }
    }
  }    
}



function logStatistics( results ) {
  
  var stats = {}, group;

  for (group in results) {
    stats[group] = {total:0, failed:0};    
    for (var test in results[group]) {
      stats[group].total++;
      if (!results[group][test].passed) stats[group].failed++;
      }     
  }

  var result = "  Test failures: \n";
  var totalFailures = 0;
  for (group in stats) {
    result+= "    " + (group+"                              ").slice(30) 
      + ": " 
      + stats[group].failed 
      + "(" + stats[group].total + ")\n";
    totalFailures += stats[group].failed;
  }
  result += "  Failures total : " + totalFailures + "\n";

  appendToErrorLog( result );

}



function addTimeStampToErrorLog() {
  appendToErrorLog( "--- " + new Date() ); 
}

function appendToErrorLog( text ) {
  var errlog = fso.OpenTextFile("do-tests-log.txt",8,0); //eslint-disable-line new-cap
  errlog.WriteLine( text );               //eslint-disable-line new-cap
  errlog.close();
}