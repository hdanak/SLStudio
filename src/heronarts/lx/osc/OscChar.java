/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.osc;

import java.nio.ByteBuffer;

public class OscChar implements OscArgument {

    private char value = 0;

    public OscChar() {}

    public OscChar(char value) {
        this.value = value;
    }

    public OscChar setValue(char value) {
        this.value = value;
        return this;
    }

    public char getValue() {
        return this.value;
    }

    @Override
    public int getByteLength() {
        return 4;
    }

    @Override
    public char getTypeTag() {
        return OscTypeTag.CHAR;
    }

    @Override
    public String toString() {
        return Character.toString(this.value);
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putChar(this.value);
        buffer.putChar(this.value);
        buffer.putChar(this.value);
        buffer.putChar(this.value);
    }

    @Override
    public int toInt() {
        return this.value;
    }

    @Override
    public float toFloat() {
        return 0;
    }

    @Override
    public double toDouble() {
        return 0;
    }
}
