package edu.stanford.cs276.util;

import edu.stanford.cs276.Stemmer;

public class StemmingUtils {

    public static String getStemmedWord(String preStem) {
        Stemmer s = new Stemmer();

        char[] ch = preStem.toCharArray();
        for (int c = 0; c < preStem.length(); c++) s.add(ch[c]);

        s.stem();

        String u;
       /* and now, to test toString() : */
        u = s.toString();
                      /* to test getResultBuffer(), getResultLength() : */
                      /* u = new String(s.getResultBuffer(), 0, s.getResultLength()); */

        //System.out.print(u);

        return u;
    }

}
