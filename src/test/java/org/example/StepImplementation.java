package org.example;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepImplementation {

    private WebDriver driver;
    private WebDriverWait wait;
    private static String hafizadakiUrunAdi = "";

    // --- 1. HAZIRLIK (HAYALET MODU & POPUP ENGELLEME) ---
    @BeforeScenario
    public void hazirlik() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        // 1. Google Şifre Kaydet Balonunu Kapatma Ayarı
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        // 2. Bot Olduğunu Gizleme
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--disable-blink-features=AutomationControlled");

        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        new ElementHelper(); // JSON okuyucuyu başlat
    }
    @Step("Kullanici <userKey> bilgileriyle giris yap (JSON)")
    public void kullaniciGirisiYapJson(String userKey) {

        System.out.println("📂 JSON dosyasından '" + userKey + "' kullanıcısı aranıyor...");

        // 1. JSON'dan bilgileri çek
        UserInfo user = UserHelper.getUser(userKey);

        String email = user.getEmail();
        String sifre = user.getPassword();

        System.out.println("🔐 Bilgiler bulundu -> Mail: " + email);

        // 2. SAYFA KONTROLÜ (Giriş sayfasında mıyız?)
        boolean loginSayfasindaMiyiz = !driver.findElements(getElementByKey("Email_Kutusu")).isEmpty();

        if (!loginSayfasindaMiyiz) {
            try {
                // Ana sayfadaysak menüye git
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,0);");
                WebElement girisMenu = wait.until(ExpectedConditions.visibilityOfElementLocated(getElementByKey("Giris_Menusu")));
                new Actions(driver).moveToElement(girisMenu).perform();
                wait.until(ExpectedConditions.elementToBeClickable(getElementByKey("Giris_Yap_Link"))).click();
            } catch (Exception e) {
                System.out.println("⚠️ Menüden gidilemedi, direkt giriş linkine gidiliyor...");
                // GÜNCEL LİNK BURASI:
                driver.get("https://giris.hepsiburada.com");
            }
        }

        // 3. EMAİL YAZ
        WebElement emailKutusu = wait.until(ExpectedConditions.visibilityOfElementLocated(getElementByKey("Email_Kutusu")));
        emailKutusu.clear();
        emailKutusu.sendKeys(email);

        // Giriş Yap butonu kontrolü
        try {
            if (driver.findElements(getElementByKey("Sifre_Kutusu")).isEmpty() || !driver.findElement(getElementByKey("Sifre_Kutusu")).isDisplayed()) {
                driver.findElement(getElementByKey("Giris_Yap_Butonu")).click();
            }
        } catch (Exception e) {}

        // 4. ŞİFRE YAZ
        WebElement sifreKutusu = wait.until(ExpectedConditions.visibilityOfElementLocated(getElementByKey("Sifre_Kutusu")));
        sifreKutusu.click();
        sifreKutusu.sendKeys(sifre);
        sifreKutusu.sendKeys(Keys.ENTER);

        System.out.println("✅ Giriş işlemi tamamlandı.");
        sabitBekle(5);
    }
    // --- 2. ELEMENT SÖZLÜĞÜ ---
    public By getElementByKey(String key) {
        return ElementHelper.getElementInfoToBy(key);
    }

    // --- 3. GENEL KOMUTLAR ---

    @Step("Url <url> adresine git")
    public void urlGit(String url) {
        driver.get(url);
        System.out.println("🌍 Siteye gidildi: " + url);
    }

    @Step("Element <key> gorunur olana kadar bekle")
    public void bekleElement(String key) {
        By locator = getElementByKey(key);
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        System.out.println("👀 Element görüldü: " + key);
    }

    @Step("Element <key> uzerine gel (Hover yap)")
    public void hoverYap(String key) {
        WebElement element = driver.findElement(getElementByKey(key));
        new Actions(driver).moveToElement(element).perform();
        System.out.println("🖱️ Mouse üzerine geldi: " + key);
        sabitBekle(1);
    }

    @Step("Sepeti tamamen bosalt")
    public void sepetiBosalt() {
        System.out.println("🧹 Sepet kontrol ediliyor...");
        driver.get("https://checkout.hepsiburada.com/sepetim");
        sabitBekle(2);

        // 1. KONTROL: Sepet zaten boş mu?
        try {
            if (driver.findElements(getElementByKey("Sepet_Bos_Mesaji")).size() > 0) {
                System.out.println("✅ Sepet zaten boş, temizliğe gerek yok.");
                driver.get("https://www.hepsiburada.com");
                return;
            }
        } catch (Exception e) {}

        // 2. KONTROL: Sepet doluysa temizle
        int maxDongu = 15;
        int sayac = 0;

        while (sayac < maxDongu) {
            List<WebElement> silButonlari = driver.findElements(getElementByKey("Sepet_Sil_Butonu"));

            if (silButonlari.isEmpty()) {
                System.out.println("✅ Sepet tamamen temizlendi.");
                break;
            }

            try {
                // Çöp kutusuna tıkla
                WebElement silBtn = silButonlari.get(0);
                silBtn.click();
                System.out.println("🗑️ Çöp kutusuna basıldı.");

                sabitBekle(1); // Popup'ın açılması için minik bir bekleme

                // --- YENİ POPUP KONTROLÜ BAŞLANGICI ---
                try {
                    // Eğer "Tümünü Sil" butonu çıktıysa ona bas
                    List<WebElement> popuplar = driver.findElements(getElementByKey("Tumunu_Sil_Popup_Butonu"));
                    if (!popuplar.isEmpty()) {
                        popuplar.get(0).click();
                        System.out.println("✅ Onay Popup'ı çıktı ve 'Tümünü sil'e basıldı.");
                        sabitBekle(1); // Popup kapansın diye bekleme
                    }
                } catch (Exception popErr) {
                    System.out.println("ℹ️ Popup çıkmadı, normal silme devam ediyor.");
                }
                // --- POPUP KONTROLÜ SONU ---

                sabitBekle(1); // Sayfa yenilenmesi için
            } catch (Exception e) {
                System.out.println("⚠️ Silme sırasında geçici hata: " + e.getMessage());
            }
            sayac++;
        }

        driver.get("https://www.hepsiburada.com");
        System.out.println("🏠 Ana sayfaya dönüldü, teste hazır.");
    }

    @Step("Element <key> tikla")
    public void tikla(String key) {
        By locator = getElementByKey(key);
        try {
            // 1. Normal Tıklama Denemesi
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
            System.out.println("✅ Tıklandı: " + key);
        } catch (Exception e) {
            System.out.println("⚠️ Normal tıklanamadı, JS ile deneniyor: " + key);
            try {
                WebElement element = driver.findElement(locator);
                JavascriptExecutor js = (JavascriptExecutor) driver;

                // Elementi ortaya hizala
                js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);

                // Üst menünün (Sticky Header) altında kalmasın diye sayfayı biraz yukarı kaydır (Element aşağı iner)
                js.executeScript("window.scrollBy(0, -150)");

                sabitBekle(1); // Kaydırma animasyonu tamamlansın diye bekleme

                // JS ile zorla tıkla
                js.executeScript("arguments[0].click();", element);

                System.out.println("✅ JS ile tıklandı: " + key);
            } catch (Exception ex) {
                System.out.println("❌ Hata! Tıklama başarısız: " + key);
                // Testi burada patlatıyoruz ki "Passed" sanıp bizi kandırmasın
                throw new RuntimeException("Elemente tıklanamadı: " + key);
            }
        }
    }

    @Step("Element <key> alanina <text> degerini yaz")
    public void metinYaz(String key, String text) {
        By locator = getElementByKey(key);
        // ÖNCE BEKLE: Element görünür olana kadar bekle (En önemli kısım burası)
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear(); // Varsa eski yazıyı sil
        element.sendKeys(text);
        System.out.println("⌨️ Yazıldı: " + text);
    }

    // --- GÜNCELLENMİŞ VE GÜÇLENDİRİLMİŞ ARAMA METODU ---
    @Step("Element <key> alanina <text> yaz ve Enter'a bas")
    public void metinYazVeEnter(String key, String text) {
        // StaleElementReferenceException hatasını önlemek için Retry (Tekrar Deneme) mekanizması
        int denemeSayisi = 0;
        while (denemeSayisi < 3) {
            try {
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(getElementByKey(key)));

                // Önce tıklayalım ki focus olsun
                try {
                    element.click();
                } catch (Exception e) {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("arguments[0].click();", element);
                }

                element.sendKeys(text);
                element.sendKeys(Keys.ENTER);
                System.out.println("🔍 Aratıldı: " + text);
                break; // Başarılı olursa döngüden çık

            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ Element bayatladı (Sayfa yenilendi), tekrar deneniyor... Deneme: " + (denemeSayisi+1));
                denemeSayisi++;
                sabitBekle(2); // Sayfanın oturması için biraz bekle
            } catch (Exception e) {
                System.out.println("❌ Hata oluştu: " + e.getMessage());
                break;
            }
        }
    }
    @Step("Urun detay sayfasindaki urun ismini hafizaya al")
    public void urunIsminiHafizayaAl() {
        System.out.println("💾 Ürün ismi hafızaya alınıyor...");

        try {
            // KRİTİK DÜZELTME: Element görünür olana kadar bekle!
            WebElement baslikElementi = wait.until(ExpectedConditions.visibilityOfElementLocated(getElementByKey("Urun_Detay_Basligi")));

            // İsmi al, boşlukları temizle
            hafizadakiUrunAdi = baslikElementi.getText().trim();
            System.out.println("💾 Hafızaya Alınan Ürün: " + hafizadakiUrunAdi);

        } catch (Exception e) {
            // Eğer başlığı bulamazsa, sayfa yüklenememiş veya sekme değişememiş demektir.
            System.out.println("❌ HATA: Ürün detay sayfasına geçilemedi veya başlık bulunamadı.");
            throw new RuntimeException("Ürün ismi alınamadı. Hata: " + e.getMessage());
        }
    }
    @Step("Listeden <sira>. urunu sec ve tikla")
    public void urunSec(int sira) {
        List<WebElement> urunler = driver.findElements(getElementByKey("Urun_Listesi"));
        if(urunler.size() >= sira) {
            WebElement urun = urunler.get(sira - 1);
            new Actions(driver).scrollToElement(urun).perform();
            sabitBekle(1);
            urun.click();
            for(String winHandle : driver.getWindowHandles()){
                driver.switchTo().window(winHandle);
            }
            System.out.println("🖱️ " + sira + ". ürüne tıklandı.");
        } else {
            System.out.println("❌ Yeterli ürün yok!");
        }
    }

    @Step("Sepet kontrolu ve urun dogrulamasi yap")
    public void sepetKontrol() {
        System.out.println("🛒 Sepete gidiliyor...");
        driver.get("https://checkout.hepsiburada.com/sepetim");
        sabitBekle(4);

        List<WebElement> sepetUrunleri = driver.findElements(getElementByKey("Sepet_Urun_Ismi"));

        // 1. Kontrol: Sepet boş mu?
        if (sepetUrunleri.isEmpty()) {
            throw new RuntimeException("❌ HATA: Sepette hiç ürün yok!");
        }

        // Sepetteki ürünün ismini al
        String sepettekiUrunAdi = sepetUrunleri.get(0).getText().trim();
        System.out.println("🛒 Sepetteki Ürün: " + sepettekiUrunAdi);

        // 2. Kontrol: İsimler Eşleşiyor mu?
        // Not: Küçük harfe çevirip (toLowerCase) ve "contains" (içeriyor mu) kullanarak kıyaslamak en güvenlisidir.
        // Çünkü bazen detay sayfasında isim çok uzundur, sepette kısaltılır.

        if (!sepettekiUrunAdi.toLowerCase().contains(hafizadakiUrunAdi.toLowerCase()) &&
                !hafizadakiUrunAdi.toLowerCase().contains(sepettekiUrunAdi.toLowerCase())) {

            System.out.println("❌ İSİM UYUŞMAZLIĞI!");
            System.out.println("Beklenen (Hafızadaki): " + hafizadakiUrunAdi);
            System.out.println("Bulunan (Sepetteki): " + sepettekiUrunAdi);

            throw new RuntimeException("❌ HATA: Sepetteki ürün, eklenen ürünle aynı değil!");
        }

        System.out.println("✅ DOĞRULAMA BAŞARILI! Doğru ürün sepette.");
    }

    @Step("<saniye> saniye bekle")
    public void sabitBekle(int saniye) {
        try {
            Thread.sleep(saniye * 1000);
        } catch (InterruptedException e) {}
    }

    @AfterScenario
    public void kapat() {
        if (driver != null) driver.quit();
    }
}