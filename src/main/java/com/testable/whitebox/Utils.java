package com.testable.whitebox;

/**
 * Utils — intentional Checkstyle / PMD violations:
 * unused locals, short names, long lines, missing javadoc, high branch count.
 */
public class Utils {

    // Long line for Checkstyle LineLength
    public static final String VERY_LONG_CONSTANT = "This is a deliberately very long string constant that exceeds the configured line length limit of one hundred characters and will be flagged by Checkstyle";

    public int calc(int x, int y, int z) {
        int a = x + y;
        int b = a * z;
        return b;
    }

    public int unusedVarsExample() {
        int result = 42;
        String temp = "throwaway";
        int[] another = {1, 2, 3};
        return result;
    }

    public String getLongDescription(int itemId, String itemName, String itemCategory, double itemPrice, String itemCurrency) {
        return "Item ID=" + itemId + ", Name=" + itemName + ", Category=" + itemCategory + ", Price=" + itemPrice + " " + itemCurrency;
    }

    public int noDocstringFunction(int value) {
        return value * 2;
    }

    public int badStyleAggregated(int X, int Y) {
        int Sum = X + Y;
        int Diff = X - Y;
        int unused = Sum * Diff;
        return Sum;
    }

    /** Too many branches — Checkstyle CyclomaticComplexity + PMD. */
    public String highBranchCount(int a, int b, int c, int d, int e, int f) {
        String out = "NONE";
        if (a == 1) {
            out = "A1";
        } else if (a == 2) {
            out = "A2";
        } else if (a == 3) {
            out = "A3";
        }
        if (b == 1) {
            out = out + "-B1";
        } else if (b == 2) {
            out = out + "-B2";
        }
        if (c > 0) {
            out = out + "-C";
        }
        if (d > 0) {
            out = out + "-D";
        }
        if (e > 0) {
            out = out + "-E";
        }
        if (f > 0) {
            out = out + "-F";
        }
        if (a + b + c + d + e + f > 20) {
            out = "OVERFLOW";
        } else if (a + b == 0) {
            out = "ZERO";
        }
        return out;
    }

    public String getUserName(String id) {
        return "user-" + id;
    }

    public int get_user_age(String id) {
        return id == null ? 0 : id.length();
    }
}
