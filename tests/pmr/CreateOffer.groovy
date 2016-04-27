// SeleniumHelper.groovy befindet sich in %USERPROFILE%\.groovy\classes

import java.util.Map;
import org.openqa.selenium.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
//import static org.junit.Assert.*;
//import static org.hamcrest.CoreMatchers.*;

@RunWith(Parameterized.class)
public class CreateOffer {

  @Parameter
  public Map fixture;

  @Delegate private SeleniumHelper selenium;

  @Parameters static Collection<Object[]> data() {
    [
      [ ofrName: "5 % auf Schokoriegel",
        ofrNameKT: "001_5pc_boss01011001",
        sortTyp:"boss",
        ofrBdwelt:"01",
        ofrBdbereich:"0101",
        ofrTactic:"01",
        ofrDistrKanal:"SM/VM",
        ofrTermineStart:SeleniumHelper.today_plus(90),
        ofrTermineEnd:SeleniumHelper.today_plus(96),
        ofrPromotype:"001",
        reduktionsTyp:"perc",
        ofrReduktionPerc:"5",
        ofrCampaigns:"keine_kampagne",
        ofrAktkat:"1000000006",
        controlling:[
          Umspln:1,
          Mengepln:1
          ],
        sortiment: [
          [
            pda:"03",
            nummer:"01011001",
            expected_text:"CHOCO SNACKS" ]
          ]
        ],
      [ ofrName: "0.10 CHF auf CHOCOLATA 400G.",
        ofrNameKT: "001_10rp_prod1001002",
        sortTyp:"boss",
        ofrBdwelt:"01",
        ofrBdbereich:"0101",
        ofrTactic:"01",
        ofrDistrKanal:"SM/VM",
        ofrTermineStart:SeleniumHelper.today_plus(90),
        ofrTermineEnd:SeleniumHelper.today_plus(96),
        ofrPromotype:"001",
        reduktionsTyp:"abs",
        ofrReduktionAbs:"0.10",
        ofrCampaigns:"keine_kampagne",
        ofrAktkat:"1000000006",
        controlling:[
          Umspln:1,
          Mengepln:1,
          Rabattsatz:5],
        sortiment: [
          [
            pda:"01",
            nummer:"100100200000",
            expected_text:"CHOCOLATA 400G." ]
          ]
        ],
      ]
  }

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
            value:fixture.ofrName

    type    id:"ofrNameKT",
            value:fixture.ofrNameKT

    erfasseSortimentsTyp[fixture.sortTyp]()


    verify { present css:"#tl_eigen li.tileOk" }

    }

  def erfasseSortimentsTyp = [
    boss: {
      click   id:"rb_Boss"

      select  id:"ofrBdwelt",
              value:fixture.ofrBdwelt

      select  id:"ofrBdbereich",
              value:fixture.ofrBdbereich

    }
  ]

  def erfasseTileLokationen =  {

    select id:"ofrTactic",          // Planungsart
           value:fixture.ofrTactic  // National = 01


    select css:"#ofrDistrKanal select.asmSelect",
           value:fixture.ofrDistrKanal

    verify { present css:"#tl_lokat li.tileOk" }

  }

  def erfasseTileTermine = {

   type   id:"ofrTermineStart",
          value:fixture.ofrTermineStart

   type   id:"ofrTermineEnd",
          value:fixture.ofrTermineEnd

   submit( )   // Der blur führt leider nicht automatisch zum Submit

   verify { present css:"#tl_termine li.tileOk" }

  }


  def erfasseTileAngebot = {

    select   id:"ofrPromotype",
             value:fixture.ofrPromotype

    erfasseReduktion[fixture.reduktionsTyp]()

    verify { present css:"#tl_angebot li.tileOk" }

    }


  def erfasseReduktion = [
    perc: {
      click    id:"ofrReduktionRBPerc"
      waitFor  { visible id:"ofrReduktionPerc" }
      click    id:"ofrReduktionPerc"
      find(id:"ofrReduktionPerc").sendKeys(fixture.ofrReduktionPerc+"\t")
    },
    abs: {
      click    id:"ofrReduktionRBAbs"
      waitFor  { visible id:"ofrReduktionAbs" }
      click    id:"ofrReduktionAbs"
      find(id:"ofrReduktionAbs").sendKeys(fixture.ofrReduktionAbs+"\t")
    }
  ]

  def erfasseTileZuordnung = {

    select css:"#ofrCampaigns select.asmSelect",
           value:fixture.ofrCampaigns

    verify { present css:"#tl_zuordnung li.tileOk" }

    }

  def erfasseTileControlling = {

    select id:"M__offer__gs_ofr_header.aktkat",
           value:fixture.ofrAktkat

    type   id:"M__offer__gs_controlling.umspln",
           value:fixture.controlling.Umspln.toString()

    find(id:"M__offer__gs_controlling.mengepln").sendKeys(fixture.controlling.Mengepln.toString()+"\t")

    if (fixture.controlling.containsKey("Rabattsatz")) {
      click id:"ofrControllingRabattsatz"
      find(id:"ofrControllingRabattsatz").sendKeys(fixture.controlling.Rabattsatz.toString()+"\t")
    }

    verify { present css:"#tl_controlling li.tileOk" }

    }

    def autocomp_selector = [
      "01":".autocomp__product",
      "03":".autocomp__bossnr"
    ]

  def erfasseSortiment = {

    // Erweiterte Sicht (ohne "tile Rechts")
    click   id:"panelToggler"
    waitFor { ! visible( css:".tileRe" ) }

    // Auf Angebots-Tile clicken
    click   css:"#tl_angebot > ul > li.tileText"
    waitFor { present css:"#divTableSortplanung td.dataTables_empty" }

    fixture.sortiment.eachWithIndex { row, i ->

      // Neue Zeile hinzufügen
      click   css:"#divTableSortplanung div.top button.hinzufuegen"

      def row_selector = "#ofr_sortplanung tbody :nth-child(" + (i+1) + ")"

      waitFor { present css:row_selector }

      select  css:row_selector + " .PDA select",
              value:row.pda

      waitFor { present css:row_selector + " " + autocomp_selector[row.pda]  }

      type    css:row_selector +  " " + autocomp_selector[row.pda],
              value: row.nummer + "\t"

      waitFor { text( css:row_selector + " .BEZEICHNUNG" ) == row.expected_text }

    }

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

    def offerNr = ( find(id:"msg").getText( ) =~ /(\d+)/ )[0][1]

    println "Angebot ${ fixture.ofrNameKT } angelegt: ${ offerNr }"

    }

}