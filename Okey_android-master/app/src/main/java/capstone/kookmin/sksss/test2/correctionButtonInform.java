package capstone.kookmin.sksss.test2;

/**
 * Created by sksss on 2017-04-17.
 */

//수정 버튼에 관한 정보가 담긴 클래스
public class correctionButtonInform {

    private String oldWord;
    private String correctionWord;

    correctionButtonInform(String oldWord, String correctionWord)
    {
        this.oldWord = oldWord;
        this.correctionWord = correctionWord;
    }


    public String getOldWord(){
        return  oldWord;
    }

    public String getCorrectionWord(){
        return correctionWord;
    }
}
