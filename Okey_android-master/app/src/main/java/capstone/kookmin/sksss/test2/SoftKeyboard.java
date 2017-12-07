/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Some Hangul InputMethod Code added by www.kandroid.org
 * 
 */

package capstone.kookmin.sksss.test2;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SoftKeyboard extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on 
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;
    static final int MAX_TEXT = 10000;  //최대 텍스트 길이

    //서버에서 받은 데이터 종류에 대한 메시지 핸들러
    static final int MSG_REQUEST_RECEIVE = 3;
    static final int MSG_AUTO_CORRECTION_RECEIVE = 1;
    static final int MSG_AUTO_SPACING_RECEIVE = 2;
    //옵션창으로 전환할 keycode
    static final int CODE_OPTION_VIEW = -8;
    static final int CODE_AUTO_CORRECTION = -9;
    static final int CODE_AUTO_SPACING = -10;
    static final int CODE_SYMBOL_KEYBOARD = -7;
    static final int CODE_DICTIONARY_SEARCH = -11;

    private KeyboardView mInputView;
//    private CompletionInfo[] mCompletions;

    //수정 가능한 String
    private StringBuilder mComposing = new StringBuilder();
    private List<correctionButtonInform> correctionButtonInformList = new ArrayList<correctionButtonInform>();    //단어 수정 버튼 클릭시 수행되는 과정에서 필요한 정보를 담은 클래스
//    private List<int[]> correctionTextPosition = new ArrayList<int[]>();

//    private boolean isAutoCorrect = false;
//    private boolean isAutoSpacing = false;
    //현재 키보드
    private Keyboard mCurrentKeyboard;
    private boolean mPredictionOn;
//    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;

    private Keyboard mSymbolsKeyboard;
    private Keyboard mSymbolsShiftedKeyboard;
    private Keyboard mSymbolsEnKeyboard;
    private Keyboard mSymbolsEnShiftedKeyboard;
    private Keyboard mQwertyKeyboard;
    private Keyboard mOptionKeyboard;

    private Keyboard mHangulKeyboard; // Hangul Code
    private Keyboard mHangulShiftedKeyboard; // Hangul Code

    private Keyboard mCurKeyboard;

    private String mWordSeparators;

    //서버 관련
    private static final String ip = "18.217.186.238";
    private static final int port = 8100;
    private MessegeHandler messegeHandler = new MessegeHandler(this);
    private TcpClient tcp = new TcpClient(this, ip, port, messegeHandler);
    Thread tcpThread;
    private AutoFunction autoFunction = new AutoFunction(messegeHandler);
    Thread autoFunctionThread = null;
//    private static int NETWORK_DELAY = 2000; //서버 데이터 전송에 과부하를 막기위한 네트워크 딜레이(자동 오타수정, 띄어쓰기 기능 수행시)
//    private long oldSendTime;
    //자동완성 및 DB 관련 -*-
    DatabaseHandler databaseH;
    String[] item = new String[]{"please Search.."};
    //end -*-

    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override public void onCreate() {
        super.onCreate();
        mWordSeparators = getResources().getString(R.string.word_separators);
        //test code임 지우기 X
//        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //
//        String[] a = new String[3];
//        a[0] = "천";
//        a[1] = "하하";
//        a[2] = "ㅋㅋㅋ";
//        String[] b = new String[2];
//        b[0] = "ㅎ";
//        b[1] = "zz";
//
//        correctionButtonInformList.add(new correctionButtonInform(1,3,"나는",a));
//        correctionButtonInformList.add(new correctionButtonInform(2,4,"나는",a));
//        correctionButtonInformList.add(new correctionButtonInform(3,5,"나는",a));
//        correctionButtonInformList.add(new correctionButtonInform(3,5,"크크",b));
//        correctionButtonInformList.add(new correctionButtonInform(4,6,"크크",b));
        //
        mCurrentKeyboard = mQwertyKeyboard;

        //DB Source -*-
        try {
            InputStreamReader inputreader = new InputStreamReader(this.getAssets().open("dic.csv"), "euc-kr");
            BufferedReader buffereader = new BufferedReader(inputreader);
            List<String> list = new ArrayList<String>();
            String line;

            do {
                line = buffereader.readLine();
                list.add(line);
            } while(line!=null);

            databaseH = new DatabaseHandler(this);

            databaseH.insert(list);

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //DB Source end -*-
    }

    //-*-DB
    public String[] getItemsFromDb(String searchTerm) {

        List<MyObject> products = databaseH.read(searchTerm);

        int rowCount = products.size();
        String[] item = new String[rowCount];
        int x = 0;

        for (MyObject record : products) {

            item[x] = record.objectName;
            x++;
        }

        return item;
    }
    //-*-

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    //UI초기화 부분, 생성 or 구성변경후 호출, 수정X
    @Override public void onInitializeInterface() {
        if (mQwertyKeyboard != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            //기존의 키보드가 바뀐 경우, 또한 넓이가 다르면 다시 빌드해야함(가로세로?)
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mQwertyKeyboard = new Keyboard(this, R.xml.qwerty);
        mSymbolsKeyboard = new Keyboard(this, R.xml.symbols);
        mSymbolsEnKeyboard = new Keyboard(this, R.xml.symbols_en);
        mSymbolsShiftedKeyboard = new Keyboard(this, R.xml.symbols_shift);
        mSymbolsEnShiftedKeyboard = new Keyboard(this, R.xml.symbols_en_shift);
        mHangulKeyboard = new HangulKeyboard(this, R.xml.hangul);
        mHangulShiftedKeyboard = new HangulKeyboard(this, R.xml.hangul_shift);
        mOptionKeyboard = new Keyboard(this, R.xml.option);
    }

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    //키보드 디스플레이시 호출(똑같), 수정x
    @Override public View onCreateInputView() {
        mInputView = (KeyboardView) getLayoutInflater().inflate(
                R.layout.input, null);
        mInputView.setOnKeyboardActionListener(this);
        mInputView.setKeyboard(mQwertyKeyboard);
        mInputView.setPreviewEnabled(false);
        return mInputView;
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    //자동완성, 오타수정 뷰 관련 변수
    View completionWordBar, correctionWordBar, correctionwordpopupLayout;
    LayoutInflater candidateViewLayoutInflater;
    Button[] completaionButton = new Button[3];
    Button[] correctionButton = new Button[3];  //수정 버튼
    int popUpPosition; //현재 클릭한 오타 수정 버튼

    //자동완성/오타수정 뷰 관련 클릭 리스너
    Button.OnClickListener mOnButtonClickListner = new View.OnClickListener(){

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            Button button = (Button)view;
//            Keyboard currentKeyboard = mInputView.getKeyboard();
            switch (view.getId()){
                //테스튼가?
                    ///////////
//                    test.setLength(0);
//                    test.append("a");
//                    test2.setLength(0);
//                    test2.append("c");
//                    CorrectionInfo a =  new CorrectionInfo(2,"a","b");
////                    getCurrentInputConnection().beginBatchEdit();
////                    getCurrentInputConnection().setSelection(2,3);
////                    getCurrentInputConnection().commitText("a",1);
////                    getCurrentInputConnection().endBatchEdit();
//                    //getCurrentInputConnection().commitText("a",0);
//                    Log.d("boolean?",String.valueOf(s));
//                    Log.d(a.getOldText().toString(),a.getNewText().toString());
                    ///////////
                case R.id.cor2ComChangeButton:
                    setCandidatesView(completionWordBar);
                    break;
                //자동완성 클릭
                case R.id.completionWord1:case R.id.completionWord2:case R.id.completionWord3:
                    changeOldWord2CompletionWord(button.getText()+" ");
                    break;
                //오타단어 클릭
                case R.id.correctionWord1:case R.id.correctionWord2:case R.id.correctionWord3:
                    popupCorrectionPopupWindow(view);
                    break;
                //오타수정 바 혹은 갱신 버튼 클릭시
                case R.id.com2CorChangeButton:
                    setCandidatesView(correctionWordBar);
                case R.id.renewCorrectionWord:
                    //서버로 보낼 오타 수정 메시지 작성 및 송신
                    connectAndSendCorrectionJson();
                    //-*-test
                    //Log.d("isTestGood?","???????????????????????");
                    //String jsonTest = "{\"response\" : [\"spacing\"], \"spacing\" : \"나는 학교를 갑니다.\"}"; //"{\"response\" : [\"modified\"], \"modified\" : {\"정말루\" : [\"0\",\"정말로\"], \"하르\" : [\"2\",\"하루\"] }}";
                    //processJsonMessegeTest(jsonTest);
                    //-*-testend
                    break;
            }
        }
    };

    //현재 단어를 자동완성단어로 커밋하는 함수
    private void changeOldWord2CompletionWord(String changeWord)
    {
        //composing의 경우 단어 자동완성
        if (mComposing.length() > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText(changeWord,1);
        }
        //composing이 아닌 경우 단어 자동완성
        else{
            setText(completionOldWordInform.getStartPos(),completionOldWordInform.getWordLength(),changeWord);
        }

        if (isHangulKeyboardCurrent()) {
            clearHangul();
        }
    }

    private boolean isHangulKeyboardCurrent()
    {
        return mInputView.getKeyboard() == mHangulKeyboard || mInputView.getKeyboard() == mHangulShiftedKeyboard;
    }
    private boolean isSymbolHangulKeyboardCurrent()
    {
        return mInputView.getKeyboard() == mSymbolsKeyboard || mInputView.getKeyboard() == mSymbolsShiftedKeyboard;
    }
    private boolean isSymbolEnglishKeyboardCurrent()
    {
        return mInputView.getKeyboard() == mSymbolsEnKeyboard || mInputView.getKeyboard() == mSymbolsEnShiftedKeyboard;
    }

    private boolean isClickedHangulModeAsQwerty(int primaryCode)
    {
        return (mInputView.getKeyboard() == mQwertyKeyboard && primaryCode == Keyboard.KEYCODE_MODE_CHANGE);
    }
    private boolean isClickedHangulModeAsSymbol(int primaryCode)
    {
        return ((mInputView.getKeyboard() == mSymbolsKeyboard || mInputView.getKeyboard() == mSymbolsShiftedKeyboard)
                && primaryCode == Keyboard.KEYCODE_MODE_CHANGE);
    }
    private boolean isClickedSymbolModeAsHangul(int primaryCode)
    {
        return ((mInputView.getKeyboard() == mHangulKeyboard || mInputView.getKeyboard() == mHangulShiftedKeyboard)
                && primaryCode == CODE_SYMBOL_KEYBOARD);
    }
    private boolean isClickedSymbolModeAsEnglish(int primaryCode)
    {
        return (mInputView.getKeyboard() == mQwertyKeyboard&& primaryCode == CODE_SYMBOL_KEYBOARD);
    }
    private boolean isClickedQwertyModeAsHangul(int primaryCode)
    {
        return ((mInputView.getKeyboard() == mHangulKeyboard || mInputView.getKeyboard() == mHangulShiftedKeyboard)
                && primaryCode == Keyboard.KEYCODE_MODE_CHANGE);
    }

    private PopupWindow dropdown;
    private String focusOldWordbyPopup; // old word 식별 (mOnPopclick2를 위함)
    TextView correctionWordTextView;
    TextView noCorrectTextView;
    private PopupWindow popupCorrectionPopupWindow(View oldTextButton){
        initatePopupWindow();

        switch(oldTextButton.getId())
        {
            case R.id.correctionWord1:
                popUpPosition = 0;
                break;
            case R.id.correctionWord2:
                popUpPosition = 1;
                break;
            default:
                popUpPosition = 2;
        }

        if(isOldWordInCorrectionButtonInformList())
        {
            //수정단어를 채움
            correctionWordTextView.setText(correctionButtonInformList.get(popUpPosition).getCorrectionWord());
            //수정 취소 버튼
            noCorrectTextView.setText("no correct");
            dropdown.showAsDropDown((Button)oldTextButton, 3, 3);
        }

        focusOldWordbyPopup = ((Button) oldTextButton).getText().toString(); //Old word 식별
        return dropdown;
    }

    private void initatePopupWindow(){
        correctionwordpopupLayout = candidateViewLayoutInflater.inflate(R.layout.correctionwordpopup, null);
        correctionWordTextView = (TextView)correctionwordpopupLayout.findViewById(R.id.correctionPopup1);
        noCorrectTextView = (TextView)correctionwordpopupLayout.findViewById(R.id.correctionPopup2);

        correctionwordpopupLayout.measure(View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED);
        dropdown = new PopupWindow(correctionwordpopupLayout, 380, 250);
        dropdown.setOutsideTouchable(true);
        dropdown.setBackgroundDrawable(new BitmapDrawable());
        Drawable background = getResources().getDrawable(R.drawable.popuplayout_bg);
        dropdown.setBackgroundDrawable(background);
        correctionWordTextView.setOnClickListener(mOnTextViewClickListner);
        noCorrectTextView.setOnClickListener(mOnTextViewClickListner);
        correctionWordTextView.setText("");
    }

    private boolean isOldWordInCorrectionButtonInformList(){
        return correctionButtonInformList.size()>popUpPosition;
    }

    TextView.OnClickListener mOnTextViewClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            InputConnection inputConnection = getCurrentInputConnection();
            switch (view.getId()) {
                //팝업에서 수정단어 클릭시..
                case R.id.correctionPopup1:
                    dropdown.dismiss();
                    processTextCorrection(view);
                    correctionButtonInformList.remove(popUpPosition);
                    renewCorrectionButtonsAsCorrectionButtonInformList();
                    break;
                case R.id.correctionPopup2:
                    dropdown.dismiss();
                    correctionButtonInformList.remove(popUpPosition);
                    renewCorrectionButtonsAsCorrectionButtonInformList();
                    break;
            }
        }
    };

    private void processTextCorrection(View view){

        InputConnection inputConnection = getCurrentInputConnection();

        //수정 위치와 수정될 단어의 길이를 구함
        if (!isTextEmpty()) {
            //현재 텍스트와 수정 단어 추출
            String currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text.toString();
            String correctionWord = ((TextView)view).getText().toString();
            //매칭 알고리즘
            Pattern word = Pattern.compile(focusOldWordbyPopup);
            Matcher matchString = word.matcher(currentText);

            if (matchString.find()) {
                if (mComposing.length() > 0) {
                    commitTyped(inputConnection);
                }
                setText(matchString.start(), focusOldWordbyPopup.length(), correctionWord);
            } else
                makeCorrectFailToast();

        } else
            makeCorrectFailToast();
    }

    private void makeCorrectFailToast(){
        Toast.makeText(this.getApplicationContext() , "수정 실패 : 이미 텍스트가 바뀌었습니다.", Toast.LENGTH_SHORT).show();
    }

    //자동완성 버튼 수정
    void setCompletionButton(String[] completionWordList)
    {
        for(int i=0; i < completaionButton.length; i++)
        {
            if(i<completionWordList.length)
                completaionButton[i].setText(completionWordList[i]);
            else
                completaionButton[i].setText("");
        }
    }

    //자동완성 갱신
    void updateCompletionButtons()
    {
        setCompletionButton(getItemsFromDb(completionOldWordInform.getOldWord()));
    }

    //candidateVIew관련 뷰와 이벤트 함수 연결
    @Override public View onCreateCandidatesView() {
        candidateViewLayoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        completionWordBar = candidateViewLayoutInflater.inflate(R.layout.completionwordbar, null);
        correctionWordBar = candidateViewLayoutInflater.inflate(R.layout.correctionwordbar, null);
        Button com2CorChangeButton = (Button) completionWordBar.findViewById(R.id.com2CorChangeButton);
        Button cor2ComChangeButton = (Button) correctionWordBar.findViewById(R.id.cor2ComChangeButton);
        Button renewCorrectionWordButton = (Button) correctionWordBar.findViewById(R.id.renewCorrectionWord);
        completaionButton[0] = (Button) completionWordBar.findViewById(R.id.completionWord1);
        completaionButton[1] = (Button) completionWordBar.findViewById(R.id.completionWord2);
        completaionButton[2] = (Button) completionWordBar.findViewById(R.id.completionWord3);
        correctionButton[0] = (Button) correctionWordBar.findViewById(R.id.correctionWord1);
        correctionButton[1] = (Button) correctionWordBar.findViewById(R.id.correctionWord2);
        correctionButton[2] = (Button) correctionWordBar.findViewById(R.id.correctionWord3);

        for(int i = 0; i< completaionButton.length; i++)
            completaionButton[i].setOnClickListener(mOnButtonClickListner);
        for(int i = 0; i< correctionButton.length; i++)
            correctionButton[i].setOnClickListener(mOnButtonClickListner);

        com2CorChangeButton.setOnClickListener(mOnButtonClickListner);
        cor2ComChangeButton.setOnClickListener(mOnButtonClickListner);
        renewCorrectionWordButton.setOnClickListener(mOnButtonClickListner);
        setCandidatesViewShown(true);
        return completionWordBar;
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    //텍스트에 대한 입력 준비(입력 텍스트 보일시), 예제 함수
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        clearHangul();

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);

        //쉬프트 클리어
        if (!restarting) {
            mMetaState = 0;
        }

        mPredictionOn = true;

        // We are now going to initialize our state based on the type of
        // text being edited.
        //편집중인 텍스트 유형에 따라 초기화
        switch (attribute.inputType&EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                //숫자와 날짜는 기호 키보드로 기본 설정되어 있으며 추가 기능은 없습니다.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case EditorInfo.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                //전화기는 기호 키보드로 기본 설정되지만 전용 전화 키보드가 필요할 수도 있습니다.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case EditorInfo.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                //이것은 일반적인 텍스트 편집입니다. 우리는 정상적인 알파벳 키보드를 기본값으로 사용하고 예상 텍스트 (사용자 유형으로 후보를 표시)를 수행해야한다고 가정합니다.
                if(mCurrentKeyboard==null)
                    mCurrentKeyboard=mQwertyKeyboard;
                mCurKeyboard = mCurrentKeyboard;
                mPredictionOn = true;

                // We now look for a few special variations of text that will
                // modify our behavior.
                //이제 우리의 행동을 수정하는 몇 가지 특수한 텍스트 변형을 찾습니다.
                int variation = attribute.inputType &  EditorInfo.TYPE_MASK_VARIATION;
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    //사용자가 암호를 입력 할 때 입력하는 내용 / 예측을 표시하지 않습니다.
                    mPredictionOn = false;
                }

                if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_URI
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    //우리의 예측은 전자 메일 주소 나 URI에 유용하지 않습니다.

                    mPredictionOn = false;
                }

                if ((attribute.inputType&EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    //이것이 자동 완성 텍스트보기 인 경우 예측이 표시되지 않고 대신 편집기가 자체 텍스트를 제공 할 수 있도록 허용합니다.
                    //우리는 전체 화면 모드 일 때만 편집기 후보를 보여 주며, 그렇지 않은 경우 자체 UI를 표시합니다.
                }

                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                //에디터의 현재 상태를 살펴보고 알파벳 키보드를 시프트 아웃할지 여부를 결정하기를 원합니다.
                updateShiftKeyState(attribute);
                break;

            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                //모든 알 수없는 입력 유형의 경우, 특수 기능이없는 알파벳 키보드를 기본값으로 사용하십시오.
                mCurKeyboard = mQwertyKeyboard;
                updateShiftKeyState(attribute);
        }

        // Update the label on the enter key, depending on what the application
        // says it will do.
    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    //유저가 필드의 에딧팅을 끝내면 불려짐. 이를 이용하여 상태를 리셋할수있다.
    @Override public void onFinishInput() {
        super.onFinishInput();
        mComposing.setLength(0);
        //updateCandidates();

        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        //사용자가 창 하단에 텍스트를 입력하는 경우 기본 응용 프로그램을 팝업 혹은 팝다운을
        //피하기 위해 특정 편집기에서 입력을 마칠 때만 후보자 창을 숨 깁니다.

        //키보드 뷰 닫기
        mCurKeyboard = mQwertyKeyboard;
        if (mInputView != null) {
            mInputView.closing();
        }
    }

    //편집기에서 입력 시작시 호출
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        //선택한 키보드를 입력보기에 적용하십시오.
        mInputView.setKeyboard(mCurKeyboard);
        mInputView.closing();
    }

    private void setAutoFunctionThreadCheckAndRun(){
        if(isAutoFunctionNotRun()) {
            Log.d("softKeyboard","is작동???");
            autoFunctionThreadRun(autoFunction);
        }
    }

    private boolean isAutoFunctionNotRun(){
        return (autoFunctionThread == null || autoFunctionThread.getState() == Thread.State.TERMINATED || autoFunctionThread.getState() == Thread.State.BLOCKED);
    }

    @Override
    public void onComputeInsets(InputMethodService.Insets outInsets){
        super.onComputeInsets(outInsets);
        if(!isFullscreenMode())
        {
            outInsets.contentTopInsets = outInsets.visibleTopInsets;
        }
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    //커서의 업데이트, 완료
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                            int newSelStart, int newSelEnd,
                                            int candidatesStart, int candidatesEnd) {

        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        InputConnection ic = getCurrentInputConnection();

        Log.i("Hangul", "onUpdateSelection :"
                + Integer.toString(oldSelStart) + ":"
                + Integer.toString(oldSelEnd) + ":"
                + Integer.toString(newSelStart) + ":"
                + Integer.toString(newSelEnd) + ":"
                + Integer.toString(candidatesStart) + ":"
                + Integer.toString(candidatesEnd)
        );

        updateCompletionOldWordInformAsCursur(newSelStart, newSelEnd);

        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        // 텍스트 뷰에서 현재 selection이 변경되면, 우리가 가진 모든 텍스트를 지워야한다, 예제코드 부분
//        Keyboard currentKeyboard = mInputView.getKeyboard();
//        if (currentKeyboard != mHangulKeyboard && currentKeyboard != mHangulShiftedKeyboard) {
//            if (mComposing.length() > 0 && (newSelStart != candidatesEnd
//                    || newSelEnd != candidatesEnd)) {
//                mComposing.setLength(0);
//                if (ic != null) {
//                    ic.finishComposingText();
//                }
//            }
//        }
        //한글키보드의 경우
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                    || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
//                Log.d("dz","4");
            clearHangul();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    //inputMethod가 디스플레이되기 원하는 자동완성 후보자를 보고할떄 호출됨
    //자동완성 리스트 적용인듯?
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
//        if (mCompletionOn) {
//            mCompletions = completions;
//            if (completions == null) {
//                //setSuggestions(null, false, false);
//                return;
//            }
//
//            List<String> stringList = new ArrayList<String>();
//            for (int i=0; i<(completions != null ? completions.length : 0); i++) {
//                CompletionInfo ci = completions[i];
//                if (ci != null) stringList.add(ci.getText().toString());
//            }
//            setSuggestions(stringList, true, true);
//        }
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    //하드 키 이벤트를 InputConnection의 편집 작업으로 변환합니다. PROCESS_HARD_KEYS 옵션을 사용할 때만 필요합니다.
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        boolean dead = false;

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() -1 );
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length()-1);
            }
        }

        onKey(c, null);

        return true;
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    //앱에 전달되는 주요 이벤트를 모니터함(키 다운 이벤트)
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("Hangul", "onKeyDown :" + Integer.toString(keyCode));

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                // 백키 지만 팝업창을 먼저 닫아야 하는 경우??
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    }
                }
                break;

            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                //삭제 키의 특수 처리 : 현재 사용자의 텍스트를 작성중인 경우 응용 프로그램에 삭제를 허용하는 대신 해당 텍스트를 수정하려고합니다.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                // 기본 텍스트편집기가 알아서 처리
                return false;

            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                //하드키의 다른경우
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        //바보 같은 예 : 우리의 입력 방법에서, Alt + Space는 소문자에서 'android'의 지름길입니다.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            //먼저 우리가 이것을 사용하고 있기 때문에 더 이상 shift 상태에 있지 않다는 것을 에디터에게 알리십시오.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    //어플에 전달되는 주요 이벤트를 모니터함.
    //첫번째 crack을 얻고, 재개하거나 app에 계속되게함
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        //하드 키보드로 입력된 텍스트의 변형을 원할 경우 추적중인 메타 키 상태를 업데이트하기 위해 업 이벤트를 처리해야함
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    //editor로 작성중인 텍스트를 커밋하는 도우미 함수
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            //텍스트 입력
            inputConnection.finishComposingText();
            mComposing.setLength(0);
            //isCommitted=true;
            //updateCandidates();
            clearHangul();
        }
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    //초기 editor상태를 기반으로 키보드의 shift상태를 업데이트하는 helper
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    //주어진 문자 코드가 알파벳인지 판단하는 도우미.
    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    //도우미가 키 다운 / 키 업 쌍을 현재 에디터로 보냅니다.
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    /**
     * Helper to send a character to the editor as raw key events.
     */
    // 에디터에 문자를 raw key events로 보내는 도우미.
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    //onKey 리스너
    public void onKey(int primaryCode, int[] keyCodes) {
        Log.i("Hangul", "onKey PrimaryCode[" + Integer.toString(primaryCode)+"]");

        //wordSeparator(\u0020.,;:!?\n()[]*&amp;@{}/&lt;&gt;_+=|&quot;)인 경우
        if (isWordSeparator(primaryCode)) {
            processWordSeparatorCase(primaryCode);
        }
        else if(primaryCode == Keyboard.KEYCODE_CANCEL){
            handleClose();
            return;
        }
        else if(isModeChangeKey(primaryCode)){
            processModeChangeCase(primaryCode);
        }
        else if(isHardKey(primaryCode)){
            processHardKeyCase(primaryCode);
        }
        //일반 문자의 경우
        else {
            processTextCase(primaryCode, keyCodes);
        }

        if(mComposing.length()>0) {
            updateCompletionWordAsComposing();
            updateCompletionButtons();// 후보자 추천 설정
        }
//        Log.d("current keyboard",mInputView.getKeyboard().toString());
    }

    private void processWordSeparatorCase(int primaryCode){

        if (mComposing.length() > 0) {
            commitTyped(getCurrentInputConnection());
        }
        if (isHangulKeyboardCurrent()) {
            clearHangul();
        }

        sendKey(primaryCode);
        updateShiftKeyState(getCurrentInputEditorInfo());
        //서버 데이터 전송 과부하를 막기 위한 delay 삽입
//        delaySendForAutoFunction();
        autoFunction.renewStateByFieldEditing();
    }

    final int AUTO_CORRECTION_BUTTON = 0;
    final int AUTO_SPACING_BUTTON = 1;
    private void processHardKeyCase(int primaryCode){
        Keyboard currentKeyboard = mInputView.getKeyboard();
        switch(primaryCode)
        {
            case Keyboard.KEYCODE_DELETE:
                if (isHangulKeyboardCurrent()) {
                    hangulSendKey(-2,HCURSOR_NONE);
                }
                else {
                    handleBackspace();
                }
                break;
            case Keyboard.KEYCODE_SHIFT:
                handleShift();
                break;
            case CODE_OPTION_VIEW:
                if(currentKeyboard != mOptionKeyboard) {
                    if (mComposing.length() > 0) {
                        getCurrentInputConnection().commitText(mComposing, 1);//mComposing.length());
                        mComposing.setLength(0);
                    }
                    clearHangul();
                    //mCurrentKeyboard = currentKeyboard;
                    //currentKeyboard = mOptionKeyboard;
                    setAutoFunctionThreadCheckAndRun();
                    mInputView.setKeyboard(mOptionKeyboard);
                    mInputView.getKeyboard().getKeys().get(AUTO_CORRECTION_BUTTON).on = autoFunction.getIsAutoCorrection();//isAutoCorrect;
                    mInputView.getKeyboard().getKeys().get(AUTO_SPACING_BUTTON).on = autoFunction.getIsAutoSpacing();//isAutoSpacing;
                }
                else {
                    //currentKeyboard = mCurrentKeyboard;
                    mInputView.setKeyboard(mCurrentKeyboard);
                }
                break;
            case CODE_AUTO_CORRECTION:
//                isAutoCorrect = !isAutoCorrect;
                autoFunction.toggleIsAutoCorrection();
                break;
            case CODE_AUTO_SPACING:
//                isAutoSpacing = !isAutoSpacing;
                autoFunction.toggleIsAutoSpacing();
                break;
            case CODE_DICTIONARY_SEARCH:

                break;
        }
    }

    private String dictionarySearchForSelectedText(){
        return ApiDictionary.Apidictionary(getCurrentInputConnection().getSelectedText(0).toString());
    }

    private boolean isHardKey(int primaryCode){
        switch (primaryCode){
            case Keyboard.KEYCODE_DELETE:
            case Keyboard.KEYCODE_SHIFT:
            case CODE_OPTION_VIEW:
            case CODE_AUTO_CORRECTION:
            case CODE_AUTO_SPACING:
                return true;
            default:
                return false;
        }
    }

    private void processModeChangeCase(int primaryCode){
        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (isSymbolEnglishKeyboardCurrent()) {
            currentKeyboard = mQwertyKeyboard;
        }
        // Hangul Start Code
        else if (isClickedHangulModeAsQwerty(primaryCode) || isSymbolHangulKeyboardCurrent()) {
//            if (mComposing.length() > 0) {
//                commitTyped(getCurrentInputConnection());
//            }
//            clearHangul();
            currentKeyboard = mHangulKeyboard;
        }
        else if(isClickedSymbolModeAsEnglish(primaryCode)){
//            if (mComposing.length() > 0) {
//                commitTyped(getCurrentInputConnection());
//            }
            currentKeyboard = mSymbolsEnKeyboard;
        }
        //now hangulKeyboard
        else{
            if (mComposing.length() > 0) {
                getCurrentInputConnection().commitText(mComposing, 1);//mComposing.length());
                mComposing.setLength(0);
            }
            clearHangul();

            if(isClickedQwertyModeAsHangul(primaryCode))
                currentKeyboard = mQwertyKeyboard;
            else
                currentKeyboard = mSymbolsKeyboard;
        }
        mInputView.setKeyboard(currentKeyboard);
//        if (currentKeyboard == mSymbolsKeyboard || currentKeyboard == mSymbolsEnKeyboard || currentKeyboard == mHangulKeyboard || currentKeyboard == mQwertyKeyboard) {
            currentKeyboard.setShifted(false);
//        }

        mCurrentKeyboard = mInputView.getKeyboard();
    }

    private boolean isModeChangeKey(int primaryCode){
        return (primaryCode == Keyboard.KEYCODE_MODE_CHANGE || primaryCode == CODE_SYMBOL_KEYBOARD);
    }

    private void processTextCase(int primaryCode, int[] keyCodes){
        // Hangul Start Code
        if (isHangulKeyboardCurrent()) {
            handleHangul(primaryCode, keyCodes);
        }
        else {
            handleCharacter(primaryCode, keyCodes);
        }
        // Hangul End Code
//        delaySendForAutoFunction();
        autoFunction.renewStateByFieldEditing();
    }

    //자동 오타수정 및 띄어쓰기
    //서버 데이터 전송 과부하를 막기 위한 delay 삽입
//    private void delaySendForAutoFunction(){
//        long newSendTime = System.currentTimeMillis();
//        if(isAutoCorrect && newSendTime-oldSendTime>NETWORK_DELAY) {
//            connectAndSendCorrectionJson();
//            oldSendTime = newSendTime;
//        }
//    }

    //예제코드
    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
//    private void updateCandidates() {
//        if (!mCompletionOn) {
//            if (mComposing.length() > 0) {
//                ArrayList<String> list = new ArrayList<String>();
//                list.add(mComposing.toString());
//                setSuggestions(list, true, true);
//            } else {
//                setSuggestions(null, false, false);
//            }
//        }
//    }

//    //자동 완성 설정
//    public void setSuggestions(List<String> suggestions, boolean completions,
//                               boolean typedWordValid) {
//        if (suggestions != null && suggestions.size() > 0) {
//            setCandidatesViewShown(true);
//        } else if (isExtractViewShown()) {
//            setCandidatesViewShown(true);
//        }
//    }

    //예제코드
    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            //updateCandidates();
            //isTextUpdate=true;
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            //updateCandidates();
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    //예제 코드
    private void handleShift() {
        if (mInputView == null) {
            return;
        }

        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (mQwertyKeyboard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        }
        // Hangul Code Start
        else if (currentKeyboard == mHangulKeyboard) {
            mHangulKeyboard.setShifted(true);
            mInputView.setKeyboard(mHangulShiftedKeyboard);
            mHangulShiftedKeyboard.setShifted(true);
            mHangulShiftState = 1;
        } else if (currentKeyboard == mHangulShiftedKeyboard) {
            mHangulShiftedKeyboard.setShifted(false);
            mInputView.setKeyboard(mHangulKeyboard);
            mHangulKeyboard.setShifted(false);
            mHangulShiftState = 0;
        }
        // Hangul Code End
        else if (currentKeyboard == mSymbolsKeyboard) {
            mSymbolsKeyboard.setShifted(true);
            mInputView.setKeyboard(mSymbolsShiftedKeyboard);
            mSymbolsShiftedKeyboard.setShifted(true);
        }else if (currentKeyboard == mSymbolsEnKeyboard) {
            mSymbolsEnKeyboard.setShifted(true);
            mInputView.setKeyboard(mSymbolsEnShiftedKeyboard);
            mSymbolsEnShiftedKeyboard.setShifted(true);
        }
        else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            mSymbolsShiftedKeyboard.setShifted(false);
            mInputView.setKeyboard(mSymbolsKeyboard);
            mSymbolsKeyboard.setShifted(false);
        }
        else if (currentKeyboard == mSymbolsEnShiftedKeyboard) {
            mSymbolsEnShiftedKeyboard.setShifted(false);
            mInputView.setKeyboard(mSymbolsEnKeyboard);
            mSymbolsEnKeyboard.setShifted(false);
        }
    }

// Hangul Code Start
    //예제코드
    private int isHangulKey(int stack_pos, int new_key) {
        if (stack_pos != 2) {
            switch (mHangulKeyStack[stack_pos]) {
                case 0:
                    if (new_key == 20) return 2;
                    break;
                case 3:
                    if (new_key == 23) return 4;
                    else if(new_key == 29) return 5;
                    break;
                case 8:
                    if (new_key == 0)return 9;
                    else if (new_key == 16) return 10;
                    else if (new_key == 17) return 11;
                    else if (new_key == 20) return 12;
                    else if (new_key == 27) return 13;
                    else if (new_key == 28) return 14;
                    else if (new_key == 29) return 15;
                    break;
                case 17:
                    if (new_key == 20) return 19;
                    break;
            }
        }
        else {
            switch (mHangulKeyStack[stack_pos]) {
                case 38:
                    if (new_key == 30) return 39;
                    else if (new_key == 31) return 40;
                    else if (new_key == 50) return 41;
                    break;
                case 43:
                    if (new_key == 34) return 44;
                    else if (new_key == 35) return 45;
                    else if (new_key == 50) return 46;
                    break;
                case 48:
                    if (new_key == 50) return 49;
                    break;
            }
        }
        return 0;
    }

    private static char HCURSOR_NONE = 0;
    private static char HCURSOR_NEW = 1;
    private static char HCURSOR_ADD = 2;
    private static char HCURSOR_UPDATE = 3;
    private static char HCURSOR_APPEND = 4;
    private static char HCURSOR_UPDATE_LAST = 5;
    private static char HCURSOR_DELETE_LAST = 6;
    private static char HCURSOR_DELETE = 7;


    //한글키보드의 경우, 커서가 업데이트 될때 해당 함수 내용을 실행할것인지 여부 (한글 자모 조합을 위해 setComposingText를 유지하기 위함)
    //자동완성단어로 교체될 단어의 시작/끝위치(길이 2) 배열
    private wordPositionInform completionOldWordInform = new wordPositionInform(0,0,null);
    //자동완성단어로 교체될 Old 단어
    private static int mHCursorState = HCURSOR_NONE;
    private int mHangulShiftState = 0;
    private int mHangulState = 0;
    private static int mHangulKeyStack[] = {0,0,0,0,0,0};
    private static int mHangulJamoStack[] = {0,0,0};
    final static int H_STATE_0 = 0;
    final static int H_STATE_1 = 1;
    final static int H_STATE_2 = 2;
    final static int H_STATE_3 = 3;
    final static int H_STATE_4 = 4;
    final static int H_STATE_5 = 5;
    final static int H_STATE_6 = 6;
    final static char[] h_chosung_idx =
            {0,1, 9,2,12,18,3, 4,5, 0, 6,7, 9,16,17,18,6, 7, 8, 9,9,10,11,12,13,14,15,16,17,18};

    final static char[] h_jongsung_idx =
            {0, 1, 2, 3,4,5, 6, 7, 0,8, 9,10,11,12,13,14,15,16,17,0 ,18,19,20,21,22,0 ,23,24,25,26,27};

    final static int[] e2h_map =
            {16,47,25,22,6, 8,29,38,32,34,30,50,48,43,31,35,17,0, 3,20,36,28,23,27,42,26,
                    16,47,25,22,7, 8,29,38,32,34,30,50,48,43,33,37,18,1, 3,21,36,28,24,27,42,26};

    private void clearHangul() {
        mHCursorState = HCURSOR_NONE;
        mHangulState = 0;
        mHangulKeyStack[0] = 0;
        mHangulKeyStack[1] = 0;
        mHangulKeyStack[2] = 0;
        mHangulKeyStack[3] = 0;
        mHangulKeyStack[4] = 0;
        mHangulKeyStack[5] = 0;
        mHangulJamoStack[0] = 0;
        mHangulJamoStack[1] = 0;
        mHangulJamoStack[2] = 0;
        Log.d("ggg","zzzzz");
        return;
    }

    //예제코드
    private void hangulSendKey(int newHangulChar, int hCursor) {

        if (hCursor == HCURSOR_NEW) {
            Log.i("Hangul", "HCURSOR_NEW");

            mComposing.append((char)newHangulChar);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            mHCursorState = HCURSOR_NEW;
        }
        else if (hCursor == HCURSOR_ADD) {
            mHCursorState = HCURSOR_ADD;
            Log.i("Hangul", "HCURSOR_ADD");

            mComposing.append((char)newHangulChar);
            Log.d("length",mComposing.toString());
            getCurrentInputConnection().setComposingText(mComposing, 1);
        }
        else if (hCursor == HCURSOR_UPDATE) {
            Log.i("Hangul", "HCURSOR_UPDATE");
            if(mComposing.length()>0)
                mComposing.setCharAt(mComposing.length()-1, (char)newHangulChar);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            mHCursorState = HCURSOR_UPDATE;
            Log.d("length", mComposing.toString());
        }
        else if (hCursor == HCURSOR_APPEND) {
            Log.i("Hangul", "HCURSOR_APPEND");
            mComposing.append((char)newHangulChar);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            mHCursorState = HCURSOR_APPEND;
        }
        else if (hCursor == HCURSOR_NONE) {
            if (newHangulChar == -1) {
                Log.i("Hangul", "HCURSOR_NONE [DEL -1]");
                keyDownUp(KeyEvent.KEYCODE_DEL);
                Log.d("dz","8");
                clearHangul();
            }
            else if (newHangulChar == -2) {
                int cho_idx,jung_idx,jong_idx;

                Log.i("Hangul", "HCURSOR_NONE [DEL -2]");

                switch(mHangulState) {
                    case H_STATE_0:
                        if(mComposing.length()>0) {
                            mComposing.delete(mComposing.length()-1,mComposing.length());
                            getCurrentInputConnection().setComposingText(mComposing, 1);
                        }
                        else{
                            getCurrentInputConnection().commitText("", 0);
                            clearHangul();
                            keyDownUp(KeyEvent.KEYCODE_DEL);
                        }
                        break;
                    case H_STATE_1: // �ʼ�
                        mComposing.setLength(mComposing.length()-1);//
                        getCurrentInputConnection().setComposingText(mComposing, 1);
                        Log.d("dz","7");
                        if(mComposing.length() == 0) {
                            getCurrentInputConnection().commitText("", 0);
                            clearHangul();
                        }
                        mHangulState = H_STATE_0;
                        break;
                    case H_STATE_2:
                        newHangulChar = 0x3131 + mHangulKeyStack[0];
                        hangulSendKey(newHangulChar, HCURSOR_UPDATE);
                        mHangulKeyStack[1] = 0;
                        mHangulJamoStack[0] = mHangulKeyStack[0];
                        mHangulState = H_STATE_1;
                        break;
                    case H_STATE_3:
                        if (mHangulKeyStack[3] == 0) {
                            mComposing.setLength(mComposing.length()-1);
                            getCurrentInputConnection().setComposingText(mComposing, 1);
                            Log.d("dz","6");
                            if(mComposing.length() == 0) {
                                getCurrentInputConnection().commitText("", 0);
                                clearHangul();
                            }
                            mHangulState = H_STATE_0;
                        }
                        else {
                            mHangulKeyStack[3] = 0;
                            newHangulChar = 0x314F + (mHangulKeyStack[2] - 30);
                            hangulSendKey(newHangulChar, HCURSOR_UPDATE);
                            mHangulJamoStack[1] = mHangulKeyStack[2];
                            mHangulState = H_STATE_3; // goto �߼�
                        }
                        break;
                    case H_STATE_4:
                        if (mHangulKeyStack[3] == 0) {
                            mHangulKeyStack[2] = 0;
                            mHangulJamoStack[1] = 0;
                            newHangulChar = 0x3131 + mHangulJamoStack[0];
                            hangulSendKey(newHangulChar, HCURSOR_UPDATE);
                            mHangulState = H_STATE_1; // goto �ʼ�
                        }
                        else {
                            mHangulJamoStack[1]= mHangulKeyStack[2];
                            mHangulKeyStack[3] = 0;
                            cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                            jung_idx = mHangulJamoStack[1] - 30;
                            jong_idx = h_jongsung_idx[mHangulJamoStack[2]];
                            newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                            hangulSendKey(newHangulChar, HCURSOR_UPDATE);
                        }
                        break;
                    case H_STATE_5:
                        mHangulJamoStack[2] = 0;
                        mHangulKeyStack[4] = 0;
                        cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                        jung_idx = mHangulJamoStack[1] - 30;
                        jong_idx = h_jongsung_idx[mHangulJamoStack[2]];
                        newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                        hangulSendKey(newHangulChar, HCURSOR_UPDATE);
                        mHangulState = H_STATE_4;
                        break;
                    case H_STATE_6:
                        mHangulKeyStack[5] = 0;
                        mHangulJamoStack[2] = mHangulKeyStack[4];
                        cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                        jung_idx = mHangulJamoStack[1] - 30;
                        jong_idx = h_jongsung_idx[mHangulJamoStack[2]+1];;
                        newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                        hangulSendKey(newHangulChar,HCURSOR_UPDATE);
                        mHangulState = H_STATE_5;
                        break;
                }
            }
            else if (newHangulChar == -3) {
                Log.i("Hangul", "HCURSOR_NONE [DEL -3]");
                final int length = mComposing.length();
                if (length > 1) {
                    mComposing.delete(length - 1, length);
                    //isTextUpdate=true;
                }
            }

        }
    }

    //예제코드
    private void handleHangul(int primaryCode, int[] keyCodes) {

        int hangulKeyIdx = -1;
        int newHangulChar;
        int cho_idx,jung_idx,jong_idx;
        int hangulChar = 0;

        //한글 범위 안
        if (primaryCode >= 0x61 && primaryCode <= 0x7A) {

            if (mHangulShiftState == 0) {
                hangulKeyIdx = e2h_map[primaryCode - 0x61];
            }
            else {
                hangulKeyIdx = e2h_map[primaryCode - 0x61 + 26];
                mHangulShiftedKeyboard.setShifted(false);
                mInputView.setKeyboard(mHangulKeyboard);
                mHangulKeyboard.setShifted(false);
                mHangulShiftState = 0;
            }
            hangulChar = 1;
        }
        else if (primaryCode >= 0x41 && primaryCode <= 0x5A) {
            hangulKeyIdx = e2h_map[primaryCode - 0x41 + 26];
            hangulChar = 1;
        }

        else {
            hangulChar = 0;
        }


        if (hangulChar == 1) {

            switch(mHangulState) {

                case H_STATE_0: // Hangul Clear State
                    Log.i("SoftKey", "HAN_STATE 0");
                    if (hangulKeyIdx < 30) { // if 자음
                        newHangulChar = 0x3131 + hangulKeyIdx;
                        hangulSendKey(newHangulChar, HCURSOR_NEW);  // 커밋(텍스트에 입력)
                        mHangulKeyStack[0] = hangulKeyIdx;
                        mHangulJamoStack[0] = hangulKeyIdx;
                        mHangulState = H_STATE_1; // goto �ʼ�
                    }
                    else { // if 모음
                        newHangulChar = 0x314F + (hangulKeyIdx - 30);
                        hangulSendKey(newHangulChar, HCURSOR_NEW);
                        mHangulKeyStack[2] = hangulKeyIdx;
                        mHangulJamoStack[1] = hangulKeyIdx;
                        mHangulState = H_STATE_3; // goto �߼�
                    }
                    break;

                case H_STATE_1: // 첫번째 입력을 자음으로 하고 두번째 입력일 경우
                    Log.i("SoftKey", "HAN_STATE 1");
                    if (hangulKeyIdx < 30) { // if 자음일 경우
                        int newHangulKeyIdx = isHangulKey(0,hangulKeyIdx);//자음 조합 확인
                        if (newHangulKeyIdx > 0) { // if 자음 조합이 있을 경우
                            newHangulChar = 0x3131 + newHangulKeyIdx;
                            mHangulKeyStack[1] = hangulKeyIdx;
                            mHangulJamoStack[0] = newHangulKeyIdx;
                            hangulSendKey(newHangulChar, HCURSOR_UPDATE);   //자음조합으로 커밋
                            mHangulState = H_STATE_2;
                        }
                        else { // 자음조합이 없을경우

                            // cursor error trick start
                            newHangulChar = 0x3131 + mHangulJamoStack[0];
                            hangulSendKey(newHangulChar,HCURSOR_UPDATE);    //선자음 업데이트
                            // trick end

                            newHangulChar = 0x3131 + hangulKeyIdx;
                            hangulSendKey(newHangulChar, HCURSOR_ADD);      //둘째자음 더하기(ADD로 이전자음 픽스)
                            mHangulKeyStack[0] = hangulKeyIdx;              //다시 시작
                            mHangulJamoStack[0] = hangulKeyIdx;
                            mHangulState = H_STATE_1;
                        }
                    }
                    else { // if 모음
                        mHangulKeyStack[2] = hangulKeyIdx;
                        mHangulJamoStack[1] = hangulKeyIdx;
                        cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                        jung_idx = mHangulJamoStack[1] - 30;
                        jong_idx = h_jongsung_idx[mHangulJamoStack[2]];
                        newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                        hangulSendKey(newHangulChar, HCURSOR_UPDATE);
                        mHangulState = H_STATE_4; // goto �ʼ�,�߼�
                    }
                    break;

                case H_STATE_2:
                    Log.i("SoftKey", "HAN_STATE 2");
                    if (hangulKeyIdx < 30) { // if 자음

                        // cursor error trick start
                        newHangulChar = 0x3131 + mHangulJamoStack[0];
                        hangulSendKey(newHangulChar,HCURSOR_UPDATE);
                        // trick end


                        mHangulKeyStack[0] = hangulKeyIdx;
                        mHangulJamoStack[0] = hangulKeyIdx;
                        mHangulJamoStack[1] = 0;
                        newHangulChar = 0x3131 + hangulKeyIdx;
                        hangulSendKey(newHangulChar, HCURSOR_ADD);
                        mHangulState = H_STATE_1; // goto �ʼ�
                    }
                    else { // if 모음
                        newHangulChar = 0x3131 + mHangulKeyStack[0];
                        mHangulKeyStack[0] = mHangulKeyStack[1];
                        mHangulJamoStack[0] = mHangulKeyStack[0];
                        mHangulKeyStack[1] = 0;
                        hangulSendKey(newHangulChar, HCURSOR_UPDATE);

                        mHangulKeyStack[2] = hangulKeyIdx;
                        mHangulJamoStack[1] = mHangulKeyStack[2];
                        cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                        jung_idx = mHangulJamoStack[1] - 30;
                        jong_idx = 0;

                        newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                        hangulSendKey(newHangulChar, HCURSOR_ADD);
                        mHangulState = H_STATE_4; // goto �ʼ�,�߼�
                    }
                    break;

                case H_STATE_3:
                    Log.i("SoftKey", "HAN_STATE 3");
                    if (hangulKeyIdx < 30) { // 자음

                        // cursor error trick start
                        newHangulChar = 0x314F + (mHangulJamoStack[1] - 30);
                        hangulSendKey(newHangulChar, HCURSOR_UPDATE);
                        // trick end

                        newHangulChar = 0x3131 + hangulKeyIdx;
                        hangulSendKey(newHangulChar, HCURSOR_ADD);
                        mHangulKeyStack[0] = hangulKeyIdx;
                        mHangulJamoStack[0] = hangulKeyIdx;
                        mHangulJamoStack[1] = 0;
                        mHangulState = H_STATE_1; // goto �ʼ�
                    }
                    else { // 모음
                        if (mHangulKeyStack[3] == 0) {
                            int newHangulKeyIdx = isHangulKey(2,hangulKeyIdx);  //모음조합확인
                            if (newHangulKeyIdx > 0) { // 모음 조합이 존재할 경우
                                //	                	hangulSendKey(-1);
                                newHangulChar = 0x314F + (newHangulKeyIdx - 30);
                                hangulSendKey(newHangulChar, HCURSOR_UPDATE);
                                mHangulKeyStack[3] = hangulKeyIdx;
                                mHangulJamoStack[1] = newHangulKeyIdx;
                            }
                            else { // 모음 조합이 존재안할 경우

                                // cursor error trick start
                                newHangulChar = 0x314F + (mHangulJamoStack[1] - 30);
                                hangulSendKey(newHangulChar, HCURSOR_UPDATE);
                                // trick end

                                newHangulChar = 0x314F + (hangulKeyIdx - 30);
                                hangulSendKey(newHangulChar,HCURSOR_ADD);
                                mHangulKeyStack[2] = hangulKeyIdx;
                                mHangulJamoStack[1] = hangulKeyIdx;
                            }
                        }
                        else {

                            // cursor error trick start
                            newHangulChar = 0x314F + (mHangulJamoStack[1] - 30);
                            hangulSendKey(newHangulChar, HCURSOR_UPDATE);
                            // trick end

                            newHangulChar = 0x314F + (hangulKeyIdx - 30);
                            hangulSendKey(newHangulChar,HCURSOR_ADD);
                            mHangulKeyStack[2] = hangulKeyIdx;
                            mHangulJamoStack[1] = hangulKeyIdx;
                            mHangulKeyStack[3] = 0;
                        }
                        mHangulState = H_STATE_3;
                    }
                    break;
                case H_STATE_4:
                    Log.i("SoftKey", "HAN_STATE 4");
                    if (hangulKeyIdx < 30 && hangulKeyIdx != 7 && hangulKeyIdx != 18 && hangulKeyIdx != 24) { // if 자음
                        mHangulKeyStack[4] = hangulKeyIdx;
                        mHangulJamoStack[2] = hangulKeyIdx;
                        cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                        jung_idx = mHangulJamoStack[1] - 30;
                        jong_idx = h_jongsung_idx[mHangulJamoStack[2]+1];
                        newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                        hangulSendKey(newHangulChar, HCURSOR_UPDATE);
                        mHangulState = H_STATE_5;
                    }
                    else if(hangulKeyIdx < 30)
                    {
                        // cursor error trick start
                        cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                        jung_idx = mHangulJamoStack[1] - 30;
                        newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28));
                        hangulSendKey(newHangulChar,HCURSOR_UPDATE);
                        // trick end


                        mHangulKeyStack[0] = hangulKeyIdx;
                        mHangulKeyStack[1] = 0;
                        mHangulKeyStack[2] = 0;
                        mHangulKeyStack[3] = 0;
                        mHangulKeyStack[4] = 0;
                        mHangulJamoStack[0] = hangulKeyIdx;
                        mHangulJamoStack[1] = 0;
                        mHangulJamoStack[2] = 0;
                        newHangulChar = 0x3131 + hangulKeyIdx;
                        hangulSendKey(newHangulChar,HCURSOR_ADD);
                        mHangulState = H_STATE_1;
                    }
                    else { // if 모음
                        if (mHangulKeyStack[3] == 0) {  //조합 모음이 아직 안나온 경우
                            int newHangulKeyIdx = isHangulKey(2,hangulKeyIdx);
                            if (newHangulKeyIdx > 0) { // if 모음조합이 된 경우
                                mHangulKeyStack[3] = hangulKeyIdx;
                                mHangulJamoStack[1] = newHangulKeyIdx;
                                cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                                jung_idx = mHangulJamoStack[1] - 30;
                                jong_idx = 0;
                                newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                                hangulSendKey(newHangulChar,HCURSOR_UPDATE);
                                mHangulState = H_STATE_4;
                            }
                            else { // 모음조합이 안되는 경우

                                // cursor error trick start
                                cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                                jung_idx = mHangulJamoStack[1] - 30;
                                jong_idx = 0;
                                newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                                hangulSendKey(newHangulChar,HCURSOR_UPDATE);
                                // trick end

                                newHangulChar = 0x314F + (hangulKeyIdx - 30);
                                hangulSendKey(newHangulChar,HCURSOR_ADD);
                                mHangulKeyStack[0] = 0;
                                mHangulKeyStack[1] = 0;
                                mHangulJamoStack[0] = 0;
                                mHangulKeyStack[2] = hangulKeyIdx;
                                mHangulJamoStack[1] = hangulKeyIdx;
                                mHangulState = H_STATE_3;
                            }
                        }
                        else {//조합모음이 이미 나온 경우

                            // cursor error trick start
                            cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                            jung_idx = mHangulJamoStack[1] - 30;
                            jong_idx = 0;
                            newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                            hangulSendKey(newHangulChar,HCURSOR_UPDATE);
                            // trick end


                            newHangulChar = 0x314F + (hangulKeyIdx - 30);
                            hangulSendKey(newHangulChar,HCURSOR_ADD);
                            mHangulKeyStack[0] = 0;
                            mHangulKeyStack[1] = 0;
                            mHangulJamoStack[0] = 0;
                            mHangulKeyStack[2] = hangulKeyIdx;
                            mHangulJamoStack[1] = hangulKeyIdx;
                            mHangulKeyStack[3] = 0;
                            mHangulState = H_STATE_3;

                        }
                    }
                    break;
                case H_STATE_5:
                    Log.i("SoftKey", "HAN_STATE 5");
                    if (hangulKeyIdx < 30) { // if 자음
                        int newHangulKeyIdx = isHangulKey(4,hangulKeyIdx);
                        if (newHangulKeyIdx > 0) { // if 자음조합이 있을 경우
                            mHangulKeyStack[5] = hangulKeyIdx;
                            mHangulJamoStack[2] = newHangulKeyIdx;

                            cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                            jung_idx = mHangulJamoStack[1] - 30;
                            jong_idx = h_jongsung_idx[mHangulJamoStack[2]+1];;
                            newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                            hangulSendKey(newHangulChar,HCURSOR_UPDATE);
                            mHangulState = H_STATE_6;
                        }
                        else { // if 자음조합이 없을 경우

                            // cursor error trick start
                            cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                            jung_idx = mHangulJamoStack[1] - 30;
                            jong_idx = h_jongsung_idx[mHangulJamoStack[2]+1];;
                            newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                            hangulSendKey(newHangulChar,HCURSOR_UPDATE);
                            // trick end


                            mHangulKeyStack[0] = hangulKeyIdx;
                            mHangulKeyStack[1] = 0;
                            mHangulKeyStack[2] = 0;
                            mHangulKeyStack[3] = 0;
                            mHangulKeyStack[4] = 0;
                            mHangulJamoStack[0] = hangulKeyIdx;
                            mHangulJamoStack[1] = 0;
                            mHangulJamoStack[2] = 0;
                            newHangulChar = 0x3131 + hangulKeyIdx;
                            hangulSendKey(newHangulChar,HCURSOR_ADD);
                            mHangulState = H_STATE_1;
                        }
                    }
                    else { // if 모음

                        cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                        jung_idx = mHangulJamoStack[1] - 30;
                        jong_idx = 0;
                        newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                        hangulSendKey(newHangulChar, HCURSOR_UPDATE);

                        mHangulKeyStack[0] = mHangulKeyStack[4];
                        mHangulKeyStack[1] = 0;
                        mHangulKeyStack[2] = hangulKeyIdx;
                        mHangulKeyStack[3] = 0;
                        mHangulKeyStack[4] = 0;
                        mHangulJamoStack[0] = mHangulKeyStack[0];
                        mHangulJamoStack[1] = mHangulKeyStack[2];
                        mHangulJamoStack[2] = 0;

                        cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                        jung_idx = mHangulJamoStack[1] - 30;
                        jong_idx = 0;
                        newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                        hangulSendKey(newHangulChar, HCURSOR_ADD);

                        // Log.i("SoftKey", "--- Goto HAN_STATE 4");
                        mHangulState = H_STATE_4;
                    }
                    break;
                case H_STATE_6:
                    Log.i("SoftKey", "HAN_STATE 6");
                    if (hangulKeyIdx < 30) {

                        // cursor error trick start
                        cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                        jung_idx = mHangulJamoStack[1] - 30;
                        jong_idx = h_jongsung_idx[mHangulJamoStack[2]+1];;
                        newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                        hangulSendKey(newHangulChar,HCURSOR_UPDATE);
                        // trick end


                        mHangulKeyStack[0] = hangulKeyIdx;
                        mHangulKeyStack[1] = 0;
                        mHangulKeyStack[2] = 0;
                        mHangulKeyStack[3] = 0;
                        mHangulKeyStack[4] = 0;
                        mHangulJamoStack[0] = hangulKeyIdx;
                        mHangulJamoStack[1] = 0;
                        mHangulJamoStack[2] = 0;

                        newHangulChar = 0x3131 + hangulKeyIdx;
                        hangulSendKey(newHangulChar,HCURSOR_ADD);

                        mHangulState = H_STATE_1;
                    }
                    else {
                        mHangulJamoStack[2] = mHangulKeyStack[4];

                        cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                        jung_idx = mHangulJamoStack[1] - 30;
                        jong_idx = h_jongsung_idx[mHangulJamoStack[2]+1];;
                        newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                        hangulSendKey(newHangulChar, HCURSOR_UPDATE);

                        mHangulKeyStack[0] = mHangulKeyStack[5];
                        mHangulKeyStack[1] = 0;
                        mHangulKeyStack[2] = hangulKeyIdx;
                        mHangulKeyStack[3] = 0;
                        mHangulKeyStack[4] = 0;
                        mHangulKeyStack[5] = 0;
                        mHangulJamoStack[0] = mHangulKeyStack[0];
                        mHangulJamoStack[1] = mHangulKeyStack[2];
                        mHangulJamoStack[2] = 0;

                        cho_idx = h_chosung_idx[mHangulJamoStack[0]];
                        jung_idx = mHangulJamoStack[1] - 30;
                        jong_idx = 0;
                        newHangulChar = 0xAC00 + ((cho_idx * 21 * 28) + (jung_idx * 28) + jong_idx);
                        hangulSendKey(newHangulChar,HCURSOR_ADD);

                        mHangulState = H_STATE_4; // goto �ʼ�,�߼�
                    }
                    break;
            }
        }
        else {
            // Log.i("Hangul", "handleHangul - No hancode");
            clearHangul();
            sendKey(primaryCode);
        }

    }
// Hangul Code End    

    //예제코드
    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (isAlphabet(primaryCode) && mPredictionOn) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
            //updateCandidates();
        } else {
            sendKeyChar((char)primaryCode);
        }
    }

    //예제코드
    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }

    //예제코드
    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    //wordSeparator인지 판별하는 함수
    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        //contatins : 문자열에 지정된 char 값을 포함하는경우에만 true 리턴
        return separators.contains(String.valueOf((char)code));
    }

//    public void pickDefaultCandidate() {
//        pickSuggestionManually(0);
//    }

//    public void pickSuggestionManually(int index) {
//        if (mCompletionOn && mCompletions != null && index >= 0
//                && index < mCompletions.length) {
//            CompletionInfo ci = mCompletions[index];
//            getCurrentInputConnection().commitCompletion(ci);
//            updateShiftKeyState(getCurrentInputEditorInfo());
//        } else if (mComposing.length() > 0) {
//            // If we were generating candidate suggestions for the current
//            // text, we would commit one of them here.  But for this sample,
//            // we will just commit the current text.
//            commitTyped(getCurrentInputConnection());
//        }
//    }

    public void swipeRight() {
    }

    public void swipeLeft() {
        handleBackspace();
    }

    public void swipeDown() {
        handleClose();
    }

    public void swipeUp() {
        connectAndSendSpacingJson();
        //-*- test
//        String jsonTest = "{\"response\" : [\"spacing\"], \"spacing\" : \"주말에 실컷 놀아야지\"}"; //"{\"response\" : [\"modified\"], \"modified\" : {\"정말루\" : [\"0\",\"정말로\"], \"하르\" : [\"2\",\"하루\"] }}";
//        processJsonMessegeTest(jsonTest);
        //-*-
//        InputConnection ic = getCurrentInputConnection();
//
//        //서버로 보낼 오타 수정 메시지 작성 및 송신
//        if(!tcp.getIsRunning())
//            TcpOpen(tcp);
//        String text = ic.getExtractedText(new ExtractedTextRequest(), 0).text.toString();
//        String sendJson = makeJsonRequireFormat(true, false, hangulTextSplitPlusOneSpace(text), null);
//        Log.d("text??", sendJson);
//        tcp.sendData(sendJson);
        ////////////
    }

    public void onPress(int primaryCode) {
    }

    public void onRelease(int primaryCode) {
    }

    //서버로 보낼 json 데이터 제작 함수
    public String makeJsonRequireFormat(boolean isSpacing, boolean isModified, String spacingData, String modifiedData)
    {
        String messege = "{\"request\":[\"";
        if(isSpacing)
            messege += "spacing\"";
        if(isModified)
//            if(isSpacing)
//                msg += ",";
            messege += "modified\"";

        messege += "]";
        if(isSpacing) {
            messege += ",\"spacingData\":\"" + spacingData + "\"";
        }
        if(isModified){
            messege += ",\"modifiedData\":\"" + modifiedData + "\"";
        }
        messege += "}";

        return messege;
    }

    //한글을 제외한 기준으로 스플릿하고 마지막에 " "삽입하는 함수
    private String hangulTextSplitPlusOneSpace(String text)
    {
        //한글을 제외한 단어를 공백처리하기 위한 정규식
        String splitWord = "[^ㄱ-힣]+";
        String rePlaceStr = text.replaceAll(splitWord," ");
        rePlaceStr = rePlaceStr.concat(" ");

        return rePlaceStr;
    }



    //softKeyboard에서 처리하는 메시지핸들러
    //여기서부터!!!!
    public void handleMessage(Message messege){

        switch (messege.what)
        {
            case MSG_REQUEST_RECEIVE:
                processJsonMessege(messege);
                break;
            case MSG_AUTO_CORRECTION_RECEIVE:
                processAutoCorrectionMessege(messege);
                break;
            case MSG_AUTO_SPACING_RECEIVE:
                processAutoSpacingMessege(messege);
                break;
        }
    }

    private void processJsonMessege(Message messege){
        JSONObject jsonObject;
        JSONArray responseType;
        JSONArray correctionList;
        InputConnection inputConnection;
        String spacingData;
        JSONObject modifiedData;

        Toast.makeText(this,"Receive Data : " + messege.obj.toString(), Toast.LENGTH_SHORT).show();
        Log.d("Receive Data : ", messege.obj.toString());
        try {
            inputConnection = getCurrentInputConnection();
            jsonObject = new JSONObject((String) messege.obj);
//            obj = new JSONObject("{\"response\" : [\"modified\"],\"modified\" : {\"나는\" : [\"0\",\"ㅋㅋ\"],\"김정민\" : [\"1\",\"ㅎㅎ\"]}}");
            responseType = (JSONArray) jsonObject.getJSONArray("response");

            //자동 띄어쓰기 일 경우
            if(responseType.getString(0).equals("spacing"))
            {
                spacingData = jsonObject.getString("spacing");
                inputConnection.finishComposingText();
                //isCommitted = true;
                //-*-띄어쓰기 문제 있을시 deleteSurrounding을 주석처리하고 아래 세 줄 주석해제 할것.
//                        int oldTextNum = ic.getExtractedText(new ExtractedTextRequest(), 0).text.length();
//                        ic.setSelection(oldTextNum, oldTextNum);
//                        ic.deleteSurroundingText(oldTextNum,0);
                inputConnection.deleteSurroundingText(MAX_TEXT,MAX_TEXT);
                inputConnection.commitText(spacingData,1);
            }

            //오타 수정의 경우
            else if(responseType.getString(0).equals("modified"))
            {
                correctionButtonInformList.clear();

                if(!jsonObject.getString("modified").equals("noData")) {
                    modifiedData = jsonObject.getJSONObject("modified");
                    Iterator<String> correctionKeys = modifiedData.keys();

                    while (correctionKeys.hasNext()){
                        String oldWord = correctionKeys.next();
                        String[] correctionWordTmp;

                        correctionList = modifiedData.getJSONArray(oldWord);

                        int index = 1;
                        correctionWordTmp = new String[correctionList.length()-1];
                        while(index < correctionList.length())
                        {
                            correctionWordTmp[index-1] = correctionList.getString(index++);
                        }

                        //오타 검사 단어와 오타 단어가 같을 경우 제외
                        if(!oldWord.equals(correctionWordTmp[0])) {
                            correctionButtonInformList.add(new correctionButtonInform(
                                    oldWord,
                                    correctionWordTmp[0]));
                        }
                    }
                }

                //오타 수정 버튼 텍스트 갱신
                renewCorrectionButtonsAsCorrectionButtonInformList();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processAutoCorrectionMessege(Message messege)
    {
        connectAndSendCorrectionJson();
    }

    private void processAutoSpacingMessege(Message messege)
    {
        connectAndSendSpacingJson();
    }

    private void renewCorrectionButtonsAsCorrectionButtonInformList(){
        clearButtonListText(correctionButton);
        for(int j = 0; j < correctionButtonInformList.size(); j++)
        {
            if(j>=correctionButton.length)
                break;

            correctionButton[j].setText(correctionButtonInformList.get(j).getOldWord());
        }
    }
    private void clearButtonListText(Button[] buttons){
        for(int i = 0; i < buttons.length; i++)
        {
            buttons[i].setText("");
        }
    }

    //tcp 연결 및 통신 시작
    private void TcpOpen(TcpClient tcp){
        if(!tcp.getIsRunning()) {
            tcpThread = null;
            tcpThread = new Thread(tcp);
            tcp.setRunningState(true);
//            Log.d("what Thread",tcp.toString());
            tcpThread.start();
            Log.d("numThread",String.valueOf(Thread.activeCount()));
        }
    }

    private void autoFunctionThreadRun(AutoFunction thread){
        autoFunctionThread = null;
        autoFunctionThread = new Thread(thread);
        autoFunctionThread.start();
    }

    //tcp 연결 해제
//    private void TcpClose(TcpClient tcp){
//        tcp.setRunningState(false);
//        tcp.socketClose();
//    }

    //pass
    private void updateCompletionWordAsOneCursor(int cursor)
    {
        InputConnection ic = getCurrentInputConnection();
        String completeOldWord = "";

        String oneLoopBeforeWord;
        //커서 전 단어
        if(!isCursorLeftmost())
        {
            String cursorLeftWord = "";
            int cursorBeforeRelativePosition = 1;
            do
            {
                oneLoopBeforeWord = cursorLeftWord;
                cursorLeftWord = ic.getTextBeforeCursor(cursorBeforeRelativePosition,0).toString();
                cursorBeforeRelativePosition++;
            }while(!cursorLeftWord.equals(oneLoopBeforeWord) && !isWordSeparator(cursorLeftWord.charAt(0)));

            completionOldWordInform.setStartPos(cursor - (cursorBeforeRelativePosition - 2));
            if(cursorBeforeRelativePosition>1) {
                completeOldWord += oneLoopBeforeWord;
            }
        }
        else
            completionOldWordInform.setStartPos(cursor);

        //커서 후 단어
        if(!isCursorRightmost(ic))
        {
            String cursorRightWord = "";
            int cursorBeforeRelativePosition = 1;
            do
            {
                oneLoopBeforeWord = cursorRightWord;
                cursorRightWord = ic.getTextAfterCursor(cursorBeforeRelativePosition,0).toString();
                cursorBeforeRelativePosition++;
                Log.d("complWord", "!" + cursorRightWord + "?");
            }while(!cursorRightWord.equals(oneLoopBeforeWord) && !isWordSeparator(cursorRightWord.charAt(cursorRightWord.length() - 1)));

            completionOldWordInform.setLastPos(cursor + (cursorBeforeRelativePosition - 2));
            if(cursorBeforeRelativePosition>1) {
                completeOldWord += oneLoopBeforeWord;
            }
        }
        else
            completionOldWordInform.setLastPos(cursor);
        completionOldWordInform.setOldWord(completeOldWord);
        Log.d("completeOldWord", completeOldWord);
    }

    private boolean isCursorLeftmost()
    {
        return (getCurrentInputConnection().getTextBeforeCursor(1,0) == null ||
                getCurrentInputConnection().getTextBeforeCursor(1,0).toString().equals("") ||
                getCurrentInputConnection().getTextBeforeCursor(1,0).length() <= 0);
    }

    private boolean isCursorRightmost(InputConnection ic)
    {
//        Log.d("completeIC", "!" + ic.getTextAfterCursor(1,0).toString() + "?");
        return (ic.getTextAfterCursor(1,0) == null ||
                ic.getTextAfterCursor(1,0).toString().equals("") ||
                ic.getTextAfterCursor(1,0).toString().length() <= 0);
    }

    private void updateCompletionWordAsComposing(){
        completionOldWordInform.setOldWord(mComposing.toString());
    }

    private void setText(int start, int len, String newWord)
    {
        InputConnection inputConnection = getCurrentInputConnection();
        clearHangul();
        inputConnection.setSelection(start,start);
        Log.d(String.valueOf(start),String.valueOf(len));
        inputConnection.deleteSurroundingText(0,len);
        inputConnection.commitText(newWord,1);
    }

    private void connectAndSendCorrectionJson()
    {
        InputConnection inputConnection = getCurrentInputConnection();
        if(!tcp.getIsRunning())
            TcpOpen(tcp);
        if(!isTextEmpty()) {
            String text = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text.toString();
            String correctionJsonMessege = makeJsonRequireFormat(false, true, null, hangulTextSplitPlusOneSpace(text));
//            Log.d("text??", sendJson);
            tcp.sendData(correctionJsonMessege);
        }
    }

    private void connectAndSendSpacingJson()
    {
        //Log.d("in???","?????????");
        InputConnection inputConnection = getCurrentInputConnection();

        //서버로 보낼 자동 띄어쓰기 모델 메시지 작성 및 송신
        if(!tcp.getIsRunning())
            TcpOpen(tcp);
        if(!isTextEmpty()) {
            String text = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text.toString();
            String sendJson = makeJsonRequireFormat(true, false, text.replaceAll("\n"," "), null);
//            Log.d("text??", sendJson);
            tcp.sendData(sendJson);
        }
    }

    private boolean isTextEmpty(){
        return (getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0) == null
                || getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0).text.toString().equals(""));
    }

    void updateCompletionOldWordInformAsCursur(int startCursor, int finishCursor){
        InputConnection inputConnection = getCurrentInputConnection();
        if(isBlurredText(startCursor, finishCursor))
        {
            completionOldWordInform.setOldWord(inputConnection.getSelectedText(0).toString());
            completionOldWordInform.setStartPos(startCursor);
            completionOldWordInform.setLastPos(finishCursor);
        }
        else if(mComposing.length()>0)
            updateCompletionWordAsComposing();
        else
            updateCompletionWordAsOneCursor(startCursor);

        updateCompletionButtons();// 후보자 추천 설정
    }

    boolean isBlurredText(int startCursor, int finishCursor){
        return (startCursor<finishCursor && getCurrentInputConnection().getSelectedText(0)!=null);
    }
}