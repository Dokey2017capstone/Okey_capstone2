package com.example.karry.myapplication;

import java.io.*;
import android.util.*;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

public class Main extends Activity {
    SpaceTokenizer spaceTokenizer;

    String[] items = {"때문이다","있습니다","것이었다","사람들이","것입니다","사람들은","것이라고","시작했다","위해서는","이야기를","대해서는","중심으로","불구하고","아니었다","지금까지","그러므로","사람들의","그러니까","가능성이","것이라는","과정에서","그렇다면","어머니는","관계자는","하더라도","경우에는","대통령이","대통령의","아름다운","처음으로","대상으로","어머니가","생각한다","대통령은","없습니다","대부분의","나타났다","생각하는","아버지는","아버지가","아이들이","그렇다고","마찬가지로","무엇인가","사실이다","한나라당","했습니다","생각하고","있었습니다","필요하다","구체적인","알려졌다","왜냐하면","생각했다","그리하여","사람들을","아버지의","어머니의","자리에서","그렇지만","주장했다","있었는데","예정이다","나타나는","일이었다","말하자면","아이들은","상황에서","때문이었다","우리에게","아무것도","우리나라","대표적인","아닙니다","바탕으로","방법으로","있어서는","이번에는","사용하는","대해서도","일반적으로","좋아하는","오랫동안","사람에게","세종계획","아니라는","차원에서","방향으로","들어갔다","생각합니다","이야기가","가능성을","않는다는","기다리고","계획이다","목소리가","지나치게","전망이다","무엇보다도","끊임없이","사랑하는","무엇보다","설명했다","구체적으로","적극적으로","중요하다","있었지만","아무래도","처음에는","사람들에게","보도했다","그럼에도","보여주는","처음부터","나타난다","일어나는","분위기를","상태에서","학생들이","나타내는","프로젝트","아이들의","이루어진","않습니다","이르기까지","같습니다","생각하면","목소리로","프로그램을","방식으로","있으므로","만들었다","요구하는","지적했다","입장에서","기준으로","말입니다","아이들을","강조했다","아니지만","들어가는","프로그램","살아가는","적극적인","덧붙였다","그야말로","대한민국","한꺼번에","자신들의","의미에서","움직이는","상대적으로","자연스럽게","목적으로","바라보는","발표했다","어머니를","그러다가","한마디로","이름으로","나름대로","뿐이었다","생각된다","있었다는","이야기는","사이에서","주장하는","이용하여","마지막으로","표정으로","일반적인","의미한다","모양이다","마음으로","가능하다","아버지를","시작한다","떨어지는","모르겠다","아니라고","목소리를","사회에서","그런데도","그러면서","우리나라의","얼마든지","그리고는","정확하게","모습으로","모르지만","우리들의","여기에서","나타나고","프랑스의","때문입니다","학교에서","존재하는","선생님이","관점에서","모른다는","아닙니까","전통적인","그것이다","바라보고","만들어진","본격적인","해당하는","분위기가","모양이었다","측면에서","차지하고","이미지를","학생들의","없었습니다","미국에서","않았지만","보여준다","세계에서","이상으로","경우에도","고등학교","서울에서","대답했다","사회주의","보여주고","마련이다","차지하는","서비스를","것이지만","말했습니다","바라보았다","자본주의","있어서의","기본적인","러시아의","어려움을","발생하는","되었습니다","마음대로","중에서도","전문가들은","들어왔다","않았습니다","들어오는","사람으로","근본적인","생각하지","마찬가지다","문화관광부","방침이다","함으로써","한편으로는","있어서도","선생님은","세계적인","자유롭게","일어났다","추구하는","이를테면","사람이다","모양으로","대부분이","세상에서","돌아가는","사람들도","계속해서","텔레비전","대통령과","컴퓨터를","예상된다","기다리는","여러가지","재미있는","있었으나","제공하는","본격적으로","나라에서","이탈리아","학생들은","조선일보","지나가는","있었으며","오늘날의","이루어지는","바라보며","국립국어연구원","한국에서","있습니까","주장하고","자리잡고","있었다고","시작하였다","한나라당은","의미하는","대변인은","자신에게","경기에서","돌아오는","바랍니다","그녀에게","것이므로","이용하는","둘러싸고","직접적인","근본적으로","기록했다","자유로운","때문이라고","있겠는가","어디까지나","이어지는","앞으로도","떠올랐다","그들에게","이르렀다","무엇인지","분야에서","불과하다","우리들은","떨어졌다","시작하는","국민들의","가능성도","주장한다","않는다고","결정적인","한국인의","불가능한","최근에는","일본에서","들어간다","하겠습니다","일으키는","속에서도","참여하는","지역에서","아이들에게","전적으로","사용하고","않습니까","일어난다","정도였다","돌아왔다","부드러운","할지라도","있으니까","초등학교","고려대학교","그러면서도","사라지고","할아버지","등장하는","이해하는","긍정적인","부정적인","사람들과","일어나고","살펴보면","무엇일까","수준으로","아버지와","분명하다","비롯하여","되었다는","반대하는","움직임을","현실적으로","벗어나지","가능성은","스스로의","그곳에서","실질적인","개인적인","생각하며","왔습니다","바람직한","철저하게","사이에는","어머니와","다음으로","않는다면","스스로를","강조하는","나타낸다","않았다는","누군가가","말하였다","지속적으로","국민학교","드러났다","이해하고","문제점을","민주당은","여기서는","민주주의","대통령을","기업들이","할머니는","들어가서","정상적인","알아보자","실정이다","강조하고","것이지요","일방적으로","관련하여","영향력을","것이라면","하면서도","선생님의","공동으로","사람처럼","방법이다","나타나지","기본적으로","배경으로","조심스럽게","여기에는","잃어버린","이리저리","가운데서","밝혀졌다","요구하고","존재하지","소프트웨어","들려왔다","연구보조원","앞으로는","이곳에서","민주당의","관계없이","결과적으로","떨어지고","싶습니다","규정하고","이루어져","운영하는","사용되는","생각이다","사용하여","국내에서","국민들이","있겠지만","들어서는","유지하고","아니에요","시장에서","현실적인","아니냐는","헤더붙임","물론이고","이스라엘","필요하다고","것이어서","거기에는","목소리는","전형적인","아저씨는","틀림없다","못하였다","대학에서","한편으로","느껴졌다","형식으로","무엇인가를","억원으로","인정하고","표현하는","제외하고는","인터넷을","따라서는","분명하게","아프리카","받아들일","지적하고","앞으로의","이루어지고","아직까지","설명이다","심지어는","가르치는","되었다고","다음에는","나타내고","고스란히","날카로운","효과적으로","할머니가","추진하고","중얼거렸다","설명하는","중요성을","벌어지고","커뮤니케이션","시점에서","진행되고","나머지는","말미암아","내용으로","시작하여","틀림없이","이루어질","오랜만에","것으로서","좋습니다","일어섰다","대표하는","경쟁력을","가리키는","어려움이","이외에도","떠오르는","그로부터","여기저기","대부분은","한나라당이","중국에서","실제로는","있으면서","필요성을","주민들의","설명하고","현장에서","한결같이","위해서도","독자적인","띄어쓰기","민족문화연구원","시작된다","전체적으로","지금까지의","언젠가는","그렇습니다","시작되는","주십시오","프로그램의","어렵다는","요구했다","엄청나게","인정하는","가능하게","순식간에","것인가를","네덜란드","국회의원","결정하는","주민들이","수단으로","사용한다","않았다고","시작되었다","분석했다","이제까지","것이었습니다","정치적인","미국으로","어리석은","쳐다보았다","이해하기","그것으로","학생들을","우리들이","그제서야","이야기다","역사적인","문제이다","사랑하고","내밀었다","늘어나고","생각하여","집중적으로","지적이다","느껴지는","친구들이","되었는데","효과적인","다양하게","이상하게","일어나서","시스템을","할아버지는","사라졌다","마찬가지였다","조금이라도","사회적인","어울리는","있었어요","삼성전자","지속적인","지배하는","국민에게","제시하고","연구하는","지원하는","아이들과","친구들과","움직임이","민주주의의","결정했다","여성들이","프로야구","소리쳤다","사회적으로","그러고는","사람이었다","합리적인","유지하는","포함되어","진지하게","구하여라","수화기를","인식하고","거기에서","움직이지","아저씨가","들어섰다","정면으로","일이라고","전망했다","하겠다는","구성하는","홈페이지","모릅니다","하였으나","들어가면","들어와서","불가능하다","파악하고","일입니다","계속되고","자신들이","이루어지지","언제든지","필요하다는","선수들이","학생들에게","하였습니다","차지했다","심각하게","유일하게","내놓았다","재미있게","현실에서","한반도의","않았으나","보편적인","한나라당의","여성들의","포기하고","곳곳에서","때문인지","전체적인","확인하고","전반적인","생각으로","해결하기","이루어진다","상관없이","그러기에","아름답게","상황이다","연합뉴스","부지런히","제시하는","관계자들은","계속되는","있으면서도","시민들의","일어나지","자신감을","없으므로","때문이라는","무시하고","되었으며","이리하여","벌어지는","인정하지","시민들이","가운데는","있는지를","많습니다","못하도록","기자회견을","않으려고","세계적으로","생각하니","절대적인","것보다는","확실하게","고맙습니다","들었습니다","없었지만","전해졌다","주인공이","이야기의","않겠다는","민주당이","사용하지","웃으면서","시작으로","회의에서","드러내고","일으켰다","거짓말을","팔레스타인","정신적인","아름다움을","촉구했다","드러내는","발생한다","드러난다","보유하고","대하여는","돌아오지","대통령에게","이들에게","생각했던","생각하게","부드럽게","프로그램이","주민들은","인간적인","완벽하게","기다렸다","상대방의","기업들의","구조조정","감사합니다","싶어하는","사람이나","있는가를","조건으로","이어졌다","나중에는","일이지만","깨달았다","지식인의","구성되어","스스로가","돌아가신","해결하는","않겠는가","여기까지","보더라도","여자들이","자연스러운","아름답고","움직이고","최소한의","메시지를","인간에게","검토하고","슬그머니","싫어하는","들어온다","존재한다","드러나는","진정으로","아파트를","특징이다","동아일보","그것들은","받아들이는","모른다고","올라가는","경제적인","광범위한","작품이다","높아지고","이후에는","하나이다","일찌감치","컴퓨터가","바라본다","벌써부터","강조한다","안된다는","나라에서는","생산하는","병원에서","벌어졌다","입장이다","셈이었다","알아봅시다","자전거를","연결되어","이런저런","운영하고","국민들은","주장이다","우리나라는","느닷없이","아니하고","들어가고","성공적으로","어디에서","올라갔다","준비하고","제공하고","알면서도","졸업하고","파악하는","가정에서","주변에서","프로그램은","돌아갔다","위원장은","있겠습니까","나로서는","조그마한","늘어났다","하나하나","국회에서","나에게는","않더라도","에너지를","제기되고","그때마다","끊임없는","국민회의","분위기는","주간조선","말이었다","기업들은","정치적으로","그때까지","다가오는","사건으로","할아버지가","바람직하다","떨어진다","바뀌었다","등장한다","이루어졌다","손가락을","유명하다","아이에게","발견하고","이제부터","막론하고","못지않게","원칙적으로","내려오는","나름대로의","지향하는","없었다는","참여하고","것인지도","개인적으로","그때부터","실질적으로","손가락으로","아니라면","컴퓨터의","보기에는","선수들의","선거에서","전국적으로","그렇게도","사회에서는","제공한다","입장에서는","회사에서","의원들은","이해하지","받아들여","사용되고","궁극적으로","상징하는","지나갔다","비교하여","중소기업","늘어나는","그에게는","여성들은","관해서는","러시아는","활발하게","못한다는","뿐입니다","사실입니다","일본으로","가리킨다","물끄러미","보았습니다","어김없이","여보세요","대대적인","내년부터","주목된다","시민사회의","않았는데","시민단체","그것들을","수준에서","아니겠는가","공부하는","전반적으로","설명한다","일찍부터","영향으로","마찬가지","간단하게","프로그램에","본질적으로","중시하는","부끄러운","전달하는","좋아한다","쓰레기를","이후에도","이번에도","일으키고","나타냈다","만들어야","당연하다","단계에서","진심으로","기억하고","기대하고","자동차를","러시아가","극단적인","텍스트의","여자들은","출신으로","돌아가고","있었음을","그림자가","의원들이","진행되는","불필요한","현재까지","처음이다","다가왔다","지금부터","개념으로","담당하는","쳐다보고","가까스로","것이기도","사람에게는","선생님을","선수들은","인터뷰에서","전문가들의","거부하고","국제적인","극복하고","사람이라면","객관적인","이야기하는","직원들이","쳐다본다","못했다는","주었습니다","일상적인","모습이다","어머니에게","하느님의","바람직하지","평가하는","쓸데없는","어디선가","다니면서","작품으로","엉덩이를","정도이다","쏟아지는","어울리지","증가하고","새삼스럽게","컴퓨터에","이상적인","비슷하다","정부에서","비판하고","아침부터","아내에게","시작하고","공산주의","선택하는","거느리고","문제들을","추진하는","이어지고","할머니의","기반으로","정신없이","그만두고","있을지도","아이디어를","획기적인","네트워크","역사적으로","일본에서는","생각하기","느껴진다","불가피한","강력하게","말이에요","자리잡은","인물이다","터뜨렸다","중요하다고","가리키며","만들어낸","비판하는","보이지만","다르다는","있었다면","실시하고","동안이나","평가했다","이루어져야","살펴보자","시작했습니다","하였으며","부분적으로","하였는데","효율적으로","생생하게","거리에서","아무렇게나","확인하는","당시에는","주인공은","사실이지만","맥락에서","젊은이들이","끄덕였다","따름이다","않았으며","이야기도","시작했고","그랬더니","근무하는","서울지검","해당한다","경제적으로","내려갔다","자본주의의","자랑하는","것들이다","있는데도","인도네시아","현실이다","조선시대"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MultiAutoCompleteTextView edit =
                (MultiAutoCompleteTextView) findViewById(R.id.edit);
        edit.setTokenizer(new SpaceTokenizer());
        edit.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, items));
    }
}