package capstone.kookmin.sksss.test2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by heo on 2017-12-07.
 */

public class ApiDictionary {

    public static String Apidictionary(String word) throws Exception{
        String Client_ID = "C9mX47eDWjeDFIxJzbm8";
        String Client_Secret = "Tncc1I_j0j";

        URL url = new URL("https://openapi.naver.com/v1/search/encyc.json?query="+word); //타입은 json이고 검색할 단어를 query로 입력

        URLConnection urlConn=url.openConnection(); //openConnection 해당 요청에 대해서 쓸 수 있는 connection 객체

        urlConn.setRequestProperty("X-Naver-Client-ID", Client_ID);
        urlConn.setRequestProperty("X-Naver-Client-Secret", Client_Secret);
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

        String msg = null;
        String result="";
        Boolean link_flag=false;
        Boolean description_flag=false;
        while((msg = br.readLine())!=null)
        {
            if(msg.contains("link"))
            {
                result+=(msg.substring(9,msg.length()-2)+"\n");
                link_flag=true;
            }
            if(msg.contains("description"))
            {
                result+=(msg.substring(16,msg.length()-2)+"\n");
                description_flag=true;
            }
            if(link_flag==true&&description_flag==true)
                break;
        }
        return result;
    }

    public static void main(String[] args) throws Exception{
        System.out.println(ApiDictionary.Apidictionary("cat"));
    }
}
