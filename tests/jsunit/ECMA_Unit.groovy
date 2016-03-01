// SeleniumHelper.groovy befindet sich in C:\Users\rplantik\.groovy\classes

import org.junit.*;
import org.openqa.selenium.*;

public class ECMA_Unit {
  
  private static final String bspPath = "/sap(bD1kZSZjPTU1Mg==)/bc/bsp/sap/zau_wtag_z/"

  @Delegate private SeleniumHelper selenium;

  @Before
  public void setUp() throws Exception {
    selenium = new SeleniumHelper("http://d12.migros.ch:8000");
    //selenium.keepBrowserOnQuit = true; // Für Tests (cmd.exe mit Task Manager löschen!)
  }

  @Test
  public void dynamictable() throws Exception {
   test_unit("testdynamictable.htm")
  }
 
  @Test
  public void global_js() throws Exception {
   test_unit("testGlobalJs.htm")
  }
 
  @Test
  public void global_js_with_prototype() throws Exception {
    test_unit("testGlobalJsWithPrototype.htm");
  }
  
  def test_unit( String file ) {
   start( bspPath + file,"auth_d12.html")
   waitFor { present css:".OK" }   
  }

  @After
  public void tearDown() throws Exception {
    selenium.finish( )
    }

}