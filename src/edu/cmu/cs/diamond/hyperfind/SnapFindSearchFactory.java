/*
 *  HyperFind, an search application for the OpenDiamond platform
 *
 *  Copyright (c) 2009-2010 Carnegie Mellon University
 *  All rights reserved.
 *
 *  HyperFind is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 2.
 *
 *  HyperFind is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with HyperFind. If not, see <http://www.gnu.org/licenses/>.
 *
 *  Linking HyperFind statically or dynamically with other modules is
 *  making a combined work based on HyperFind. Thus, the terms and
 *  conditions of the GNU General Public License cover the whole
 *  combination.
 * 
 *  In addition, as a special exception, the copyright holders of
 *  HyperFind give you permission to combine HyperFind with free software
 *  programs or libraries that are released under the GNU LGPL or the
 *  Eclipse Public License 1.0. You may copy and distribute such a system
 *  following the terms of the GNU GPL for HyperFind and the licenses of
 *  the other code concerned, provided that you include the source code of
 *  that other code when and as the GNU GPL requires distribution of source
 *  code.
 *
 *  Note that people who make modified versions of HyperFind are not
 *  obligated to grant this special exception for their modified versions;
 *  it is their choice whether to do so. The GNU General Public License
 *  gives permission to release a modified version without this exception;
 *  this exception also makes it possible to release a modified version
 *  which carries forward this exception.
 */

package edu.cmu.cs.diamond.hyperfind;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class SnapFindSearchFactory extends HyperFindSearchFactory {

    private final File pluginRunner;

    /*
     * (non-Javadoc)
     * 
     * @see edu.cmu.cs.diamond.hyperfind.HyperFindSearchFactory#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    public String getInternalName() {
        return internalName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.cmu.cs.diamond.hyperfind.HyperFindSearchFactory#getType()
     */
    @Override
    public HyperFindSearchType getType() {
        return type;
    }

    private final String displayName;

    private final String internalName;

    private final HyperFindSearchType type;

    private final boolean needsPatches;

    public SnapFindSearchFactory(File pluginRunner, Map<String, byte[]> map) {
        this.pluginRunner = pluginRunner;

        Charset utf8 = Charset.forName("UTF-8");

        displayName = new String(getOrFail(map, "display-name"), utf8);
        internalName = new String(getOrFail(map, "internal-name"), utf8);
        needsPatches = Boolean.parseBoolean(new String(getOrFail(map,
                "needs-patches"), utf8));

        String typeString = new String(getOrFail(map, "type"), utf8);
        try {
            type = HyperFindSearchType.fromString(typeString);
        } catch (IllegalArgumentException e) {
            throw new UnknownSearchTypeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.cmu.cs.diamond.hyperfind.HyperFindSearchFactory#createHyperFindSearch
     * ()
     */
    @Override
    public HyperFindSearch createHyperFindSearch() throws IOException,
            InterruptedException {
        return new SnapFindSearch(pluginRunner, displayName, internalName,
                type, needsPatches);
    }

    static <V> V getOrFail(Map<String, V> map, String key) {
        V value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Cannot find key: " + key);
        }

        return value;
    }

    public static List<HyperFindSearchFactory> createHyperFindSearchFactories(
            File pluginRunner) throws IOException, InterruptedException {
        ArrayList<HyperFindSearchFactory> result = new ArrayList<HyperFindSearchFactory>();

        Process p = new ProcessBuilder(pluginRunner.getPath(), "list-plugins")
                .start();

        DataInputStream in = new DataInputStream(p.getInputStream());

        List<Map<String, byte[]>> listPluginsResult = readKeyValueSetList(in);
        for (Map<String, byte[]> map : listPluginsResult) {
            try {
                result.add(new SnapFindSearchFactory(pluginRunner, map));
            } catch (UnknownSearchTypeException ignore) {
            }
        }

        if (p.waitFor() != 0) {
            throw new IOException("Bad result for list-plugins");
        }

        // System.out.println(result);
        return result;
    }

    static List<Map<String, byte[]>> readKeyValueSetList(DataInputStream in)
            throws IOException {
        List<Map<String, byte[]>> result = new ArrayList<Map<String, byte[]>>();

        while (true) {
            Map<String, byte[]> m = readKeyValueSet(in);
            if (m.isEmpty()) {
                break;
            }
            result.add(m);
        }

        return result;
    }

    static Map<String, byte[]> readKeyValueSet(DataInputStream in)
            throws IOException {
        Map<String, byte[]> result = new HashMap<String, byte[]>();

        boolean inMiddle = false;
        try {
            while (true) {
                expect("K ".getBytes(), in);

                inMiddle = true;

                int keyLen = readLength(in);
                String key = new String(readData(in, keyLen), "UTF-8");

                expect("V ".getBytes(), in);
                int valueLen = readLength(in);
                byte[] value = readData(in, valueLen);

                result.put(key, value);
                inMiddle = false;
            }
        } catch (IOException e) {
            if (inMiddle) {
                throw e;
            }
        }

        return result;
    }

    private static byte[] readData(DataInputStream in, int len)
            throws IOException {
        byte[] buf = new byte[len];
        in.readFully(buf);

        // read newline
        in.readByte();

        return buf;
    }

    private static int readLength(DataInputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int c = in.readByte() & 0xFF;
            if (c == '\n') {
                return Integer.parseInt(sb.toString());
            } else {
                sb.append((char) c);
            }
        }
    }

    private static void expect(byte[] expected, DataInputStream in)
            throws IOException {
        for (int i = 0; i < expected.length; i++) {
            int c = in.readByte() & 0xFF;
            if (c != (expected[i] & 0xFF)) {
                throw new IOException("expected " + (expected[i] & 0xFF)
                        + ", got " + c);
            }
        }
    }

    @Override
    public String toString() {
        return displayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.cmu.cs.diamond.hyperfind.HyperFindSearchFactory#needsPatches()
     */
    @Override
    public boolean needsPatches() {
        return needsPatches;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.cmu.cs.diamond.hyperfind.HyperFindSearchFactory#createHyperFindSearch
     * (java.util.List)
     */
    @Override
    public HyperFindSearch createHyperFindSearch(List<BufferedImage> patches)
            throws IOException, InterruptedException {
        return new SnapFindSearch(pluginRunner, displayName, internalName,
                type, needsPatches, patches);
    }

    @Override
    public HyperFindSearch createHyperFindSearchFromZipMap(
            Map<String, byte[]> zipMap, Properties p) {
        return null;
    }
}
