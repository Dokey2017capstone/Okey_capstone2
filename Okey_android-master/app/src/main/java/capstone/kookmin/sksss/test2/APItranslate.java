package capstone.kookmin.sksss.test2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by user on 2017-12-04.
 */

// 네이버 Papago NMT API

public class APItranslate {
    public static String APItranslate(CharSequence inputtext, String sourcetext, String targettext) {

        String clientId = "18WZck0vzX6RMMjqLajD";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "K6I8tDaj75";//애플리케이션 클라이언트 시크릿값";

        try {

            String text = URLEncoder.encode(inputtext.toString(), "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            // post request
            String postParams = "source=" + sourcetext + "&" + "target=" + targettext + "&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();


            String tempoutput=response.toString();
            String output="";
            int index=tempoutput.indexOf("translatedText")+17;

            output=tempoutput.substring(index,tempoutput.length()-4);

            return output;
        } catch (Exception e) {
            System.out.println(e);
            return "";
        }
    }


}