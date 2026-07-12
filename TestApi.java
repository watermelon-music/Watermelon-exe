import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class TestApi {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://de1.api.radio-browser.info/json/stations/topclick/10");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "WatermelonMusicApp/1.0");
            conn.connect();
            Scanner scanner = new Scanner(url.openStream());
            String response = scanner.useDelimiter("\\Z").next();
            System.out.println(response);
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
