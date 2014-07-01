package net.pdiaz.cainsearcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by pdiaz on 30/06/2014.
 */
public class Util {
    /**
     * Method to count the number of lines, in scala I don't know how to do this efficiently...
     * Processing cain.txt file it takes 20ms with this method and ~300ms doing it with my scala
     * knowledge...
     * <code>
     *     // Scala slow way
     *     Source.fromFile(...).count(_=='\n')
     * </code>
     * @param is
     * @return
     * @throws IOException
     */
    public static int countLines(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);

        int count = 0;
        char buf[] = new char[4096];
        int i;

        while((i=isr.read(buf)) != -1) {
            for(int j=0; j<i; j++) {
                if(buf[j] == '\n') count++;
            }
        }

        return count;
    }

}
