package capstone.kookmin.sksss.test2;

import android.os.Message;
import android.util.Log;

import static capstone.kookmin.sksss.test2.SoftKeyboard.MSG_AUTO_CORRECTION_RECEIVE;
import static capstone.kookmin.sksss.test2.SoftKeyboard.MSG_AUTO_SPACING_RECEIVE;
import static capstone.kookmin.sksss.test2.SoftKeyboard.MSG_REQUEST_RECEIVE;

/**
 * Created by sksss on 2017-12-06.
 */

public class AutoFunction implements Runnable{
    private static final int FIELD_EDITING_DELAY = 1000; //텍스트 입력 마침 시간 설정
    private static final int NETWORK_DELAY_CORRECTION = 2000; //서버 데이터 전송에 과부하를 막기위한 네트워크 딜레이(자동 오타수정, 띄어쓰기 기능 수행시)
//    private static final int NETWORK_DELAY_SPACING = 2000; //서버 데이터 전송에 과부하를 막기위한 네트워크 딜레이(자동 오타수정, 띄어쓰기 기능 수행시)
    private long lastEditingTime = 0;
    private long lastAutoCorrectionTime = 0;
    private long currentTime;
    private boolean isAutoCorrection = false;
    private boolean isAutoSpacing = false;
    private boolean fieldEditingFlag = false;
//    boolean renewLastEditingTime = true;
//    boolean renewLastAutoCorrectionTime = true;
    private MessegeHandler messegeHandler;
    private Message autoSpacingMessage;
    private Message autoCorrectionMessage;

    @Override
    public void run() {
        while(true) {
//            Log.d("AutoFunction", "run1");

            currentTime = System.currentTimeMillis();
            //필드 에딧팅 끝나면 fieldEditingFlag를 false로 설정
            if (isFinishTextFieldEditing() && fieldEditingFlag) {
//                Log.d("AutoFunction", "run2");
                if (isAutoSpacing) {
                    Log.d("AutoFunction", "run3");
                    autoSpacingMessage = messegeHandler.obtainMessage(MSG_AUTO_SPACING_RECEIVE);
                    messegeHandler.sendMessage(autoSpacingMessage);
                }

                fieldEditingFlag = false;
            }

            if (isAutoCorrection && fieldEditingFlag && isAutoCorrectionTime()) {
                Log.d("AutoFunction", "run4");
                autoCorrectionMessage = messegeHandler.obtainMessage(MSG_AUTO_CORRECTION_RECEIVE);
                messegeHandler.sendMessage(autoCorrectionMessage);
                lastAutoCorrectionTime = currentTime;
            }
        }
    }

    public AutoFunction(MessegeHandler messegeHandler)
    {
        this.messegeHandler = messegeHandler;
    }

    public void renewStateByFieldEditing(){
        lastEditingTime = System.currentTimeMillis();
        fieldEditingFlag = true;
    }

    public void setIsAutoCorrection(boolean bool){
        isAutoCorrection = bool;
    }

    public boolean getIsAutoCorrection(){
        return isAutoCorrection;
    }

    public void setIsAutoSpacing(boolean bool){
        isAutoSpacing = bool;
    }

    public boolean getIsAutoSpacing(){
        return isAutoSpacing;
    }

    public void toggleIsAutoSpacing(){
        isAutoSpacing = !isAutoSpacing;
    }

    public void toggleIsAutoCorrection(){
        isAutoCorrection = !isAutoCorrection;
    }

    private boolean isFinishTextFieldEditing(){
        return ((currentTime - lastEditingTime) > FIELD_EDITING_DELAY);
    }

    private boolean isAutoCorrectionTime(){
        return currentTime - lastAutoCorrectionTime > NETWORK_DELAY_CORRECTION;
    }

//    private boolean isAutoSpacingTime(){
//        return currentTime - lastEditingTime > NETWORK_DELAY_SPACING;
//    }
}
