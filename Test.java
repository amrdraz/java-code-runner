import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

public class Test {
    public final static String DEFAULT_HASH="TestOut";
    private final static String DEFAULT_PASS="Passed Test";
    private final static String DEFAULT_FAIL="Failed Test";
    private final static int DEFAULT_SCORE=0;

    private String hash=DEFAULT_HASH;
    private String solution="";
    
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
    public void setSolution(String c) {
        solution = c;
    }
    public String getSolution() {
        return solution;
    }

    private void out(boolean pass,String msg, int point, String tag) {
        String str = "<["+hash+"]>{"+
            "\"pass\":"+pass+
            ",\"message\":\""+msg+"\""+
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

    public void pass(int s, String tag) {
        pass(DEFAULT_PASS,s, tag);
    }

    public void pass(int s) {
        pass(DEFAULT_PASS,s, null);
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
    public void fail(int s) {
        fail(DEFAULT_FAIL,s);
    }
    public void fail(int s, String tag) {
        fail(DEFAULT_FAIL,s,tag);
    }
    public void fail() {
        fail(DEFAULT_FAIL);
    }

    public void expect(Object user, Object expected, String msg, String failmsg, int s, String tag) {
        if(user.equals(expected)){
            pass(msg,s,tag);
        } else {
            fail(failmsg,s,tag);
        }
    }
    public void expect(Object user, Object expected, String msg, int s, String tag) {
        if(user.equals(expected)){
            pass(msg, s,tag);
        } else {
            fail("Expected "+user+" to equal "+expected+"",s,tag);
        }
    }
    public void expect(Object user, Object expected, String msg, String failmsg, String tag) {
        expect(user,expected,msg,failmsg,0,tag);
    }
    public void expect(Object user, Object expected, String msg, String tag) {
        expect(user,expected,msg,0,tag);
    }
     public void expect(Object user, Object expected, String msg, String failmsg, int s) {
        expect(user,expected,msg,failmsg,0,null);
    }
    public void expect(Object user, Object expected, String msg, int s) {
        expect(user,expected,msg,s,null);
    }
    public void expect(Object user, Object expected, String msg) {
        expect(user,expected,msg,0,null);
    }
    public void expect(Object user, Object expected, int s, String tag) {
        expect(user,expected,DEFAULT_PASS,s,tag);
    }
    public void expect(Object user, Object expected, int s) {
        expect(user,expected,DEFAULT_PASS,s);
    }
    public void expect(Object user, Object expected) {
        expect(user,expected,DEFAULT_PASS);
    }
}