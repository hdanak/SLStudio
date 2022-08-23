package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.LXEffect;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.osc.OscMessage;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.EnumParameter;

import com.symmetrylabs.slstudio.ApplicationState;

/**
 * Quick and dirty "effect" for using LXAutomationRecorder for sending OSC
 * messages. Doesn't affect color buffer. Only sends when enabled.
 */
public class SendOscMessageEffect extends LXEffect {
    private static final String TAG = "SendOscMessageEffect";
    private static final String LOG_TAG = "[" + TAG + "] ";

    public enum OscType {
        INT, FLOAT, STRING;
    }

    public final BooleanParameter sendParam;
    public final StringParameter hostParam;
    public final StringParameter portParam;
    public final StringParameter pathParam;
    public final EnumParameter<OscType> typeParam;
    public final StringParameter valueParam;

    private LXOscEngine.Transmitter oscTransmitter = null;

    public SendOscMessageEffect(LX lx) {
        super(lx);

        addParameter(sendParam = new BooleanParameter("send")
                .setMode(BooleanParameter.Mode.MOMENTARY));
        addParameter(hostParam = new StringParameter("host", "localhost"));
        addParameter(portParam = new StringParameter("port"));
        addParameter(pathParam = new StringParameter("path"));
        addParameter(typeParam = new EnumParameter<OscType>("type", OscType.INT));
        addParameter(valueParam = new StringParameter("value"));
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == hostParam || p == portParam || p == pathParam) {
            String host = hostParam.getString();
            String portStr = portParam.getString();
            String path = pathParam.getString();

            if ("".equals(host) || "".equals(portStr) || "".equals(path)) {
                return;
            }

            try {
                int port = Integer.parseInt(portStr);
                oscTransmitter = lx.engine.osc.transmitter(host, port);
            } catch (Exception e) {
                oscTransmitter = null;

                ApplicationState.setWarning(TAG, "Could not open OSC socket: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (p == sendParam && sendParam.isOn() && oscTransmitter != null && enabled.isOn()) {
            try {
                OscMessage oscMessage = new OscMessage(pathParam.getString());
                String valueStr = valueParam.getString();
                //System.out.println(LOG_TAG + "Sending value '" + valueStr + "' to path '" + pathParam.getString() + "' at '" + hostParam.getString() + ":" + portParam.getString() + "'");
                switch (typeParam.getEnum()) {
                case INT: {
                    int value = Integer.parseInt(valueStr);
                    if (!(value + "").equals(valueStr)) {
                        ApplicationState.setWarning(TAG, "Value is not int, sending " + value);
                    }
                    oscMessage.add(value);
                }; break;
                case FLOAT: {
                    float value = Float.parseFloat(valueStr);
                    oscMessage.add(value);
                }; break;
                case STRING: {
                    oscMessage.add(valueStr);
                }; break;
                }
                oscTransmitter.send(oscMessage);
            } catch (Exception e) {
                ApplicationState.setWarning(TAG, "Failed to send OSC message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
