package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.openqa.selenium.By;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ElementHelper {

    private static ConcurrentHashMap<String, ElementInfo> elementMapList;

    // Sınıf oluşturulduğunda dosyayı okumayı başlat
    public ElementHelper() {
        initMap(getFileList());
    }

    // JSON dosyasını okuyan metod
    private void initMap(List<ElementInfo> elementInfoList) {
        elementMapList = new ConcurrentHashMap<>();
        for (ElementInfo elementInfo : elementInfoList) {
            elementMapList.put(elementInfo.getKey(), elementInfo);
        }
    }

    // Dosyayı bulup listeye çeviren metod
    private List<ElementInfo> getFileList() {
        Type listType = new TypeToken<List<ElementInfo>>() {}.getType();
        try {
            return new Gson().fromJson(new FileReader("src/test/resources/elements.json"), listType);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("❌ HATA: elements.json dosyası bulunamadı! Yolunu kontrol et.", e);
        }
    }

    // EN ÖNEMLİ METOD: Key verince By (Locator) döndüren kısım
    public static By getElementInfoToBy(String key) {
        ElementInfo elementInfo = elementMapList.get(key);

        if (elementInfo == null) {
            throw new RuntimeException("❌ HATA: Aradığın '" + key + "' anahtarı JSON dosyasında yok!");
        }

        String type = elementInfo.getType();
        String value = elementInfo.getValue();

        switch (type) {
            case "css": return By.cssSelector(value);
            case "xpath": return By.xpath(value);
            case "id": return By.id(value);
            case "name": return By.name(value);
            case "linkText": return By.linkText(value);
            default: throw new RuntimeException("Desteklenmeyen locator tipi: " + type);
        }
    }
}