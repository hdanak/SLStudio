/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

package heronarts.lx.midi;

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;

public abstract class LXMidiMapping {

    public enum Type {
        NOTE,
        CONTROL_CHANGE
    };

    public final int channel;

    public final Type type;

    public final LXParameter parameter;

    protected LXMidiMapping(int channel, Type type, LXParameter parameter) {
        this.channel = channel;
        this.type = type;
        this.parameter = parameter;
    }

    static boolean isValidMessageType(LXShortMessage message) {
        return (message instanceof MidiNote) || (message instanceof MidiControlChange);
    }

    static LXMidiMapping create(LXShortMessage message, LXParameter parameter) {
        if (message instanceof MidiNote) {
            return new Note((MidiNote) message, parameter);
        } else if (message instanceof MidiControlChange) {
            return new ControlChange((MidiControlChange) message, parameter);
        }
        throw new IllegalArgumentException("Not a valid message type for a MIDI mapping: " + message);
    }


    abstract boolean matches(LXShortMessage message);
    abstract void apply(LXShortMessage message);

    public abstract String getDescription();

    protected void setValue(boolean value) {
        if (this.parameter instanceof BooleanParameter) {
            ((BooleanParameter) this.parameter).setValue(value);
        } else if (this.parameter instanceof LXNormalizedParameter) {
            ((LXNormalizedParameter) this.parameter).setNormalized(value ? 1 : 0);
        } else {
            this.parameter.setValue(value ? 1 : 0);
        }
    }

    protected void setNormalized(double normalized) {
        if (this.parameter instanceof LXNormalizedParameter) {
            ((LXNormalizedParameter) this.parameter).setNormalized(normalized);
        } else {
            this.parameter.setValue(normalized);
        }
    }

    protected void toggleValue() {
        if (this.parameter instanceof BooleanParameter) {
            ((BooleanParameter) this.parameter).toggle();
        } else if (this.parameter instanceof LXNormalizedParameter) {
            LXNormalizedParameter normalized = (LXNormalizedParameter) this.parameter;
            normalized.setNormalized(normalized.getNormalized() == 0 ? 1 : 0);
        } else {
            this.parameter.setValue(this.parameter.getValue() == 0 ? 1 : 0);
        }
    }

    public static class Note extends LXMidiMapping {

        public final BooleanParameter momentary = new BooleanParameter("Momentary", false);

        public final int pitch;

        private Note(MidiNote note, LXParameter parameter) {
            super(note.getChannel(), Type.NOTE, parameter);
            this.pitch = note.getPitch();
        }

        @Override
        boolean matches(LXShortMessage message) {
            if (!(message instanceof MidiNote)) {
                return false;
            }
            MidiNote note = (MidiNote) message;
            return
                (note.getChannel() == this.channel) &&
                (note.getPitch() == this.pitch);
        }

        @Override
        void apply(LXShortMessage message) {
            MidiNote note = (MidiNote) message;
            if ((note instanceof MidiNoteOff) || note.getVelocity() == 0) {
                if (momentary.isOn()) {
                    setValue(false);
                }
            } else {
                if (this.parameter instanceof DiscreteParameter) {
                    ((DiscreteParameter) this.parameter).increment();
                } else if (momentary.isOn()) {
                    setValue(true);
                } else {
                    toggleValue();
                }
            }
        }

        @Override
        public String getDescription() {
            return "Note " + MidiNote.getPitchString(this.pitch);
        }
    }

    public static class ControlChange extends LXMidiMapping {

        public final int cc;

        private ControlChange(MidiControlChange controlChange, LXParameter parameter) {
            super(controlChange.getChannel(), Type.CONTROL_CHANGE, parameter);
            this.cc = controlChange.getCC();
        }

        @Override
        boolean matches(LXShortMessage message) {
            if (!(message instanceof MidiControlChange)) {
                return false;
            }
            MidiControlChange controlChange = (MidiControlChange) message;
            return
                (controlChange.getChannel() == this.channel) &&
                (controlChange.getCC() == this.cc);
        }

        @Override
        void apply(LXShortMessage message) {
            MidiControlChange controlChange = (MidiControlChange) message;
            setNormalized(controlChange.getValue() / 127.);
        }

        @Override
        public String getDescription() {
            return "CC " + this.cc;
        }
    }

}
