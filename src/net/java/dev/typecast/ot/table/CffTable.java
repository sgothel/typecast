/*
 * Typecast - The Font Development Environment
 *
 * Copyright (c) 2004-2015 David Schweinsberg
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

package net.java.dev.typecast.ot.table;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Compact Font Format Table
 * @author <a href="mailto:david.schweinsberg@gmail.com">David Schweinsberg</a>
 */
public class CffTable implements Table {
    
    public static class Dict {
        
        private final Map<Integer, Object> _entries = new HashMap<>();
        private final int[] _data;
        private int _index;
        
        protected Dict(int[] data, int offset, int length) {
            _data = data;
            _index = offset;
            while (_index < offset + length) {
                addKeyAndValueEntry();
            }
        }
        
        protected Dict(DataInput di, int length) throws IOException {
            _data = new int[length];
            for (int i = 0; i < length; ++i) {
                _data[i] = di.readUnsignedByte();
            }
            _index = 0;
            while (_index < length) {
                addKeyAndValueEntry();
            }
        }
        
        public Object getValue(int key) {
            return _entries.get(key);
        }
        
        private boolean addKeyAndValueEntry() {
            ArrayList<Object> operands = new ArrayList<>();
            Object operand = null;
            while (isOperandAtIndex()) {
                operand = nextOperand();
                operands.add(operand);
            }
            int operator = _data[_index++];
            if (operator == 12) {
                operator <<= 8;
                operator |= _data[_index++];
            }
            if (operands.size() == 1) {
                _entries.put(operator, operand);
            } else {
                _entries.put(operator, operands);
            }
            return true;
        }
        
        private boolean isOperandAtIndex() {
            int b0 = _data[_index];
            return (32 <= b0 && b0 <= 254)
                    || b0 == 28
                    || b0 == 29
                    || b0 == 30;
        }

//        private boolean isOperatorAtIndex() {
//            int b0 = _data[_index];
//            return 0 <= b0 && b0 <= 21;
//        }

        private Object nextOperand() {
            int b0 = _data[_index];
            if (32 <= b0 && b0 <= 246) {
                
                // 1 byte integer
                ++_index;
                return b0 - 139;
            } else if (247 <= b0 && b0 <= 250) {
                
                // 2 byte integer
                int b1 = _data[_index + 1];
                _index += 2;
                return (b0 - 247) * 256 + b1 + 108;
            } else if (251 <= b0 && b0 <= 254) {
                
                // 2 byte integer
                int b1 = _data[_index + 1];
                _index += 2;
                return -(b0 - 251) * 256 - b1 - 108;
            } else if (b0 == 28) {
                
                // 3 byte integer
                int b1 = _data[_index + 1];
                int b2 = _data[_index + 2];
                _index += 3;
                return b1 << 8 | b2;
            } else if (b0 == 29) {
                
                // 5 byte integer
                int b1 = _data[_index + 1];
                int b2 = _data[_index + 2];
                int b3 = _data[_index + 3];
                int b4 = _data[_index + 4];
                _index += 5;
                return b1 << 24 | b2 << 16 | b3 << 8 | b4;
            } else if (b0 == 30) {
                
                // Real number
                StringBuilder fString = new StringBuilder();
                int nibble1 = 0;
                int nibble2 = 0;
                ++_index;
                while ((nibble1 != 0xf) && (nibble2 != 0xf)) {
                    nibble1 = _data[_index] >> 4;
                    nibble2 = _data[_index] & 0xf;
                    ++_index;
                    fString.append(decodeRealNibble(nibble1));
                    fString.append(decodeRealNibble(nibble2));
                }                
                return Float.valueOf(fString.toString());
            } else {
                return null;
            }
        }
        
        private String decodeRealNibble(int nibble) {
            if (nibble < 0xa) {
                return Integer.toString(nibble);
            } else if (nibble == 0xa) {
                return ".";
            } else if (nibble == 0xb) {
                return "E";
            } else if (nibble == 0xc) {
                return "E-";
            } else if (nibble == 0xe) {
                return "-";
            }
            return "";
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Iterator<Integer> keys = _entries.keySet().iterator();
            while (keys.hasNext()) {
                Integer key = keys.next();
                if ((key & 0xc00) == 0xc00) {
                    sb.append("12 ").append(key & 0xff).append(": ");
                } else {
                    sb.append(key.toString()).append(": ");
                }
                sb.append(_entries.get(key).toString()).append("\n");
            }
            return sb.toString();
        }
    }
    
    public class Index {
        
        private final int _count;
        private final int _offSize;
        private final int[] _offset;
        private final int[] _data;
        
        protected Index(DataInput di) throws IOException {
            _count = di.readUnsignedShort();
            _offset = new int[_count + 1];
            _offSize = di.readUnsignedByte();
            for (int i = 0; i < _count + 1; ++i) {
                int thisOffset = 0;
                for (int j = 0; j < _offSize; ++j) {
                    thisOffset |= di.readUnsignedByte() << ((_offSize - j - 1) * 8);
                }
                _offset[i] = thisOffset;
            }
            _data = new int[getDataLength()];
            for (int i = 0; i < getDataLength(); ++i) {
                _data[i] = di.readUnsignedByte();
            }
        }
        
        public final int getCount() {
            return _count;
        }
        
        public final int getOffset(int index) {
            return _offset[index];
        }
        
        public final int getDataLength() {
            return _offset[_offset.length - 1] - 1;
        }
        
        public final int[] getData() {
            return _data;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("DICT\n");
            sb.append("count: ").append(_count).append("\n");
            sb.append("offSize: ").append(_offSize).append("\n");
            for (int i = 0; i < _count + 1; ++i) {
                sb.append("offset[").append(i).append("]: ").append(_offset[i]).append("\n");
            }
            sb.append("data:");
            for (int i = 0; i < _data.length; ++i) {
                if (i % 8 == 0) {
                    sb.append("\n");
                } else {
                    sb.append(" ");
                }
                sb.append(_data[i]);
            }
            sb.append("\n");
            return sb.toString();
        }
    }
    
    public class TopDictIndex extends Index {

        protected TopDictIndex(DataInput di) throws IOException {
            super(di);
        }
        
        public Dict getTopDict(int index) {
            int offset = getOffset(index) - 1;
            int len = getOffset(index + 1) - offset - 1;
            return new Dict(getData(), offset, len);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < getCount(); ++i) {
                sb.append(getTopDict(i).toString()).append("\n");
            }
            return sb.toString();
        }
    }
    
    public class NameIndex extends Index {

        protected NameIndex(DataInput di) throws IOException {
            super(di);
        }
        
        public String getName(int index) {
            String name = null;
            int offset = getOffset(index) - 1;
            int len = getOffset(index + 1) - offset - 1;

            // Ensure the name hasn't been deleted
            if (getData()[offset] != 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = offset; i < offset + len; ++i) {
                    sb.append((char) getData()[i]);
                }
                name = sb.toString();
            } else {
                name = "DELETED NAME";
            }
            return name;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < getCount(); ++i) {
                sb.append(getName(i)).append("\n");
            }
            return sb.toString();
        }
    }

    public class StringIndex extends Index {

        protected StringIndex(DataInput di) throws IOException {
            super(di);
        }
        
        public String getString(int index) {
            if (index < CffStandardStrings.standardStrings.length) {
                return CffStandardStrings.standardStrings[index];
            } else {
                index -= CffStandardStrings.standardStrings.length;
                if (index >= getCount()) {
                    return null;
                }
                int offset = getOffset(index) - 1;
                int len = getOffset(index + 1) - offset - 1;

                StringBuilder sb = new StringBuilder();
                for (int i = offset; i < offset + len; ++i) {
                    sb.append((char) getData()[i]);
                }
                return sb.toString();
            }
        }
        
        @Override
        public String toString() {
            int nonStandardBase = CffStandardStrings.standardStrings.length;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < getCount(); ++i) {
                sb.append(nonStandardBase + i).append(": ");
                sb.append(getString(nonStandardBase + i)).append("\n");
            }
            return sb.toString();
        }
    }
    
    private class CharsetRange {
        
        private int _first;
        private int _left;
        
        public final int getFirst() {
            return _first;
        }

        protected final void setFirst(int first) {
            _first = first;
        }
        
        public final int getLeft() {
            return _left;
        }

        protected final void setLeft(int left) {
            _left = left;
        }
    }

    private class CharsetRange1 extends CharsetRange {
        
        protected CharsetRange1(DataInput di) throws IOException {
            setFirst(di.readUnsignedShort());
            setLeft(di.readUnsignedByte());
        }
    }
    
    private class CharsetRange2 extends CharsetRange {
        
        protected CharsetRange2(DataInput di) throws IOException {
            setFirst(di.readUnsignedShort());
            setLeft(di.readUnsignedShort());
        }
    }
    
    private abstract class Charset {
        
        public abstract int getFormat();
        
        public abstract int getSID(int gid);
    }
    
    private class CharsetFormat0 extends Charset {
        
        private final int[] _glyph;
        
        protected CharsetFormat0(DataInput di, int glyphCount) throws IOException {
            _glyph = new int[glyphCount - 1];  // minus 1 because .notdef is omitted
            for (int i = 0; i < glyphCount - 1; ++i) {
                _glyph[i] = di.readUnsignedShort();
            }
        }
        
        @Override
        public int getFormat() {
            return 0;
        }

        @Override
        public int getSID(int gid) {
            if (gid == 0) {
                return 0;
            }
            return _glyph[gid - 1];
        }
    }
    
    private class CharsetFormat1 extends Charset {
        
        private final ArrayList<CharsetRange> _charsetRanges = new ArrayList<>();
        
        protected CharsetFormat1(DataInput di, int glyphCount) throws IOException {
            int glyphsCovered = glyphCount - 1;  // minus 1 because .notdef is omitted
            while (glyphsCovered > 0) {
                CharsetRange range = new CharsetRange1(di);
                _charsetRanges.add(range);
                glyphsCovered -= range.getLeft() + 1;
            }
        }

        @Override
        public int getFormat() {
            return 1;
        }

        @Override
        public int getSID(int gid) {
            if (gid == 0) {
                return 0;
            }
            
            // Count through the ranges to find the one of interest
            int count = 1;
            for (CharsetRange range : _charsetRanges) {
                if (gid <= range.getLeft() + count) {
                    int sid = gid - count + range.getFirst();
                    return sid;
                }
                count += range.getLeft() + 1;
            }
            return 0;
        }
    }

    private class CharsetFormat2 extends Charset {
        
        private final ArrayList<CharsetRange> _charsetRanges = new ArrayList<>();
        
        protected CharsetFormat2(DataInput di, int glyphCount) throws IOException {
            int glyphsCovered = glyphCount - 1;  // minus 1 because .notdef is omitted
            while (glyphsCovered > 0) {
                CharsetRange range = new CharsetRange2(di);
                _charsetRanges.add(range);
                glyphsCovered -= range.getLeft() + 1;
            }
        }

        @Override
        public int getFormat() {
            return 2;
        }

        @Override
        public int getSID(int gid) {
            if (gid == 0) {
                return 0;
            }
            
            // Count through the ranges to find the one of interest
            int count = 1;
            for (CharsetRange range : _charsetRanges) {
                if (gid <= range.getLeft() + count) {
                    int sid = gid - count + range.getFirst();
                    return sid;
                }
                count += range.getLeft() + 1;
            }
            return 0;
        }
    }
    
    public class CffFont {
        private Index _charStringsIndex;
        private Dict _privateDict;
        private Index _localSubrsIndex;
        private Charset _charset;
        private Charstring[] _charstrings;
        
        public CffFont(
                Index charStringsIndex,
                Dict privateDict,
                Index localSubrsIndex,
                Charset charset,
                Charstring[] charstrings) {
            _charStringsIndex = charStringsIndex;
            _privateDict = privateDict;
            _localSubrsIndex = localSubrsIndex;
            _charset = charset;
            _charstrings = charstrings;
        }
        
        public Index getCharStringsIndex() {
            return _charStringsIndex;
        }
        
        public Dict getPrivateDict() {
            return _privateDict;
        }
        
        public Index getLocalSubrsIndex() {
            return _localSubrsIndex;
        }
        
        public Charset getCharset() {
            return _charset;
        }
        
        public Charstring[] getCharstrings() {
            return _charstrings;
        }
    }
    
    private DirectoryEntry _de;
    private int _major;
    private int _minor;
    private int _hdrSize;
    private int _offSize;
    private NameIndex _nameIndex;
    private TopDictIndex _topDictIndex;
    private StringIndex _stringIndex;
    private Index _globalSubrIndex;
    private CffFont[] _fonts;

    private byte[] _buf;

    /** Creates a new instance of CffTable
     * @param de
     * @param di
     * @throws java.io.IOException */
    protected CffTable(DirectoryEntry de, DataInput di) throws IOException {
        _de = (DirectoryEntry) de.clone();

        // Load entire table into a buffer, and create another input stream
        _buf = new byte[de.getLength()];
        di.readFully(_buf);
        DataInput di2 = getDataInputForOffset(0);

        // Header
        _major = di2.readUnsignedByte();
        _minor = di2.readUnsignedByte();
        _hdrSize = di2.readUnsignedByte();
        _offSize = di2.readUnsignedByte();
        
        // Name INDEX
        di2 = getDataInputForOffset(_hdrSize);
        _nameIndex = new NameIndex(di2);
        
        // Top DICT INDEX
        _topDictIndex = new TopDictIndex(di2);

        // String INDEX
        _stringIndex = new StringIndex(di2);
        
        // Global Subr INDEX
        _globalSubrIndex = new Index(di2);
        
        // TESTING
        Charstring gscs = new CharstringType2(
                0,
                "Global subrs",
                _globalSubrIndex.getData(),
                _globalSubrIndex.getOffset(0) - 1,
                _globalSubrIndex.getDataLength());
        System.out.println(gscs.toString());

        // Encodings go here -- but since this is an OpenType font will this
        // not always be a CIDFont?  In which case there are no encodings
        // within the CFF data.
        
        // Load each of the fonts
        _fonts = new CffFont[_topDictIndex.getCount()];
        for (int i = 0; i < _topDictIndex.getCount(); ++i) {

            // Charstrings INDEX
            // We load this before Charsets because we may need to know the number
            // of glyphs
            Integer charStringsOffset = (Integer) _topDictIndex.getTopDict(i).getValue(17);
            di2 = getDataInputForOffset(charStringsOffset);
            Index charStringsIndex = new Index(di2);
            int glyphCount = charStringsIndex.getCount();

            // Private DICT
            List<Integer> privateSizeAndOffset = (List<Integer>) _topDictIndex.getTopDict(i).getValue(18);
            di2 = getDataInputForOffset(privateSizeAndOffset.get(1));
            Dict privateDict = new Dict(di2, privateSizeAndOffset.get(0));
            
            // Local Subrs INDEX
            Index localSubrsIndex = null;
            Integer localSubrsOffset = (Integer) privateDict.getValue(19);
            if (localSubrsOffset != null) {
                di2 = getDataInputForOffset(privateSizeAndOffset.get(1) + localSubrsOffset);
                localSubrsIndex = new Index(di2);
            }
        
            // Charsets
            Charset charset = null;
            Integer charsetOffset = (Integer) _topDictIndex.getTopDict(i).getValue(15);
            di2 = getDataInputForOffset(charsetOffset);
            int format = di2.readUnsignedByte();
            switch (format) {
                case 0:
                    charset = new CharsetFormat0(di2, glyphCount);
                    break;
                case 1:
                    charset = new CharsetFormat1(di2, glyphCount);
                    break;
                case 2:
                    charset = new CharsetFormat2(di2, glyphCount);
                    break;
            }

            // Create the charstrings
            Charstring[] charstrings = new Charstring[glyphCount];
            for (int j = 0; j < glyphCount; ++j) {
                int offset = charStringsIndex.getOffset(j) - 1;
                int len = charStringsIndex.getOffset(j + 1) - offset - 1;
                charstrings[j] = new CharstringType2(
                        i,
                        _stringIndex.getString(charset.getSID(j)),
                        charStringsIndex.getData(),
                        offset,
                        len);
            }
            
            _fonts[i] = new CffFont(charStringsIndex, privateDict, localSubrsIndex, charset, charstrings);
        }
    }
    
    private DataInput getDataInputForOffset(int offset) {
        return new DataInputStream(new ByteArrayInputStream(
                _buf, offset,
                _de.getLength() - offset));
    }

    public NameIndex getNameIndex() {
        return _nameIndex;
    }
    
    public Index getGlobalSubrIndex() {
        return _globalSubrIndex;
    }

    public CffFont getFont(int fontIndex) {
        return _fonts[fontIndex];
    }

//    public Charset getCharset(int fontIndex) {
//        return _charsets[fontIndex];
//    }

    public Charstring getCharstring(int fontIndex, int gid) {
        return _fonts[fontIndex].getCharstrings()[gid];
    }
    
    public int getCharstringCount(int fontIndex) {
        return _fonts[fontIndex].getCharstrings().length;
    }
    
    @Override
    public int getType() {
        return CFF;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("'CFF' Table - Compact Font Format\n---------------------------------\n");
        sb.append("\nName INDEX\n");
        sb.append(_nameIndex.toString());
        sb.append("\nTop DICT INDEX\n");
        sb.append(_topDictIndex.toString());
        sb.append("\nString INDEX\n");
        sb.append(_stringIndex.toString());
        sb.append("\nGlobal Subr INDEX\n");
        sb.append(_globalSubrIndex.toString());
        for (int i = 0; i < _fonts.length; ++i) {
            sb.append("\nCharStrings INDEX ").append(i).append("\n");
            sb.append(_fonts[i].getCharStringsIndex().toString());
        }
        return sb.toString();
    }
    
    /**
     * Get a directory entry for this table.  This uniquely identifies the
     * table in collections where there may be more than one instance of a
     * particular table.
     * @return A directory entry
     */
    @Override
    public DirectoryEntry getDirectoryEntry() {
        return _de;
    }
}
