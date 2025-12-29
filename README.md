# 🛒 Hepsiburada E2E Test Automation Project

Bu proje, **Hepsiburada.com** web sitesi için geliştirilmiş, ölçeklenebilir ve sürdürülebilir bir uçtan uca (E2E) test otomasyon projesidir. **Java** dili, **Gauge** framework'ü ve **Selenium WebDriver** kullanılarak geliştirilmiştir.

## 🚀 Proje Hakkında
Proje, gerçek bir kullanıcının alışveriş deneyimini simüle eder. Page Object Model (POM) yapısına sadık kalınarak, elementler ve aksiyonlar birbirinden ayrıştırılmıştır. Veri odaklı (Data Driven) test yaklaşımı ile kullanıcı verileri dış kaynaklardan (JSON ve Environment Variables) yönetilmektedir.

### 🧪 Test Senaryosu
1.  Hepsiburada anasayfasına gidilir ve çerezler (Cookie) yönetilir.
2.  **Hibrit Giriş Modu:** Kullanıcı bilgileri güvenli bir şekilde Environment Variable veya JSON dosyasından okunarak giriş yapılır.
3.  Sepet kontrol edilir, doluysa akıllı temizleme (Smart Clean) mekanizması ile boşaltılır.
4.  Parametrik olarak belirlenen ürün (örn: "Bilgisayar") aranır.
5.  Arama sonuçlarından dinamik olarak belirlenen sıradaki (örn: 5. ürün) ürün seçilir.
6.  Ürün detay sayfasındaki isim hafızaya alınır ve sepete eklenir.
7.  Sepete gidilerek, eklenen ürün ile hafızadaki ürünün eşleşip eşleşmediği doğrulanır.

## 🛠️ Kullanılan Teknolojiler ve Araçlar
* **Dil:** Java (JDK 17+)
* **Framework:** [Gauge](https://gauge.org/) (BDD - Behavior Driven Development)
* **Web Driver:** Selenium WebDriver (v4.16+)
* **Build Tool:** Maven
* **Veri Yönetimi:** GSON (JSON Parsing)
* **Locator Yönetimi:** JSON tabanlı element yönetimi

## ⭐ Öne Çıkan Teknik Özellikler

* **🛡️ Hibrit Veri Yönetimi:** Hassas veriler (Şifre vb.) kod içinde saklanmaz. Environment Variable öncelikli olmak üzere JSON dosyasından okuma yapan hibrit bir yapı kurulmuştur.
* **🔄 Retry Mechanism (Tekrar Deneme):** `StaleElementReferenceException` gibi geçici hatalarda testi patlatmak yerine, belirli aralıklarla tekrar deneyen kararlı (stable) metotlar yazılmıştır.
* **⚡ JavaScript Executor Fallback:** Selenium'un standart tıklama yönteminin engellendiği durumlarda (Overlay, Popup vb.), otomatik olarak JavaScript ile müdahale eden akıllı tıklama fonksiyonları kullanılmıştır.
* **🧹 Akıllı Sepet Temizliği:** Sepetin boş olup olmadığını kontrol eden, doluysa temizleyen ve çıkan popup'ları otomatik yöneten dinamik bir yapı mevcuttur.
* **Wait Strategy:** `Thread.sleep` yerine `WebDriverWait` (Explicit Wait) kullanılarak test süresi optimize edilmiştir.

## 📂 Proje Yapısı
