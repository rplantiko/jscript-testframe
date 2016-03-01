// SeleniumHelper.groovy befindet sich in C:\Users\rplantik\.groovy\classes

import org.junit.*;
//import static org.junit.Assert.*;
//import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;


public class CreateOffer {

  @Delegate private SeleniumHelper selenium;

  @Before
  public void setUp() throws Exception {

    selenium = new SeleniumHelper("http://migzm251.migros.ch:8000");

    //selenium.keepBrowserOnQuit = true; // Für Tests (cmd.exe mit Task Manager löschen!)

  }

  @Test
  public void createOffer() throws Exception {

    startCreateOffer( )
    erfasseTileEigenschaften( )
    erfasseTileLokationen( )
    erfasseTileTermine( )
    erfasseTileAngebot( )
    erfasseTileZuordnung( )
    erfasseTileControlling( )
    erfasseSortiment( )
    saveOffer( )

  }
 
  @After
  public void tearDown() throws Exception {
    selenium.finish( )
    }

  def erfasseTileEigenschaften = {

    type    id:"ofrName",
            value:"Testangebot"

    type    id:"ofrNameKT",
            value:"Test Offer kurz"

    click   id:"rb_Boss"

    select  id:"ofrBdwelt",
            value:"01"

    select  id:"ofrBdbereich",
            value:"0101"

    verify { present css:"#tl_eigen li.tileOk" }

    }

  def erfasseTileLokationen =  {

    select id:"ofrTactic",  // Planungsart
           value:"01"       // National


    select css:"#ofrDistrKanal select.asmSelect",
           value:"SM/VM"

    verify { present css:"#tl_lokat li.tileOk" }

  }

  def erfasseTileTermine = {

   type   id:"ofrTermineStart",
          value:"6.8.2016"

   type   id:"ofrTermineEnd",
          value:"6.9.2016"
          
   submit( )   // Der blur führt leider nicht automatisch zum Submit

   verify { present css:"#tl_termine li.tileOk" }

  }

  def erfasseTileAngebot = {

    select   id:"ofrPromotype",
             value:"001"

    click    id:"ofrReduktionRBPerc"
    waitFor  { present id:"ofrReduktionPerc" }    
    
    click    id:"ofrReduktionPerc"
    find(id:"ofrReduktionPerc").sendKeys("5\t")

    // Auf Angebots-Tile clicken !!! NOTBEHELF, da obige Eingabe nicht funktioniert!!!
    //click   css:"#tl_angebot > ul > li.tileText"

    verify { present css:"#tl_termine li.tileOk" }

    }

  def erfasseTileZuordnung = {

    select css:"#ofrCampaigns select.asmSelect",
           value:"keine_kampagne"

    verify { present css:"#tl_zuordnung li.tileOk" }

    }

  def erfasseTileControlling = {
  
    // Aktionskategorie 1000000006 gültig bis unendlich          
    select id:"M__offer__gs_ofr_header.aktkat",
           value:"1000000006"
    
    /*
    select id:"M__offer__gs_controlling.aktrg",
           value:"1000000009" //FM Angebote
    */           

    type   id:"M__offer__gs_controlling.umspln",
           value:"1"

    find(id:"M__offer__gs_controlling.mengepln").sendKeys("1\t")

    verify { present css:"#tl_controlling li.tileOk" }

    }



  def erfasseSortiment = {
    
    // Erweiterte Sicht (ohne "tile Rechts")
    click   id:"panelToggler"
    waitFor { ! visible( css:".tileRe" ) }
    
    // Auf Angebots-Tile clicken
    click   css:"#tl_angebot > ul > li.tileText"
    waitFor { present css:"#divTableSortplanung td.dataTables_empty" }

    // Neue Zeile hinzufügen
    click   css:"#divTableSortplanung div.top button.hinzufuegen"
    
    select  id:"M__sortiment__gt_sortiment[000001].PDA",
            value:"03"
            
    waitFor { present css:"#divTableSortplanung .autocomp__bossnr" }        
            
    type    id:"M__sortiment__gt_sortiment[000001].NUMMER",
            value:"01011001\t"                              //CHOCO SNACKS
            
    waitFor { text( css:"#divTableSortplanung td.BEZEICHNUNG" ) == "CHOCO SNACKS" }                  
                    
    }


  def startCreateOffer = {

     start("/sap(bD1kZSZjPTY5MA==)/bc/bsp/sap/zpmr_start/main.do",
           "auth_da1.html")
     click id:"ka04"              // Angebot = Kachel 04
     click id:"btnOfferAnlegen"   // Anlegen


    }
    
  def saveOffer = {
    
    click id:"ofr_btn_anlegen"     
    
    waitFor { present css:"#msg.success" }
    
    println find(id:"msg").getText( )   
    
    }  


}