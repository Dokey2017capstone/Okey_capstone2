package capstone.kookmin.sksss.test2;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by sksss on 2017-04-10.
 */

//서버에서 받은 메시지를 처리하기 위한 핸들러
public class MessegeHandler extends Handler {

    private final WeakReference<SoftKeyboard> messageService;
    public MessegeHandler(SoftKeyboard service){
        messageService = new WeakReference<SoftKeyboard>(service);
    }

    @Override
    public void handleMessage(Message message){
        SoftKeyboard keyboard = messageService.get();
        if(keyboard != null)
        {
            keyboard.handleMessage(message);
        }
    }
}
