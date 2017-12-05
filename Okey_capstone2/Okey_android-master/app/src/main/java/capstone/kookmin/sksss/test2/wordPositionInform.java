package capstone.kookmin.sksss.test2;

/**
 * Created by sksss on 2017-10-06.
 */

public class wordPositionInform {
    private String oldWord;
    private int startPos;
    private int lastPos;

    wordPositionInform(int startPos, int lastPos, String oldWord)
    {
        this.oldWord = oldWord;
        this.startPos = startPos;
        this.lastPos = lastPos;
    }

    public void setStartPos(int startPos)
    {
        this.startPos = startPos;
    }
    public void setLastPos(int lastPos)
    {
        this.lastPos = lastPos;
    }
    public void setOldWord(String oldWord)
    {
        this.oldWord = oldWord;
    }
    public int getStartPos()
    {
        return this.startPos;
    }
    public int getLastPos()
    {
        return this.lastPos;
    }
    public int getWordLength()
    {
        return this.lastPos - this.startPos;
    }
    public String getOldWord()
    {
        return this.oldWord;
    }
}
