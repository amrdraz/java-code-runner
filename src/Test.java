import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

public class Test {
    public final static String DEFAULT_HASH="TestOut";
    private final static String DEFAULT_PASS="Passed Test";
    private final static String DEFAULT_FAIL="Failed Test";
    private final static int DEFAULT_SCORE=0;

    private String hash=DEFAULT_HASH;
    private String code;
    
    private ByteArrayOutputStream testOut;
    private PrintStream out;

    public Test () {
        testOut = new ByteArrayOutputStream();
        out = new PrintStream(testOut);
    }

    public Test(String h) {
        this();
        hash = h;
    }

    public Test(String h, String c) {
        this(h);
        code = c;
    }

    public void println(String s) {
        print(s+"\n");
    }
    public void print(String s) {
        if(out!=null) {
            out.print(s);
        } else {
            System.out.print(s);
        }
    }
    public ByteArrayOutputStream getTestOut() {
        return testOut;
    }
    public void setHash(String h) {
        hash = h;
    }
    public String getCode() {
        return this.code;
    }
    public void setCode(String s) {
        code = s;
    }

    private void out(boolean pass,String msg, int point, String tag) {
        String str = "<["+hash+"]>{"+
            "\"pass\":"+pass+
            ",\"message\":\""+msg.replaceAll("\\\\(.)", "\\\\\\\\$1").replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\\\n")+"\""+
            ",\"score\":"+point;
        if(tag!=null) {
            str+=",\"tag\":\""+tag+"\"";
        }
        str+="}<["+hash+"]>";
        println(str);
    }
    private void out(boolean pass,String msg, int point) {
        out( pass, msg,  point, null);
    }
    public void pass(String msg, int point, String tag) {
        if(point<0) { // you can not award negative points for pass
            point=0;
        }
        out(true,msg,point, tag);
    }
    public void pass(String msg, int point) {
        pass(msg,point, null);
    }

    public void pass(String msg) {
        pass(msg,0);
    }
    public void pass(String msg, String tag) {
        pass(msg,0, tag);
    }

    public void pass(int score, String tag) {
        pass(DEFAULT_PASS,score, tag);
    }

    public void pass(int score) {
        pass(DEFAULT_PASS,score, null);
    }

    public void pass() {
        pass(DEFAULT_PASS);
    }

    public void fail(String msg, int point, String tag) {
        if(point>0) { // you can not award positive points for failure
            point=0;
        }
        out(false,msg,0, tag);
    }
    public void fail(String msg, int point) {
        fail(msg,0,null);
    }
    public void fail(String msg) {
        fail(msg,0);
    }
    public void fail(String msg,String tag) {
        fail(msg,0,tag);
    }
    public void fail(int score) {
        fail(DEFAULT_FAIL,score);
    }

    public void fail(int score, String tag) {
        fail(DEFAULT_FAIL,score,tag);
    }
    public void fail() {
        fail(DEFAULT_FAIL);
    }

    /**
     * Tests whether a given String matches another string using matches
     * @param user     String test
     * @param expected String regexp
     * @param msg      String msg to write on success
     * @param failmsg  String msg to write on failure
     * @param s        int    score awarded for test
     * @param tag      String tag assissiated with test
     */
    public void matches(String user, String expected, String msg, String failmsg, int score, String tag) {
        if(user.matches("[\\s\\S]*"+expected+"[\\s\\S]*")){
            pass(msg,score,tag);
        } else {
            fail(failmsg,score,tag);
        }
    }
    public void matches(String user, String expected, String msg, int score, String tag) {
        if(user.matches(expected)){
            pass(msg, score,tag);
        } else {
            fail("Expected "+user+" to match "+expected+"",score,tag);
        }
    }
    public void matches(String user, String expected, String msg, String failmsg, String tag) {
        matches(user,expected,msg,failmsg,0,tag);
    }
    public void matches(String user, String expected, String msg, String failmsg, int score) {
        matches(user,expected,msg,failmsg,score,null);
    }
    public void matches(String user, String expected, String msg, String tag) {
        matches(user,expected,msg,0,tag);
    }
    public void matches(String user, String expected, String msg, int score) {
        matches(user,expected,msg,score,null);
    }
    public void matches(String user, String expected, String msg) {
        matches(user,expected,msg,0,null);
    }
    public void matches(String user, String expected, int score, String tag) {
        matches(user,expected,DEFAULT_PASS,score,tag);
    }
    public void matches(String user, String expected, int score) {
        matches(user,expected,DEFAULT_PASS,score);
    }
    public void matches(String user, String expected) {
        matches(user,expected,DEFAULT_PASS);
    }

    /**
     * Tests whether a given String contains another string using matches
     * @param user     String user string to test
     * @param expected String substring in the given user string
     * @param msg      String msg to write on success
     * @param failmsg  String msg to write on failure
     * @param s        int    score awarded for test
     * @param tag      String tag assissiated with test
     */
    public void contains(String user, Object expected, String msg, String failmsg, int score, String tag) {
        matches(user, "[\\s\\S]*"+expected+"[\\s\\S]*", msg, failmsg, score, tag);
    }
    public void contains(String user, Object expected, String msg, int score, String tag) {
        matches(user, "[\\s\\S]*"+expected+"[\\s\\S]*", msg, "Expected "+user+" to contain "+expected+"",score,tag);
    }
    public void contains(String user, Object expected, String msg, String failmsg, String tag) {
        contains(user,expected,msg,failmsg,0,tag);
    }
    public void contains(String user, Object expected, String msg, String failmsg, int score) {
        contains(user,expected,msg,failmsg,score,null);
    }
    public void contains(String user, Object expected, String msg, String tag) {
        contains(user,expected,msg,0,tag);
    }
    public void contains(String user, Object expected, String msg, int score) {
        contains(user,expected,msg,score,null);
    }
    public void contains(String user, Object expected, String msg) {
        contains(user,expected,msg,0,null);
    }
    public void contains(String user, Object expected, int score, String tag) {
        contains(user,expected,DEFAULT_PASS,score,tag);
    }
    public void contains(String user, Object expected, int score) {
        contains(user,expected,DEFAULT_PASS,score);
    }
    public void contains(String user, Object expected) {
        contains(user,expected,DEFAULT_PASS);
    }

    /**
     * Tests whether a given Object matches another using equals method
     * @param user     Object to equate
     * @param expected expected result
     * @param msg      msg to write on success
     * @param failmsg  msg to write on failure
     * @param s        score awarded for test
     * @param tag      tag assissiated with test
     */
    public void expect(Object user, Object expected, String msg, String failmsg, int score, String tag) {
        if(user.equals(expected)){
            pass(msg,score,tag);
        } else {
            fail(failmsg,score,tag);
        }
    }
    public void expect(Object user, Object expected, String msg, int score, String tag) {
        if(user.equals(expected)){
            pass(msg, score,tag);
        } else {
            fail("Expected "+user+" to equal "+expected+"",score,tag);
        }
    }
    public void expect(Object user, Object expected, String msg, String failmsg, String tag) {
        expect(user,expected,msg,failmsg,0,tag);
    }
    public void expect(Object user, Object expected, String msg, String tag) {
        expect(user,expected,msg,0,tag);
    }
     public void expect(Object user, Object expected, String msg, String failmsg, int score) {
        expect(user,expected,msg,failmsg,0,null);
    }
    public void expect(Object user, Object expected, String msg, int score) {
        expect(user,expected,msg,score,null);
    }
    public void expect(Object user, Object expected, String msg) {
        expect(user,expected,msg,0,null);
    }
    public void expect(Object user, Object expected, int score, String tag) {
        expect(user,expected,DEFAULT_PASS,score,tag);
    }
    public void expect(Object user, Object expected, int score) {
        expect(user,expected,DEFAULT_PASS,score);
    }
    public void expect(Object user, Object expected) {
        expect(user,expected,DEFAULT_PASS);
    }
}