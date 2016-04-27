// SeleniumHelper.groovy befindet sich in %USERPROFILE%\.groovy\classes

import org.openqa.selenium.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
//import static org.junit.Assert.*;
//import static org.hamcrest.CoreMatchers.*;

@RunWith(Parameterized.class)
public class QUnit {

  private static final String UI5 = "/sap/bc/ui5_ui5/sap/"

  @Delegate private SeleniumHelper selenium;

  @Parameter
  public String testfile;

  @Parameters static Collection<Object[]> data() {
   [
     "zrt_library/test.qunit.html"
   ]
  }



  @Before
  public void setUp() throws Exception {
    selenium = new SeleniumHelper("http://dgm.migros.ch:8000");
    //selenium.keepBrowserOnQuit = true; // Für Tests (cmd.exe mit Task Manager löschen!)
  }

  @Test
  public void test() throws Exception {
   println "Teste Script ${ testfile }..."
   test_unit(testfile)
  }

  def test_unit( String file ) {
   start( UI5 + file,"auth_dgm.html")
   waitFor { present css:"#qunit-banner.qunit-pass" }
  }

  @After
  public void tearDown() throws Exception {
    selenium.finish( )
    }

}