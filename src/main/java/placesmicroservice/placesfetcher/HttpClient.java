package placesmicroservice.placesfetcher;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class HttpClient {
    public String getRequest(String requestUrl) {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream (conn.getOutputStream());
            wr.close();

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
                resp.append("\r");
            }
            reader.close();

            return resp.toString();

        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
