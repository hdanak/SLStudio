package com.symmetrylabs.slstudio.pattern;

import static com.symmetrylabs.util.MathUtils.*;

import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.ui.v2.FileDialog;
import com.symmetrylabs.slstudio.component.ModelImageProjector;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;


public class ImageProject extends SLPattern<SLModel> {
    private static final String TAG = "ImageProject";

    private final StringParameter file = new StringParameter("file", null);
    private final BooleanParameter chooseFile = new BooleanParameter("pick", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private final ModelImageProjector modelImageProjector = new ModelImageProjector(lx);

    private BufferedImage image = null;

    public ImageProject(LX lx) {
        super(lx);

        addParameter(file);
        addParameter(chooseFile);

        modelImageProjector.addToPattern(this);
    }

    @Override
    public void onActive() {
        super.onActive();
        if (image == null && file.getString() != null) {
            loadFile();
        }
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == chooseFile && chooseFile.getValueb()) {
            FileDialog.open(lx, "Choose image:", load -> {
                    Path loadPath = load.toPath().toAbsolutePath();
                    Path repoRoot = Paths.get("").toAbsolutePath();
                    Path rel = repoRoot.relativize(loadPath);
                    file.setValue(rel.toString());
                    loadFile();
                });
        } else if (modelImageProjector != null) {
            modelImageProjector.clearCache();
        }
    }

    @Override
    public void onVectorsChanged() {
        super.onVectorsChanged();

        if (modelImageProjector != null) {
            modelImageProjector.clearCache();
        }
    }

    private void loadFile() {
        ApplicationState.setWarning(TAG, null);
        String path = file.getString();
        if (path == null) {
            return;
        }
        try {
            image = ImageIO.read(new File(path));
            modelImageProjector.clearCache();
        } catch (IOException e) {
            ApplicationState.setWarning(TAG, String.format("could not read file %s: %s", path, e.getMessage()));
        }
    }

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        int[] ccs = (int[]) getArray(PolyBuffer.Space.SRGB8);
        modelImageProjector.projectImageToPoints(image, getVectors(), ccs);
        markModified(PolyBuffer.Space.SRGB8);
    }
}
