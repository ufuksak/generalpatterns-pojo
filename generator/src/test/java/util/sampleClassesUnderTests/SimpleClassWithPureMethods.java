package util.sampleClassesUnderTests;

public class SimpleClassWithPureMethods {

    public boolean isValid(boolean a, boolean b) {
        return a&&b;
    }

    public int addNumbers(int a, int b) {
        return a+b;
    }

    public char[] build(char a, char b) {
        char[] result = new char[2];
        result[0] = a;
        result[1] = b;
        return result;
    }

    // not primitive, but let's roll with it
    public String addPrefix(String a) {
        return "prefix"+a;
    }

}
