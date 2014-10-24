package edu.umass.ckc.wo.util;

import java.util.List;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 23, 2008
 * Time: 5:01:55 PM
 */
public class Lists {

    public static boolean inList (int pid, List<String> attemptedProbs) {
        for (String id: attemptedProbs)
            if (pid == Integer.parseInt(id))
                return true ;
        return false;
    }


    public static int indexOf (int[] a, int val) {
        for (int i=0;i<a.length;i++)
            if (a[i] == val)
                return i;
        return -1;
    }
}
