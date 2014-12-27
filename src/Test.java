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
    public boolean pass(String msg, int point, String tag) {
        if(point<0) { // you can not award negative points for pass
            point=0;
        }
        out(true,msg,point, tag);
        return true;
    }
    public boolean pass(String msg, int point) {
        return pass(msg,point, null);
    }

    public boolean pass(String msg) {
        return pass(msg,0);
    }
    public boolean pass(String msg, String tag) {
        return pass(msg,0, tag);
    }

    public boolean pass(int score, String tag) {
        return pass(DEFAULT_PASS,score, tag);
    }

    public boolean pass(int score) {
        return pass(DEFAULT_PASS,score, null);
    }

    public boolean pass() {
        return pass(DEFAULT_PASS);
    }

    public boolean fail(String msg, int point, String tag) {
        if(point>0) { // you can not award positive points for failure
            point=0;
        }
        out(false,msg,0, tag);
        return false;
    }
    public boolean fail(String msg, int point) {
        return fail(msg,0,null);
    }
    public boolean fail(String msg) {
        return fail(msg,0);
    }
    public boolean fail(String msg,String tag) {
        return fail(msg,0,tag);
    }
    public boolean fail(int score) {
        return fail(DEFAULT_FAIL,score);
    }

    public boolean fail(int score, String tag) {
        return fail(DEFAULT_FAIL,score,tag);
    }
    public boolean fail() {
        return fail(DEFAULT_FAIL);
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
    public boolean matches(String user, String expected, String msg, String failmsg, int score, String tag) {
        if(user.matches("[\\s\\S]*"+expected+"[\\s\\S]*")){
            return pass(msg,score,tag);
        } else {
            return fail(failmsg,score,tag);
        }
    }
    public boolean matches(String user, String expected, String msg, int score, String tag) {
        if(user.matches(expected)){
            return pass(msg, score,tag);
        } else {
            return fail("Expected "+user+" to match "+expected+"",score,tag);
        }
    }
    public boolean matches(String user, String expected, String msg, String failmsg, String tag) {
        return matches(user,expected,msg,failmsg,0,tag);
    }
    public boolean matches(String user, String expected, String msg, String failmsg, int score) {
        return matches(user,expected,msg,failmsg,score,null);
    }
    public boolean matches(String user, String expected, String msg, String tag) {
        return matches(user,expected,msg,0,tag);
    }
    public boolean matches(String user, String expected, String msg, int score) {
        return matches(user,expected,msg,score,null);
    }
    public boolean matches(String user, String expected, String msg) {
        return matches(user,expected,msg,0,null);
    }
    public boolean matches(String user, String expected, int score, String tag) {
        return matches(user,expected,DEFAULT_PASS,score,tag);
    }
    public boolean matches(String user, String expected, int score) {
        return matches(user,expected,DEFAULT_PASS,score);
    }
    public boolean matches(String user, String expected) {
        return matches(user,expected,DEFAULT_PASS);
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
    public boolean contains(String user, Object expected, String msg, String failmsg, int score, String tag) {
        return matches(user, "[\\s\\S]*"+expected+"[\\s\\S]*", msg, failmsg, score, tag);
    }
    public boolean contains(String user, Object expected, String msg, int score, String tag) {
        return matches(user, "[\\s\\S]*"+expected+"[\\s\\S]*", msg, "Expected "+user+" to contain "+expected+"",score,tag);
    }
    public boolean contains(String user, Object expected, String msg, String failmsg, String tag) {
        return contains(user,expected,msg,failmsg,0,tag);
    }
    public boolean contains(String user, Object expected, String msg, String failmsg, int score) {
        return contains(user,expected,msg,failmsg,score,null);
    }
    public boolean contains(String user, Object expected, String msg, String tag) {
        return contains(user,expected,msg,0,tag);
    }
    public boolean contains(String user, Object expected, String msg, int score) {
        return contains(user,expected,msg,score,null);
    }
    public boolean contains(String user, Object expected, String msg) {
        return contains(user,expected,msg,0,null);
    }
    public boolean contains(String user, Object expected, int score, String tag) {
        return contains(user,expected,DEFAULT_PASS,score,tag);
    }
    public boolean contains(String user, Object expected, int score) {
        return contains(user,expected,DEFAULT_PASS,score);
    }
    public boolean contains(String user, Object expected) {
        return contains(user,expected,DEFAULT_PASS);
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
    public boolean expect(Object user, Object expected, String msg, String failmsg, int score, String tag) {
        if(user.equals(expected)){
            return pass(msg,score,tag);
        } else {
            return fail(failmsg,score,tag);
        }
    }
    public boolean expect(Object user, Object expected, String msg, int score, String tag) {
        if(user.equals(expected)){
            return pass(msg, score,tag);
        } else {
            return fail("Expected "+user+" to equal "+expected+"",score,tag);
        }
    }
    public boolean expect(Object user, Object expected, String msg, String failmsg, String tag) {
        return expect(user,expected,msg,failmsg,0,tag);
    }
    public boolean expect(Object user, Object expected, String msg, String tag) {
        return expect(user,expected,msg,0,tag);
    }
     public boolean expect(Object user, Object expected, String msg, String failmsg, int score) {
        return expect(user,expected,msg,failmsg,0,null);
    }
    public boolean expect(Object user, Object expected, String msg, int score) {
        return expect(user,expected,msg,score,null);
    }
    public boolean expect(Object user, Object expected, String msg) {
        return expect(user,expected,msg,0,null);
    }
    public boolean expect(Object user, Object expected, int score, String tag) {
        return expect(user,expected,DEFAULT_PASS,score,tag);
    }
    public boolean expect(Object user, Object expected, int score) {
        return expect(user,expected,DEFAULT_PASS,score);
    }
    public boolean expect(Object user, Object expected) {
        return expect(user,expected,DEFAULT_PASS);
    }
}