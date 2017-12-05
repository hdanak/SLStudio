/**
 *     DOUBLE BLACK DIAMOND        DOUBLE BLACK DIAMOND
 *
 *         //\\   //\\                 //\\   //\\
 *        ///\\\ ///\\\               ///\\\ ///\\\
 *        \\\/// \\\///               \\\/// \\\///
 *         \\//   \\//                 \\//   \\//
 *
 *        EXPERTS ONLY!!              EXPERTS ONLY!!
 *
 * This file implements the mapping functions needed to lay out the physical
 * cubes and the output ports on the panda board. It should only be modified
 * when physical changes or tuning is being done to the structure.
 */

import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

static final float globalOffsetX = 0;
static final float globalOffsetY = 0;
static final float globalOffsetZ = 0;

static final float globalRotationX = 0;
static final float globalRotationY = 0;
static final float globalRotationZ = 0;

static final float objOffsetX = 0;
static final float objOffsetY = 0;
static final float objOffsetZ = 0;

static final float objRotationX = 0;
static final float objRotationY = 0;
static final float objRotationZ = 0;

static final float INCHES_PER_METER = 39.3701;

/* Fulton Street Layout of the Suns */
public class FultonStreetLayout {
  private final Map<String, float[]> positions = new HashMap<String, float[]>();

  public FultonStreetLayout() {
    // These are the center positions of the sun stages as measured
    // photographically using the thermal cameras and perspective
    // transforms, NOT as measured in real life.  Measurements are
    // in inches but may be ~12 inches off from real-life distances.
    // Please talk to Ping if you need to touch this.
    // Note: y values are set in Sun class by type
    positions.put("A" /* 01 */, new float[] {1456, 0, 200});
    positions.put("B" /* 02 */, new float[] {1243, 0, 96});
    positions.put("C" /* 03 */, new float[] {1104, 0, 269});
    positions.put("D" /* 04 */, new float[] {933, 0, 128});
    positions.put("E" /* 05 */, new float[] {772, 0, 295});
    positions.put("F" /* 06 */, new float[] {395, 0, 58});
    positions.put("G" /* 07 */, new float[] {264, 0, 369});
    positions.put("H" /* 08 */, new float[] {0, 0, 0});  // origin, nearest to power supply
    positions.put("I" /* 09 */, new float[] {-265, 0, 201});
    positions.put("J" /* 10 */, new float[] {-401, 0, 356});
    positions.put("K" /* 11 */, new float[] {-686, 0, 261});
  }

  public float[] get(String id) {
    return positions.get(id);
  }
}

public SLModel buildModel() {
  FultonStreetLayout layout = new FultonStreetLayout();
  final float[] NO_ROTATION = {0, 0, 0};
  final float[] FLIP_Y = {0, 180, 0};

  // Any global transforms
  LXTransform transform = new LXTransform();
  transform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
  transform.rotateX(globalRotationX * PI / 180.);
  transform.rotateY(globalRotationY * PI / 180.);
  transform.rotateZ(globalRotationZ * PI / 180.);

  /* Suns ------------------------------------------------------------*/
  List<Sun> suns = new ArrayList<Sun>();

  suns.add(new Sun("sun2", Sun.Type.ONE_THIRD, layout.get("A"), NO_ROTATION, transform,
    new int[][] { // uncompleted
      { // Top - Front
          0,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 152, 153, 153,  // 41 - 50
        154, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 153,  // 41 - 50
        154, 155, 155, 155, 155, 157, 155, 157, 157, 159,  // 51 - 60
        158, 159, 159, 159, 161, 161, 161, 161, 161        // 61 - 69
      },
    }
  ));

  suns.add(new Sun("sun1", Sun.Type.ONE_THIRD, layout.get("B"), FLIP_Y, transform,
    new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 152, 153, 153,  // 41 - 50
        154, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 153,  // 41 - 50
        154, 155, 155, 155, 155, 157, 155, 157, 157, 159,  // 51 - 60
        158, 159, 159, 159, 161, 161, 161, 161, 161        // 61 - 69
      },
    }
  ));

  suns.add(new Sun("sun4", Sun.Type.ONE_HALF, layout.get("C"), NO_ROTATION, transform,
    new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 152, 153, 153,  // 41 - 50
        154, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 153,  // 41 - 50
        154, 155, 155, 155, 155, 157, 155, 157, 157, 159,  // 51 - 60
        158, 159, 159, 159, 161, 161, 161, 161, 161        // 61 - 69
      },
    }
  ));

  suns.add(new Sun("sun6", Sun.Type.TWO_THIRDS, layout.get("D"), FLIP_Y, transform,
    new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 135, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        153, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 138, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 153,  // 41 - 50
        155, 158, 155, 155, 157, 157, 155, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Bottom - Front
        149, 151, 151, 153, 153, 153, 155, 155, 155, 157,  //  1 - 10 
        157, 157, 157, 159, 159, 159, 159, 159, 159, 161,  // 11 - 20
        161, 161                                           // 21 - 22
      },
      { // Bottom - Back
        149, 151, 151, 153, 154, 153, 154, 155, 155, 157,  //  1 - 10
        157, 155, 157, 159, 159, 159, 159, 159, 161, 161,  // 11 - 20 
        161, 161                                           // 21 - 22
      },
    }
  ));

  suns.add(new Sun("sun7", Sun.Type.TWO_THIRDS, layout.get("E"), FLIP_Y , transform,
     new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  82,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 135, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        153, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          25,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 124, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        154, 158, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Bottom - Front
        149, 151, 151, 153, 153, 153, 155, 155, 155, 157,  //  1 - 10 
        157, 157, 157, 159, 159, 159, 159, 158, 161, 161,  // 11 - 20
        161, 161                                           // 21 - 22
      },
      { // Bottom - Back
        149, 151, 151, 153, 154, 154, 154, 155, 155, 157,  //  1 - 10
        157, 157, 157, 159, 159, 159, 159, 159, 161, 161,  // 11 - 20 
        161, 161                                           // 21 - 22
      },
    }
  ));

  suns.add(new Sun("sun9", Sun.Type.FULL, layout.get("F"), NO_ROTATION, transform,
    new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 135, 139, 141, 141, 143,  // 31 - 40
        143, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        155, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        153, 158, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Bottom - Front
          0,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 135, 139, 141, 141, 143,  // 31 - 40
        147, 145, 145, 147, 151, 149, 151, 153, 153, 153,  // 41 - 50
        153, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Bottom - Back
          0,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        153, 158, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      }
    }
  ));

  suns.add(new Sun("sun10", Sun.Type.FULL, layout.get("G"), NO_ROTATION, transform,
    new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 135, 139, 141, 141, 143,  // 31 - 40
        143, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        155, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 153, 154,  // 41 - 50
        154, 158, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Bottom - Front
          0,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 135, 139, 141, 141, 143,  // 31 - 40
        147, 145, 145, 147, 151, 149, 151, 153, 153, 153,  // 41 - 50
        153, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Bottom - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 114, 120, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 135, 137, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        153, 158, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      }
    }
  ));

  suns.add(new Sun("sun11", Sun.Type.FULL, layout.get("H"), NO_ROTATION, transform,
    new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 135, 139, 141, 141, 143,  // 31 - 40
        143, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        155, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 114, 119, 122, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 153,  // 41 - 50
        153, 158, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Bottom - Front
          0,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 135, 139, 141, 141, 143,  // 31 - 40
        147, 145, 145, 147, 151, 149, 151, 153, 153, 153,  // 41 - 50
        153, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Bottom - Back
          0,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 114, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        153, 158, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      }
    }
  ));

  suns.add(new Sun("sun8", Sun.Type.TWO_THIRDS, layout.get("I"), NO_ROTATION, transform,
    new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 135, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        153, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          0,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 114, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 154,  // 41 - 50
        154, 158, 155, 155, 157, 157, 155, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Bottom - Front
        149, 151, 151, 153, 153, 153, 155, 155, 155, 157,  //  1 - 10 
        157, 157, 157, 159, 159, 159, 159, 159, 161, 161,  // 11 - 20
        161, 161                                           // 21 - 22
      },
      { // Bottom - Back
        149, 151, 151, 153, 154, 154, 154, 155, 155, 157,  //  1 - 10
        157, 157, 157, 159, 159, 159, 159, 159, 161, 161,  // 11 - 20 
        161, 161                                           // 21 - 22
      },
    }
  ));

  suns.add(new Sun("sun5", Sun.Type.ONE_HALF, layout.get("J"), NO_ROTATION, transform,
    new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 152, 153, 153,  // 41 - 50
        154, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  72,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 150, 151, 154, 153,  // 41 - 50
        154, 155, 155, 155, 157, 157, 155, 159, 157, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      }
    }
  ));

  suns.add(new Sun("sun3", Sun.Type.ONE_THIRD, layout.get("K"), NO_ROTATION, transform,
    new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 152, 153, 153,  // 41 - 50
        154, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 153,  // 41 - 50
        154, 155, 155, 155, 155, 157, 155, 157, 157, 159,  // 51 - 60
        158, 159, 159, 159, 161, 161, 161, 161, 161        // 61 - 69
      },
    }
  ));


  /* Obj Importer ----------------------------------------------------*/
  List<LXModel> objModels = new ObjImporter("data", transform).getModels();

  return new SLModel(suns);
}