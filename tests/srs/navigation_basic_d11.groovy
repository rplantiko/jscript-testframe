// SeleniumHelper.groovy befindet sich in C:\Users\rplantik\.groovy\classes

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import org.openqa.selenium.*;

@RunWith(Parameterized.class)
public class NavigationBasicD11 {
  
  @Parameter
  public Map application;

  @Delegate private SeleniumHelper selenium; 
  private static SeleniumHelper seleniumStatic;
  private static WebDriver driver;

  @Parameters(name="{index}: {0}") static Collection<Object[]> data() {
    /*single test: */ false ?  /* Um nur eine einzelne Navigation zu testen */
    [
      [ id:"wosw",
        title:"Liste Direktlieferungen"],
    ]
    :    
    [
    [ id:"wosw",
      title:"Betrieb anzeigen"],
    [ id:"wstsa",
      title:"Sortimentsliste"],
    [ id:"_/sap/bc/bsp/sap/zsrs_zmati/zmati.do",
      title:"Artikelinfo"],
    [ id:"_/sap/bc/bsp/sap/zsrs_zsmb/main.do", hasDatalossPopup: true,
      title:"Soll- Meldebestände"],
    [ id:"_/sap/bc/bsp/sap/zsrs_lili/main.do",
      title:"Lieferliste"],
    [ id:"_/sap/bc/bsp/sap/zsrs_wsau_n/main.do",
      title:"Aufteiler"],
    [ id:"_/sap/bc/bsp/sap/zsrs_wsau_sa/main.do",
      title:"Saisonaufteiler"],
    [ id:"_/sap/bc/bsp/sap/zsrs_wsak_n/main.do",
      title:"Aktionen"],
    [ id:"_/sap/bc/bsp/sap/zsrs_liqun/liqun.do",
      title:"Liquidation"],
    [ id:"zwstru",
      title:"Bestandsübersicht"],
    [ id:"zwstrb",
      title:"Artikelbewegungen"],
    [ id:"zwstrs",
      title:"Sortimentsauswertung"],
    [ id:"_/sap/bc/bsp/sap/zsrs_avlbf/avlbf.sap",
      title:"AV-Liste Best.-führung"],
    [ id:"_/sap/bc/bsp/sap/zsrs_trfc/monitor.do",
      title:"Hintergrundverarbeitungen"],
    [ id:"_/sap/bc/bsp/sap/zsrs_zrinv/zrinv.do",
      title:"Rollende Inventur"],
    [ id:"wstv",
      title:"Warenverschiebung"],
    [ id:"wsti",
      title:"Inventur"],
    [ id:"zwstir",
      title:"Inventur"],
    [ id:"wsta",
      title:"Filialauftrag"],
    [ id:"zdrlanf", hasDatalossPopup: true,
      title:"Drucklisten"]
  ]
  }


  
  @Test
  public void testNavigationBasic() throws Exception {
      
      String fw = application.id.startsWith("_") ? "bsp" : "its"
      
      // Jeder Test muss mit dem Menü starten
      gotoURL(selenium.baseURL+"/sap(bD1kZSZjPTUyNQ==)/bc/bsp/sap/zsrs_wosm/main.do")
      waitFor { pageLoaded() }
      
//      println "· " + application.title

      def linkSelector = /span[id='${application.id}']/                   
      verify {  present css:linkSelector }
      click   css:linkSelector 
      waitFor { pageLoaded() }
            
      String headerTextFromPage = getHeaderText[fw]();      
      String headerText = new String(headerTextFromPage.getBytes(java.nio.charset.StandardCharsets.UTF_8));      
      
      verify { headerText == application.title }

      gotoMenu[fw](application.useMenuButton)
      if (application.hasDatalossPopup) {
        verify { alertPresent() }
        acceptAlert()
      }
      
      waitFor { pageLoaded() }
      verify {  driver.getCurrentUrl().endsWith('zsrs_wosm/main.do') }
    
  }

  @Before
  public void setUp() throws Exception {
    if (seleniumStatic == null) {
      seleniumStatic = selenium = new SeleniumHelper("https://d11.migros.ch:1490")  
      // selenium.keepBrowserOnQuit = true; // Für Tests (cmd.exe mit Task Manager löschen!)
      start( "","auth_d11_525_4823.html")
      driver = selenium.driver
    }
    else {
      selenium = seleniumStatic
    }  
  }

  @AfterClass
  public static void tearDown() throws Exception {
    seleniumStatic.finish( )
    }

  
  def getHeaderText = [
    bsp: { text( css:'.headerIACText' ) },
    its: { executeScript( 'return window.frames[0].document.querySelector(".headerIACText").textContent.replace(/^\\s*/,"").replace(/\\s*$/,"")'
      ) }
  ]

  def gotoMenu = [
    bsp: {  useMenuButton ->
      char accesskey = useMenuButton ? '1' : '2';
      click css: "area:nth-of-type(${accesskey})"
      //click css: "'area[accesskey=${accesskey}]'"  // Leider sind im BSP im D11 nicht immer accesskeys gesetzt
      },
    its: { useMenuButton ->
      char accesskey = useMenuButton ? 'e' : 'z';
      executeScript("window.frames[0].document.querySelector('area[accesskey=${accesskey}]').click()")
      }
  ]


}