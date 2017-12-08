package capstone.kookmin.sksss.test2;

import android.os.Message;
import android.renderscript.RenderScript;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static capstone.kookmin.sksss.test2.SoftKeyboard.MSG_TRANSLATE_RECEIVE;

/**
 * Created by user on 2017-12-04.
 */

// 네이버 Papago NMT API

public class APItranslate implements Runnable{

    private String rowText;
    private MessegeHandler messegeHandler;
    private int translateFlag;
    private Message translateMessege;
    private final static String ENGLISH = "en";
    private final static String KOREAN = "ko";
    public final static int TRAN_KO_TO_EN = 0;
    public final static int TRAN_EN_TO_KO = 1;

    public APItranslate(MessegeHandler messegeHandler){
        this.messegeHandler = messegeHandler;
    }

    public APItranslate(MessegeHandler messegeHandler, String text, int flag){
        this.messegeHandler = messegeHandler;
        this.rowText = text;
        this.translateFlag = flag;
    }

    @Override
    public void run() {
        String tranlatText = doTranlate(rowText,translateFlag);
        translateMessege = messegeHandler.obtainMessage(MSG_TRANSLATE_RECEIVE, tranlatText);
        messegeHandler.sendMessage(translateMessege);
    }

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

    public void setRowText(String text){
        this.rowText = text;
    }

    public void setTranslateFlag(int flag){
        this.translateFlag = flag;
    }

    private String doTranlate(String rowText, int translateFlag){
        switch (translateFlag){
            case TRAN_EN_TO_KO:
                return APItranslate(rowText, ENGLISH, KOREAN);
            case TRAN_KO_TO_EN:
                return APItranslate(rowText, KOREAN, ENGLISH);
        }
        return "ERROR";
    }
}