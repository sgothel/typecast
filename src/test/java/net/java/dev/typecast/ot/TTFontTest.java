/*
 * Typecast
 *
 * Copyright © 2004-2019 David Schweinsberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.java.dev.typecast.ot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.java.dev.typecast.ot.table.HeadTable;

public class TTFontTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TTFontTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TTFontTest.class);
    }

    public void testLoadFont() throws URISyntaxException, IOException {
        TTFont font = loadFontResource("Lato-Regular.ttf");
        assertEquals(HeadTable.class, font.getHeadTable().getClass());

        dumpFont("Lato-Regular.txt", font);
    }

    public void testLoadColorFont() throws URISyntaxException, IOException {
        TTFont font = loadFontResource("NotoColorEmoji.ttf");
        dumpFont("NotoColorEmoji.txt", font);
    }
    
    public void testLoadColorFont2() throws URISyntaxException, IOException {
        TTFont font = loadFontResource("Gilbert-Color Bold Preview5.otf");
        dumpFont("Gilbert-Color Bold Preview5.txt", font);
    }
    
    private void dumpFont(String name, TTFont font)
            throws IOException, FileNotFoundException {
        new File("target/tmp").mkdirs();
        try (Writer out = new OutputStreamWriter(new FileOutputStream(new File("target/tmp/" + name)))) {
            font.dumpTo(out);
        }
    }

    private TTFont loadFontResource(String name)
            throws URISyntaxException, IOException {
        return loadFont( ClassLoader.getSystemResource(name) );
    }

    private TTFont loadFont(URL url) throws URISyntaxException, IOException {
        final URLConnection con = url.openConnection();
        final int len = con.getContentLength();
        try(InputStream is = con.getInputStream()) {
            return new TTFont(is, len);
        }
    }
}
