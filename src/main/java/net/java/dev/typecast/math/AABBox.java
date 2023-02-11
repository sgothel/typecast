/*
 * Typecast - The Font Development Environment
 *
 * Copyright (c) 2004-2015 David Schweinsberg
 * Copyright (c) 2010-2023 JogAmp Community
 * Copyright (c) 2010-2023 Gothel Software e.K.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * And this file is also licensed under the simplified BSD license w/o any warranties etc,
 * same as the Apache 2.0 license.
 */
package net.java.dev.typecast.math;

/**
 * Axis Aligned Bounding Box. Defined by two 3D coordinates (low and high)
 * The low being the the lower left corner of the box, and the high being the upper
 * right corner of the box.
 * <p>
 * This is a simplified variation of the class maintained within JOGL 
 * for compatibility purposes of our patched typecast branch. 
 * </p>
 */
public class AABBox {
    private final float[] low = new float[3];
    private final float[] high = new float[3];
    private final float[] center = new float[3];

    /**
     * Create an Axis Aligned bounding box (AABBox)
     * where the low and and high MAX float Values.
     */
    public AABBox() {
        reset();
    }

    /**
     * Create an AABBox copying all values from the given one
     * @param src the box value to be used for the new instance
     */
    public AABBox(final AABBox src) {
        copy(src);
    }

    /**
     * Create an AABBox specifying the coordinates
     * of the low and high
     * @param lx min x-coordinate
     * @param ly min y-coordnate
     * @param lz min z-coordinate
     * @param hx max x-coordinate
     * @param hy max y-coordinate
     * @param hz max z-coordinate
     */
    public AABBox(final float lx, final float ly, final float lz,
                  final float hx, final float hy, final float hz) {
        setSize(lx, ly, lz, hx, hy, hz);
    }

    /**
     * Create a AABBox defining the low and high
     * @param low min xyz-coordinates
     * @param high max xyz-coordinates
     */
    public AABBox(final float[] low, final float[] high) {
        setSize(low, high);
    }

    /**
     * resets this box to the inverse low/high, allowing the next {@link #resize(float, float, float)} command to hit.
     * @return this AABBox for chaining
     */
    public final AABBox reset() {
        setLow(Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE);
        setHigh(-1*Float.MAX_VALUE,-1*Float.MAX_VALUE,-1*Float.MAX_VALUE);
        center[0] = 0f;
        center[1] = 0f;
        center[2] = 0f;
        return this;
    }

    /** Get the max xyz-coordinates
     * @return a float array containing the max xyz coordinates
     */
    public final float[] getHigh() {
        return high;
    }

    private final void setHigh(final float hx, final float hy, final float hz) {
        this.high[0] = hx;
        this.high[1] = hy;
        this.high[2] = hz;
    }

    /** Get the min xyz-coordinates
     * @return a float array containing the min xyz coordinates
     */
    public final float[] getLow() {
        return low;
    }

    private final void setLow(final float lx, final float ly, final float lz) {
        this.low[0] = lx;
        this.low[1] = ly;
        this.low[2] = lz;
    }

    private final void computeCenter() {
        center[0] = (high[0] + low[0])/2f;
        center[1] = (high[1] + low[1])/2f;
        center[2] = (high[2] + low[2])/2f;
    }

    /**
     * Copy given AABBox 'src' values to this AABBox.
     *
     * @param src source AABBox
     * @return this AABBox for chaining
     */
    public final AABBox copy(final AABBox src) {
        System.arraycopy(src.low, 0, low, 0, 3);
        System.arraycopy(src.high, 0, high, 0, 3);
        System.arraycopy(src.center, 0, center, 0, 3);
        return this;
    }

    /**
     * Set size of the AABBox specifying the coordinates
     * of the low and high.
     *
     * @param low min xyz-coordinates
     * @param high max xyz-coordinates
     * @return this AABBox for chaining
     */
    public final AABBox setSize(final float[] low, final float[] high) {
        return setSize(low[0],low[1],low[2], high[0],high[1],high[2]);
    }

    /**
     * Set size of the AABBox specifying the coordinates
     * of the low and high.
     *
     * @param lx min x-coordinate
     * @param ly min y-coordnate
     * @param lz min z-coordinate
     * @param hx max x-coordinate
     * @param hy max y-coordinate
     * @param hz max z-coordinate
     * @return this AABBox for chaining
     */
    public final AABBox setSize(final float lx, final float ly, final float lz,
                                final float hx, final float hy, final float hz) {
        this.low[0] = lx;
        this.low[1] = ly;
        this.low[2] = lz;
        this.high[0] = hx;
        this.high[1] = hy;
        this.high[2] = hz;
        computeCenter();
        return this;
    }

    /**
     * Resize the AABBox to encapsulate another AABox
     * @param newBox AABBox to be encapsulated in
     * @return this AABBox for chaining
     */
    public final AABBox resize(final AABBox newBox) {
        final float[] newLow = newBox.getLow();
        final float[] newHigh = newBox.getHigh();

        /** test low */
        if (newLow[0] < low[0])
            low[0] = newLow[0];
        if (newLow[1] < low[1])
            low[1] = newLow[1];
        if (newLow[2] < low[2])
            low[2] = newLow[2];

        /** test high */
        if (newHigh[0] > high[0])
            high[0] = newHigh[0];
        if (newHigh[1] > high[1])
            high[1] = newHigh[1];
        if (newHigh[2] > high[2])
            high[2] = newHigh[2];

        computeCenter();
        return this;
    }

    /**
     * Resize the AABBox to encapsulate the passed
     * xyz-coordinates.
     * @param x x-axis coordinate value
     * @param y y-axis coordinate value
     * @param z z-axis coordinate value
     * @return this AABBox for chaining
     */
    public final AABBox resize(final float x, final float y, final float z) {
        /** test low */
        if (x < low[0]) {
            low[0] = x;
        }
        if (y < low[1]) {
            low[1] = y;
        }
        if (z < low[2]) {
            low[2] = z;
        }

        /** test high */
        if (x > high[0]) {
            high[0] = x;
        }
        if (y > high[1]) {
            high[1] = y;
        }
        if (z > high[2]) {
            high[2] = z;
        }

        computeCenter();
        return this;
    }

    /**
     * Resize the AABBox to encapsulate the passed
     * xyz-coordinates.
     * @param xyz xyz-axis coordinate values
     * @param offset of the array
     * @return this AABBox for chaining
     */
    public final AABBox resize(final float[] xyz, final int offset) {
        return resize(xyz[0+offset], xyz[1+offset], xyz[2+offset]);
    }

    /**
     * Resize the AABBox to encapsulate the passed
     * xyz-coordinates.
     * @param xyz xyz-axis coordinate values
     * @return this AABBox for chaining
     */
    public final AABBox resize(final float[] xyz) {
        return resize(xyz[0], xyz[1], xyz[2]);
    }

    /**
     * Check if the x & y coordinates are bounded/contained
     * by this AABBox
     * @param x  x-axis coordinate value
     * @param y  y-axis coordinate value
     * @return true if  x belong to (low.x, high.x) and
     * y belong to (low.y, high.y)
     */
    public final boolean contains(final float x, final float y) {
        if(x<low[0] || x>high[0]){
            return false;
        }
        if(y<low[1]|| y>high[1]){
            return false;
        }
        return true;
    }

    /**
     * Check if the xyz coordinates are bounded/contained
     * by this AABBox.
     * @param x x-axis coordinate value
     * @param y y-axis coordinate value
     * @param z z-axis coordinate value
     * @return true if  x belong to (low.x, high.x) and
     * y belong to (low.y, high.y) and  z belong to (low.z, high.z)
     */
    public final boolean contains(final float x, final float y, final float z) {
        if(x<low[0] || x>high[0]){
            return false;
        }
        if(y<low[1]|| y>high[1]){
            return false;
        }
        if(z<low[2] || z>high[2]){
            return false;
        }
        return true;
    }

    /**
     * Check if there is a common region between this AABBox and the passed
     * 2D region irrespective of z range
     * @param x lower left x-coord
     * @param y lower left y-coord
     * @param w width
     * @param h hight
     * @return true if this AABBox might have a common region with this 2D region
     */
    public final boolean intersects2DRegion(final float x, final float y, final float w, final float h) {
        if (w <= 0 || h <= 0) {
            return false;
        }

        final float _w = getWidth();
        final float _h = getHeight();
        if (_w <= 0 || _h <= 0) {
            return false;
        }

        final float x0 = getMinX();
        final float y0 = getMinY();
        return (x + w > x0 &&
                y + h > y0 &&
                x < x0 + _w &&
                y < y0 + _h);
    }

    /**
     * Get the Center of this AABBox
     * @return the xyz-coordinates of the center of the AABBox
     */
    public final float[] getCenter() {
        return center;
    }

    public final float getMinX() {
        return low[0];
    }

    public final float getMinY() {
        return low[1];
    }

    public final float getMinZ() {
        return low[2];
    }

    public final float getMaxX() {
        return high[0];
    }

    public final float getMaxY() {
        return high[1];
    }

    public final float getMaxZ() {
        return high[2];
    }

    public final float getWidth(){
        return high[0] - low[0];
    }

    public final float getHeight() {
        return high[1] - low[1];
    }

    public final float getDepth() {
        return high[2] - low[2];
    }

    @Override
    public final String toString() {
        return "[ dim "+getWidth()+" x "+getHeight()+" x "+getDepth()+
               ", box "+low[0]+" / "+low[1]+" / "+low[2]+" .. "+high[0]+" / "+high[1]+" / "+high[2]+
               ", ctr "+center[0]+" / "+center[1]+" / "+center[2]+" ]";
    }
}
