/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.transform;

/**
 * A 4x4 matrix for 3-D transformations
 */
public class LXMatrix {

    public float
        m11 = 1, m12 = 0, m13 = 0, m14 = 0,
        m21 = 0, m22 = 1, m23 = 0, m24 = 0,
        m31 = 0, m32 = 0, m33 = 1, m34 = 0,
        m41 = 0, m42 = 0, m43 = 0, m44 = 1;

    /**
     * Makes a new identity matrix.
     */
    public LXMatrix() {}

    public LXMatrix(float[] m) {
        if (m.length != 16) {
            throw new IllegalArgumentException("LXMatrix must have 16 values");
        }
        this.m11 = m[0];
        this.m12 = m[1];
        this.m13 = m[2];
        this.m14 = m[3];
        this.m21 = m[4];
        this.m22 = m[5];
        this.m23 = m[6];
        this.m24 = m[7];
        this.m31 = m[8];
        this.m32 = m[9];
        this.m33 = m[10];
        this.m34 = m[11];
        this.m41 = m[12];
        this.m42 = m[13];
        this.m43 = m[14];
        this.m44 = m[15];
    }

    public LXMatrix(float m11, float m12, float m13, float m14,
                                    float m21, float m22, float m23, float m24,
                                    float m31, float m32, float m33, float m34,
                                    float m41, float m42, float m43, float m44) {
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m14 = m14;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m24 = m24;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        this.m34 = m34;
        this.m41 = m41;
        this.m42 = m42;
        this.m43 = m43;
        this.m44 = m44;
    }

    /**
     * Copies the existing matrix
     *
     * @param m matrix
     */
    public LXMatrix(LXMatrix m) {
        m11 = m.m11;
        m12 = m.m12;
        m13 = m.m13;
        m14 = m.m14;
        m21 = m.m21;
        m22 = m.m22;
        m23 = m.m23;
        m24 = m.m24;
        m31 = m.m31;
        m32 = m.m32;
        m33 = m.m33;
        m34 = m.m34;
        m41 = m.m41;
        m42 = m.m42;
        m43 = m.m43;
        m44 = m.m44;
    }

    /**
     * Multiplies the matrix by another matrix
     *
     * @param m Matrix to multiply by
     */
    public LXMatrix multiply(LXMatrix m) {
        return multiply(
            m.m11, m.m12, m.m13, m.m14,
            m.m21, m.m22, m.m23, m.m24,
            m.m31, m.m32, m.m33, m.m34,
            m.m41, m.m42, m.m43, m.m44
        );
    }

    public LXMatrix multiply(float b11, float b12, float b13, float b14,
                                                     float b21, float b22, float b23, float b24,
                                                     float b31, float b32, float b33, float b34,
                                                     float b41, float b42, float b43, float b44) {

        float a11 = m11 * b11 + m12 * b21 + m13 * b31 + m14 * b41;
        float a12 = m11 * b12 + m12 * b22 + m13 * b32 + m14 * b42;
        float a13 = m11 * b13 + m12 * b23 + m13 * b33 + m14 * b43;
        float a14 = m11 * b14 + m12 * b24 + m13 * b34 + m14 * b44;

        float a21 = m21 * b11 + m22 * b21 + m23 * b31 + m24 * b41;
        float a22 = m21 * b12 + m22 * b22 + m23 * b32 + m24 * b42;
        float a23 = m21 * b13 + m22 * b23 + m23 * b33 + m24 * b43;
        float a24 = m21 * b14 + m22 * b24 + m23 * b34 + m24 * b44;

        float a31 = m31 * b11 + m32 * b21 + m33 * b31 + m34 * b41;
        float a32 = m31 * b12 + m32 * b22 + m33 * b32 + m34 * b42;
        float a33 = m31 * b13 + m32 * b23 + m33 * b33 + m34 * b43;
        float a34 = m31 * b14 + m32 * b24 + m33 * b34 + m34 * b44;

        float a41 = m41 * b11 + m42 * b21 + m43 * b31 + m44 * b41;
        float a42 = m41 * b12 + m42 * b22 + m43 * b32 + m44 * b42;
        float a43 = m41 * b13 + m42 * b23 + m43 * b33 + m44 * b43;
        float a44 = m41 * b14 + m42 * b24 + m43 * b34 + m44 * b44;

        m11 = a11;
        m12 = a12;
        m13 = a13;
        m14 = a14;
        m21 = a21;
        m22 = a22;
        m23 = a23;
        m24 = a24;
        m31 = a31;
        m32 = a32;
        m33 = a33;
        m34 = a34;
        m41 = a41;
        m42 = a42;
        m43 = a43;
        m44 = a44;

        return this;
    }

    public float x() {
        return m14;
    }

    public float y() {
        return m24;
    }

    public float z() {
        return m34;
    }

    public LXMatrix scale(float sv) {
        return scale(sv, sv, sv);
    }

    public LXMatrix scaleX(float sx) {
        return scale(sx, 1, 1);
    }

    public LXMatrix scaleY(float sy) {
        return scale(1, sy, 1);
    }

    public LXMatrix scaleZ(float sz) {
        return scale(1, 1, sz);
    }

    public LXMatrix scale(float sx, float sy, float sz) {
        return multiply(
            sx,  0,  0,  0,
             0, sy,  0,  0,
             0,  0, sz,  0,
             0,  0,  0,  1
        );
    }

    public LXMatrix translate(float tx, float ty, float tz) {
        return multiply(
            1,  0,  0, tx,
            0,  1,  0, ty,
            0,  0,  1, tz,
            0,  0,  0,  1
        );
    }

    public LXMatrix rotateX(float rx) {
        float cos = (float) Math.cos(rx);
        float sin = (float) Math.sin(rx);
        return multiply(
            1,    0,    0,  0,
            0,  cos, -sin,  0,
            0,  sin,  cos,  0,
            0,    0,    0,  1
        );
    }

    public LXMatrix rotateY(float ry) {
        float cos = (float) Math.cos(ry);
        float sin = (float) Math.sin(ry);
        return multiply(
             cos,  0, sin,  0,
                 0,  1,   0,  0,
            -sin,  0, cos,  0,
                 0,  0,   0,  1
        );
    }

    public LXMatrix rotateZ(float rz) {
        float cos = (float) Math.cos(rz);
        float sin = (float) Math.sin(rz);
        return multiply(
            cos, -sin,  0,  0,
            sin,  cos,  0,  0,
                0,    0,  1,  0,
                0,    0,  0,  1
        );
    }
}