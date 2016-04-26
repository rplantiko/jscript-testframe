import org.junit.*;
import org.openqa.selenium.*

import java.text.SimpleDateFormat;
import java.time.*
import java.time.format.DateTimeFormatter;

/**
 * Run it with
 * cd c:/dev
 * groovy.bat "S:\Projekte\IT Retail\06 Retail Technik\10 - SAP Entwicklung\45 Fabrizio Lazzaretti\selenium\eso_aus_create\selenium_eso_aus_create.groovy"
 *
 * @author Fabrizio Lazzaretti
 * @create 2.2016
 */

public class SeleniumEsoAusCreate {

    @Delegate private SeleniumHelper selenium;

    public WebDriver driver;
    public String baseURL;
    protected String belnr;

    @Before
    public void setUp() throws Exception {
        baseURL = "https://da3.migros.net";
        selenium = new SeleniumHelper(baseURL);

        //selenium.keepBrowserOnQuit = true; // Für Tests (cmd.exe mit Task Manager löschen!)

    }

    @Test
    public void testSeleniumEsoAusCreate() throws Exception {
        //login
        login("auth_da3_tst_ectr_cm.html");//"tst_ectr_cm","ectr2014"
        startKachel("ka01")
        //create
        ausschreibungCreate();
        ausschreibungFillHead();
        //add lief
        ausschreibungAddLief();
        ausschreibungSave(false);
        //add pos
        ausschreibungNavigateToPos();
        ausschreibungEdit();
        ausschreibungAddPos();
        ausschreibungSave(true);
        //freigeben
        ausschreibungEdit();
        ausschreibungRelase();
        //Ausschreibung auf läuft setzen
        ausschreibungSetRun();
  }

    protected void ausschreibungSetRun(){
        driver.get(baseURL + "/zeso/tests?a=run_job&belnr="+belnr+"&set_start_past=X");
    }

    protected void login(String auth){
        start("/sap(bD1kZSZjPTUyMA==)/bc/bsp/sap/zectr_start/main.do",auth);
        this.driver = selenium.driver;
    }

    protected void startKachel(String kachel){
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            try { if (isElementPresent(By.id(kachel))) break; } catch (Exception e) {}
            Thread.sleep(1000);
        }

        driver.findElement(By.id(kachel)).click();
    }

    protected void ausschreibungCreate(){
        driver.findElement(By.id("btn_CREATE")).click();
    }

    protected void ausschreibungEdit(){
        waitFor { present id:"btn_EDIT" }
        driver.findElement(By.id("btn_EDIT")).click();
    }

    protected void ausschreibungRelase(){
        waitFor { present id:"btn_RELEASE" }
        click   id: "btn_RELEASE"
        dialogOk();
    }

    protected void ausschreibungFillHead(){
        belnr = find(id:"head_belnr").getAttribute("value");
        println belnr;
        driver.findElement(By.id("head_name")).clear();
//        type
        driver.findElement(By.id("head_name")).sendKeys("DOB HOB ACCESSOIRES: Selenium Test");

        //date
//        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(20);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        //date from
        driver.findElement(By.id("head_StartDate_from")).click();
        //Leer lassen -> wird automatisch gefüllt
//        type id: "head_StartDate_from", value: startDate.format(dateFormat);
//        driver.findElement(By.cssSelector(".ui-state-highlight")).click();
        //anderes feld anklicken, damit der datapicker verschwindet
        driver.findElement(By.id("head_starttim")).click();

        //date to
        waitFor { present css: "#head_StartDate_to"}
        driver.findElement(By.id("head_StartDate_to")).click();
        type id: "head_StartDate_to", value: endDate.format(dateFormat);
//        driver.findElement(By.cssSelector(".ui-state-highlight")).click();
        //anderes feld anklicken, damit der datapicker verschwindet
        driver.findElement(By.id("head_endtim")).click();

        //sourcing
        driver.findElement(By.id("button_addsor")).click();
        //new Select(driver.findElement(By.id("sornr_0001"))).selectByValue("8000000000");
        select  id: "sornr_0001",
                value: "8000000000"

//        new Select(driver.findElement(By.id("soransnam_0001"))).selectByValue("TST_ECTR_HK");

        select  id: "soransnam_0001",
                value: "TST_ECTR_HK"

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            try { if ("hans.muster@mgb.ch".equals(driver.findElement(By.id("soransnam_0001txt")).getAttribute("value"))) break; } catch (Exception e) {}
            Thread.sleep(1000);
        }
    }

    /**
     * Only working in edit mode -> because of wait
     */
    protected void ausschreibungSelectPanelLief(){
        driver.findElement(By.cssSelector("#tl_single_lief > ul > li.tileText")).click();
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            try { if (isElementPresent(By.id("button_addlif"))) break; } catch (Exception e) {}
            Thread.sleep(1000);
        }
    }

    protected void ausschreibungAddLief(){
        ausschreibungSelectPanelLief();

        driver.findElement(By.id("button_addlif")).click();
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            try { if (isElementPresent(By.id("lifnr_0001"))) break; } catch (Exception e) {}
            Thread.sleep(1000);
        }

        driver.findElement(By.id("lifnr_0001")).click();
        driver.findElement(By.id("lifnr_0001")).clear();
        driver.findElement(By.id("lifnr_0001")).sendKeys("10001027");


//        -->Lieferanten eintippen und panel neu laden
        ausschreibungSelectPanelLief();

        //new Select(driver.findElement(By.id("ansnam_0001"))).selectByValue("TST_ECT_LIF1");
        select  id: "ansnam_0001",
                value: "TST_ECT_LIF1"
    }


    protected void ausschreibungSave(boolean warningPossible){
        driver.findElement(By.id("btn_SAVE")).click();
        boolean warning = false;
        if(warningPossible){
            for (int second = 0;; second++) {
                Thread.sleep(1000);
                if (second >= 30){
                    //keine Warnung aufgetreten
                    break;
                }
                try {
                    if (present( css: "#message .warning_icon")){
                        click css: ".ui-dialog .ui-dialog-buttonset button"
                        warning = true;
                        break;
                    }
                }catch (Exception e) {}
            }
        }
        if(!warning){
            dialogOk();
        }
    }

    protected void dialogOk(){
        verify { present css:".ui-dialog .success_icon" }
        driver.findElement(By.cssSelector(".ui-dialog button.ui-button.ui-state-default")).click();
    }

    protected void ausschreibungNavigateToPos(){
        driver.findElement(By.cssSelector("#tl_single_pos > ul > li.tileText")).click();
    }

    protected void ausschreibungAddPos(){
        driver.findElement(By.id("button_addpos")).click();
        driver.findElement(By.id("matnr_0001")).click();
        driver.findElement(By.id("matnr_0001")).clear();
        driver.findElement(By.id("matnr_0001")).sendKeys("804500800000");

//        Autocomp macht nur Probleme - one auswahl vortfahren
//        for (int second = 0;; second++) {
//            if (second >= 60) fail("timeout");
//            try { if (isElementPresent(By.cssSelector("ul.ui-autocomplete > li:first"))) break; } catch (Exception e) {}
//            Thread.sleep(1000);
//        }
//        driver.findElement(By.cssSelector("ul.ui-autocomplete > li:first")).click();
    }

  @After
  public void tearDown() throws Exception {
      selenium.finish( );
  }

  private boolean isElementPresent(By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }
}
