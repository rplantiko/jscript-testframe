// SeleniumHelper.groovy befindet sich in %USERPROFILE%\.groovy\classes

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import org.openqa.selenium.*;

@RunWith(Parameterized.class)
public class NavigationBasicD12 {
  
  @Parameter
  public Map application;

  @Delegate private SeleniumHelper selenium; 
  private static SeleniumHelper seleniumStatic;
  private static WebDriver driver;

  @Parameters(name="{index}: {0}") static Collection<Object[]> data() {
    /*single test: */ false ?  /* Um nur eine einzelne Navigation zu testen */
    [
      [ href:"../zsrs_zwstrd/zwstrd.do",
        title:"Liste Direktlieferungen"],
    ]
    :    
    [
      [ href:"../zsrs_wosw/wosw.do",
        title:"Betrieb anzeigen"],
      [ href:"../zsrs_wstsa/wstsa.do",
        title:"Sortimentsliste"],
      [ href:"../zsrs_zmati/zmati.do",
        title:"Artikelinfo"],
      [ href:"../zsrs_zsmb/main.do",
        title:"Soll- Meldebestände",
        hasDatalossPopup:true],
      [ href:"../ZSRS_wssa/wssa.do",
        title:"Kundenauftrag"],
      [ href:"../zsrs_kdauf_list/kdauf.do",
        title:"Kundenauftragslisten"],
      [ href:"../zsrs_zwstra/zwstra.do",
        title:"Anzahlungen"],
      [ href:"../zsrs_zwsli/zwsli.do",
        title:"Lieferliste"],
      [ href:"../zsrs_wsau_n/main.do",
        title:"Aufteiler"],
      [ href:"../zsrs_wsak_n/main.do",
        title:"Aktionen"],
      [ href:"../zsrs_liqun/liqun.do",
        title:"Liquidation"],
      [ href:"../zsrs_zwstru/zwstru.do",
        title:"Bestandsübersicht"],
      [ href:"../zsrs_zwstrb/zwstrb.do",
        title:"Artikelbewegungen"],
      [ href:"../zsrs_zwstrs/zwstrs.do",
        title:"Sortimentsauswertung"],
      [ href:"../zsrs_zwstrd/zwstrd.do",
        title:"Liste Direktlieferungen"],
      [ href:"../zsrs_zwstrn/zwstrn.do",
        title:"Negative Bestände"],
      [ href:"../zsrs_trfc/monitor.do",
        title:"Hintergrundverarbeitungen"],
      [ href:"../zsrs_zrinv/zrinv.do",
        title:"Rollende Inventur"],
      [ href:"../zsrs_wste/wste.do",
        title:"Wareneingang"],
      [ href:"../zsrs_wstv/wstv.do",
        title:"Warenclearing"],
      [ href:"../zsrs_zwstld/zwstld.do",
        title:"Lieferdiff./TP-Schäden"],
      [ href:"../zsrs_wsti/wsti.do",
        title:"Inventur"],
      [ href:"../zsrs_rfaf/rfaf.do",
        title:"Filialauftragsfolgebelege"]
    ]
  }

  private static final String backSelector = 'area[accesskey=b]'
  private static final String menuSelector = 'area[accesskey=m]'

  
  @Test
  public void testNavigationBasic() throws Exception {
      
      // Jeder Test muss mit dem Menü starten
      gotoURL(selenium.baseURL+"/sap/bc/bsp/sap/zsrs_intro/intro.do")
      waitFor { pageLoaded() }
      
      def linkSelector = /a[href="${application.href}"]/            
       
      verify {  present css:linkSelector }
      click   css:linkSelector 
      waitFor { pageLoaded() }
      String headerTextFromPage = text( css:'.headerIACText' )
      String headerText = new String(headerTextFromPage.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      verify { headerText == application.title }
      click css: ( application.useMenuButton ? menuSelector : backSelector )      
      if (application.hasDatalossPopup) {
        verify { alertPresent() }
        acceptAlert()
      }
    
  }

  @Before
  public void setUp() throws Exception {
    if (seleniumStatic == null) {
      seleniumStatic = selenium = new SeleniumHelper("https://d12.migros.ch:1490")  
      // selenium.keepBrowserOnQuit = true; // Für Tests (cmd.exe mit Task Manager löschen!)
      start( "","auth_d12_565_2700.html")
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


}
