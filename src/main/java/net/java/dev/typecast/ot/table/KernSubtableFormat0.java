/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package net.java.dev.typecast.ot.table;

import java.io.DataInput;
import java.io.IOException;

/**
 *
 * @author <a href="mailto:david.schweinsberg@gmail.com">David Schweinsberg</a>
 */
public class KernSubtableFormat0 extends KernSubtable {

    private int nPairs;
    private final int searchRange;
    private final int entrySelector;
    private final int rangeShift;
    private KerningPair[] kerningPairs;

    /** Creates new KernSubtableFormat0 */
    KernSubtableFormat0(final int version, final int length, final int coverage, final DataInput di) throws IOException {
        super(version, length, coverage);
        nPairs = di.readUnsignedShort();
        searchRange = di.readUnsignedShort();
        entrySelector = di.readUnsignedShort();
        rangeShift = di.readUnsignedShort();
        kerningPairs = new KerningPair[nPairs];
        for (int i = 0; i < nPairs; i++) {
            kerningPairs[i] = new KerningPair(di);
        }
    }

    @Override
    public void clearKerningPairs() {
        nPairs = 0;
        kerningPairs = null;
    }

    @Override
    public int getKerningPairCount() {
        return nPairs;
    }

    public int getSearchRange() {
        return searchRange;
    }
    public int getEntrySelector() {
        return entrySelector;
    }
    public int getRangeShift() {
        return rangeShift;
    }

    @Override
    public KerningPair getKerningPair(final int i) {
        return kerningPairs[i];
    }

}
