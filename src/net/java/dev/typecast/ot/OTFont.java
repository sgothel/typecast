/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/

package net.java.dev.typecast.ot;

import java.io.DataInputStream;
import java.io.IOException;

import net.java.dev.typecast.ot.table.TTCHeader;
import net.java.dev.typecast.ot.table.TableDirectory;
import net.java.dev.typecast.ot.table.Table;
import net.java.dev.typecast.ot.table.Os2Table;
import net.java.dev.typecast.ot.table.CmapTable;
import net.java.dev.typecast.ot.table.GlyfTable;
import net.java.dev.typecast.ot.table.HeadTable;
import net.java.dev.typecast.ot.table.HheaTable;
import net.java.dev.typecast.ot.table.HmtxTable;
import net.java.dev.typecast.ot.table.LocaTable;
import net.java.dev.typecast.ot.table.MaxpTable;
import net.java.dev.typecast.ot.table.NameTable;
import net.java.dev.typecast.ot.table.PostTable;
import net.java.dev.typecast.ot.table.TableFactory;

/**
 * The TrueType font.
 * @version $Id: OTFont.java,v 1.2 2004-12-09 23:45:02 davidsch Exp $
 * @author <a href="mailto:davidsch@dev.java.net">David Schweinsberg</a>
 */
public class OTFont {

    private OTFontCollection _fc;
    private TableDirectory _tableDirectory = null;
    private Table[] _tables;
    private Os2Table _os2;
    private CmapTable _cmap;
    private GlyfTable _glyf;
    private HeadTable _head;
    private HheaTable _hhea;
    private HmtxTable _hmtx;
    private LocaTable _loca;
    private MaxpTable _maxp;
    private NameTable _name;
    private PostTable _post;

    /**
     * Constructor
     */
    public OTFont(OTFontCollection fc) {
        _fc = fc;
    }

    public Table getTable(int tableType) {
        for (int i = 0; i < _tables.length; i++) {
            if ((_tables[i] != null) && (_tables[i].getType() == tableType)) {
                return _tables[i];
            }
        }
        return null;
    }

    public Os2Table getOS2Table() {
        return _os2;
    }
    
    public CmapTable getCmapTable() {
        return _cmap;
    }
    
    public HeadTable getHeadTable() {
        return _head;
    }
    
    public HheaTable getHheaTable() {
        return _hhea;
    }
    
    public HmtxTable getHmtxTable() {
        return _hmtx;
    }
    
    public LocaTable getLocaTable() {
        return _loca;
    }
    
    public MaxpTable getMaxpTable() {
        return _maxp;
    }

    public NameTable getNameTable() {
        return _name;
    }

    public PostTable getPostTable() {
        return _post;
    }

    public int getAscent() {
        return _hhea.getAscender();
    }

    public int getDescent() {
        return _hhea.getDescender();
    }

    public int getNumGlyphs() {
        return _maxp.getNumGlyphs();
    }

    public Glyph getGlyph(int i) {
        return (_glyf.getDescription(i) != null)
            ? new Glyph(
                _glyf.getDescription(i),
                _hmtx.getLeftSideBearing(i),
                _hmtx.getAdvanceWidth(i))
            : null;
    }

    public TableDirectory getTableDirectory() {
        return _tableDirectory;
    }

    /**
     * @param dis OpenType/TrueType font file data.
     * @param directoryOffset The Table Directory offset within the file.  For a
     * regular TTF/OTF file this will be zero, but for a TTC (Font Collection)
     * the offset is retrieved from the TTC header.  For a Mac font resource,
     * offset is retrieved from the resource headers.
     * @param tablesOrigin The point the table offsets are calculated from.
     * Once again, in a regular TTF file, this will be zero.  In a TTC is is
     * also zero, but within a Mac resource, it is the beggining of the
     * individual font resource data.
     */
    protected void read(
            DataInputStream dis,
            int directoryOffset,
            int tablesOrigin) throws IOException {
        
        // Load the table directory
        dis.reset();
        dis.skip(directoryOffset);
        _tableDirectory = new TableDirectory(dis);
        _tables = new Table[_tableDirectory.getNumTables()];

        // Load each of the tables
        for (int i = 0; i < _tableDirectory.getNumTables(); i++) {
            dis.reset();
            dis.skip(tablesOrigin + _tableDirectory.getEntry(i).getOffset());
            _tables[i] =
                    TableFactory.create(_fc, _tableDirectory.getEntry(i), dis);
        }

        // Get references to commonly used tables (these happen to be all the
        // required tables)
        _os2 = (Os2Table) getTable(Table.OS_2);
        _cmap = (CmapTable) getTable(Table.cmap);
        _glyf = (GlyfTable) getTable(Table.glyf);
        _head = (HeadTable) getTable(Table.head);
        _hhea = (HheaTable) getTable(Table.hhea);
        _hmtx = (HmtxTable) getTable(Table.hmtx);
        _loca = (LocaTable) getTable(Table.loca);
        _maxp = (MaxpTable) getTable(Table.maxp);
        _name = (NameTable) getTable(Table.name);
        _post = (PostTable) getTable(Table.post);

        // Initialize the tables that require it
        _hmtx.init(
                _hhea.getNumberOfHMetrics(),
                _maxp.getNumGlyphs() - _hhea.getNumberOfHMetrics());
        _loca.init(_maxp.getNumGlyphs(), _head.getIndexToLocFormat() == 0);
        _glyf.init(_maxp.getNumGlyphs(), _loca);
    }

    public String toString() {
        if (_tableDirectory != null) {
            return _tableDirectory.toString();
        } else {
            return "Empty font";
        }
    }
}