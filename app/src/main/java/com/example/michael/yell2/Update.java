package com.example.michael.yell2;

import java.math.BigInteger;

public class Update implements Comparable<Update> {

    String content;
    String from;
    String ms;
    String id;

    public Update(String from, String ms, String content, String id) {
        this.from = from;
        this.ms = ms;
        this.content = content;
        this.id = id;
    }

    public String toString() {
        return from + "::" + id;
    }

    public boolean equals(Update other) {
        return id.equals(other.id);
    }

    public int compareTo(Update other) {
        BigInteger thisOne = new BigInteger(ms);
        BigInteger thatOne = new BigInteger(other.ms);
        int x = thisOne.subtract(thatOne).compareTo(new BigInteger("0"));   // x > 0 ==> 1; x < 0 ==> -1
                                                                            // newer ==> 1; older ==> -1
        return x * -1;
    }

}
