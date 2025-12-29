package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserHelper {

    // Kullanıcıları hafızada tutacağımız yer (Map)
    private static final Map<String, UserInfo> userMap = new ConcurrentHashMap<>();

    // Sınıf ilk çalıştığında dosyayı bir kere okusun
    static {
        initUserInfo();
    }

    private static void initUserInfo() {
        try {
            // Dosya yolunu belirle
            String filePath = System.getProperty("user.dir") + "/src/test/resources/users.json";

            // Gson kütüphanesi ile oku
            Gson gson = new Gson();
            Type listType = new TypeToken<List<UserInfo>>(){}.getType();
            List<UserInfo> userInfoList = gson.fromJson(new FileReader(filePath), listType);

            // Listeyi Map'e çevir (Hızlı arama yapmak için)
            for (UserInfo user : userInfoList) {
                userMap.put(user.getKey(), user);
            }
            System.out.println("✅ Kullanıcı bilgileri JSON dosyasından yüklendi.");

        } catch (FileNotFoundException e) {
            System.out.println("❌ HATA: users.json dosyası bulunamadı! Yol: " + e.getMessage());
        }
    }

    // Dışarıdan çağıracağımız metod bu
    public static UserInfo getUser(String key) {
        if (!userMap.containsKey(key)) {
            throw new RuntimeException("❌ HATA: JSON dosyasında '" + key + "' anahtarına sahip kullanıcı bulunamadı!");
        }
        return userMap.get(key);
    }
}