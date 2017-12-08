package capstone.kookmin.sksss.test2;

import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import static capstone.kookmin.sksss.test2.SoftKeyboard.MSG_DICTIONARY_RECEIVE;

/**
 * Created by heo on 2017-12-07.
 */

public class ApiDictionary implements Runnable {

    private MessegeHandler messegeHandler;
    private Message dictionaryMessege;
    private String searchWord;

    public ApiDictionary(MessegeHandler messegeHandler){
        this.messegeHandler = messegeHandler;
    }

    public ApiDictionary(MessegeHandler messegeHandler, String word){
        this.messegeHandler = messegeHandler;
        this.searchWord = word;
    }

    @Override
    public void run() {
        String dictionaryContent = Apidictionary(searchWord);
        Log.d("ㅇㅂㅇ", dictionaryContent);
        dictionaryMessege = messegeHandler.obtainMessage(MSG_DICTIONARY_RECEIVE, dictionaryContent);
        messegeHandler.sendMessage(dictionaryMessege);
    }

    public static String Apidictionary(String word) {
        String Client_ID = "C9mX47eDWjeDFIxJzbm8";
        String Client_Secret = "Tncc1I_j0j";
        String result = "";

        try {
            String text = URLEncoder.encode(word, "UTF-8");
            URL url = new URL("https://openapi.naver.com/v1/search/encyc.json?query=" + text); //타입은 json이고 검색할 단어를 query로 입력

            URLConnection urlConn = url.openConnection(); //openConnection 해당 요청에 대해서 쓸 수 있는 connection 객체

            urlConn.setRequestProperty("X-Naver-Client-ID", Client_ID);
            urlConn.setRequestProperty("X-Naver-Client-Secret", Client_Secret);
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            String msg = null;
            Boolean link_flag = false;
            Boolean description_flag = false;
            int link_start = 9;
            int description_start = 16;
            int end = 2;
            while ((msg = br.readLine()) != null) {
                if (msg.contains("link")) {
                    result += (msg.substring(link_start, msg.length() - end) + "\n");
                    link_flag = true;
                }
                if (msg.contains("description")) {
                    result += (msg.substring(description_start, msg.length() - end).replaceAll("<b>","").replaceAll("</b>","") + "\n");
                    description_flag = true;
                }
                if (link_flag == true && description_flag == true)
                    break;
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return result;
    }

    void setSearchWord(String word)
    {
        searchWord = word;
    }
}
