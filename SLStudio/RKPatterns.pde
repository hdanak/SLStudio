public class RKPattern01 extends P3CubeMapPattern {

  CompoundParameter rX = new CompoundParameter("rX", 0, -PI, PI);
  CompoundParameter rY = new CompoundParameter("rY", 0, -PI, PI);
  CompoundParameter rZ = new CompoundParameter("rZ", 0, -PI, PI);
  CompoundParameter speed = new CompoundParameter("speed", PI, -TWO_PI*2, TWO_PI*2);
  CompoundParameter dsp = new CompoundParameter("dsp", HALF_PI, 0, PI);

  int ringRes = 40;
  float l1 = 600, l2 = 600, l3 = 600;
  float rotX, rotXT, rotY, rotYT, rotZ, rotZT, dspmt, dspmtT, thetaSpeed, thetaSpeedT;
  Ring [] testRings;

  public RKPattern01(LX lx) {

    super((P3LX) lx, new PVector(lx.model.cx, lx.model.cy, lx.model.cz), new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange), 100);
    testRings = new Ring[20];
    for (int i=0; i<testRings.length; i++) {
      float initTheta = i*PI/testRings.length;
      testRings[i] = new Ring(ringRes, initTheta, l1, l2, l3);
    }

    addParameter(rX);
    addParameter(rY);
    addParameter(rZ);
    addParameter(speed);
    addParameter(dsp);
  }

  void run(double deltaMs, PGraphics pg) {

    rotXT = rX.getValuef();
    rotYT = rY.getValuef();
    rotZT = rZ.getValuef();
    thetaSpeedT = speed.getValuef();
    dspmtT = dsp.getValuef();
    rotX = lerp(rotX, rotXT, .1);
    rotY = lerp(rotY, rotYT, .1);
    rotZ = lerp(rotZ, rotZT, .1);
    thetaSpeed = lerp(thetaSpeed, thetaSpeedT, .1);
    dspmt = lerp(dspmt, dspmtT, .1);

    for (int i=0; i<testRings.length; i++) {
      testRings[i].update();
    }
    updateCubeMaps();

    pg.beginDraw();
    pg.background(0);
    pg.image(pgL, 0, 100);
    pg.image(pgR, 200, 100);
    pg.image(pgD, 100, 0);
    pg.image(pgU, 100, 200);
    pg.image(pgF, 100, 100);
    pg.image(pgB, 300, 100);
    pg.endDraw();
  }

  void updateCubeMap(PGraphics pg, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
    pg.beginDraw();
    pg.background(0);
    pg.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    pg.frustum(-10, 10, -10, 10, 10, 1000);
    pg.rotateX(rotX);
    pg.rotateY(rotY);
    pg.rotateZ(rotZ);
    drawScene(pg);
    pg.endDraw();
  }

  void updateCubeMaps() {
    updateCubeMap(pgF, 0, 0, 0, 0, 0, 1, 0, 1, 0);
    updateCubeMap(pgL, 0, 0, 0, 1, 0, 0, 0, 1, 0);
    updateCubeMap(pgR, 0, 0, 0, -1, 0, 0, 0, 1, 0);
    updateCubeMap(pgB, 0, 0, 0, 0, 0, -1, 0, 1, 0);
    updateCubeMap(pgD, 0, 0, -.001, 0, -1, 0, 0, 1, 0);
    updateCubeMap(pgU, 0, 0, -.001, 0, 1, 0, 0, 1, 0);
  }

  void drawScene(PGraphics pg) {
    for (int i=0; i<testRings.length; i++) {
      testRings[i].display(pg);
    }
  }

  class Ring {

    int amt;
    float l1, l2, l3, theta, weight;
    Vtx [] vts;

    Ring(int amt, float theta, float l1, float l2, float l3) {

      this.amt = amt;
      this.l1 = l1;
      this.l2 = l2;
      this.l3 = l3;
      this.theta = theta;

      vts = new Vtx[this.amt];
      for (int i=0; i<vts.length; i++) {
        float initPhi = i*TWO_PI/amt;
        vts[i] = new Vtx(this.theta, initPhi, this.l1, this.l2, this.l3);
      }
    }

    void update() {
      theta += thetaSpeed/720;
      if (theta>PI) theta -= PI;
      else if (theta<0) theta += PI;

      weight = sin(theta)*4;

      for (int i=0; i<vts.length; i++) {
        vts[i].update(theta);
      }
    }

    void display(PGraphics pg) {
      pg.noFill();
      pg.stroke(255);
      pg.strokeWeight(weight);
      pg.beginShape();
      for (int i=0; i<vts.length; i++) {
        pg.curveVertex(vts[i].pos.x, vts[i].pos.y, vts[i].pos.z);
      }
      pg.curveVertex(vts[0].pos.x, vts[0].pos.y, vts[0].pos.z);
      pg.curveVertex(vts[1].pos.x, vts[1].pos.y, vts[1].pos.z);
      pg.curveVertex(vts[2].pos.x, vts[2].pos.y, vts[2].pos.z);
      pg.endShape();
    }
  }

  class Vtx {

    PVector pos;
    float thetaBase, thetaOfst, thetaOfstRange, theta, phi;
    float l1, l2, l3;

    Vtx(float theta, float phi, float l1, float l2, float l3) {
      pos = new PVector();
      this.theta = thetaBase = theta;
      this.phi = phi;
      this.l1 = l1;
      this.l2 = l2;
      this.l3 = l3;
    }

    void update(float thetaBase) {
      this.thetaBase = thetaBase;

      thetaOfstRange = sin(theta)*dspmt;
      thetaOfst = (noise(phi+frameCount*.005, cos(this.thetaBase)-frameCount*.005)-.5)*thetaOfstRange;
      theta = this.thetaBase + thetaOfst;
      pos.set(
        sin(theta)*cos(phi)*.5*l1, 
        cos(theta)*.5*l2, 
        sin(theta)*sin(phi)*.5*l3
        );
    }
  }
}

public class RKPattern02 extends P3CubeMapPattern {

  CompoundParameter rX = new CompoundParameter("rX", 0, -PI, PI);
  CompoundParameter rY = new CompoundParameter("rY", 0, -PI, PI);
  CompoundParameter rZ = new CompoundParameter("rZ", 0, -PI, PI);
  BooleanParameter noise = new BooleanParameter("noise");
  CompoundParameter speed = new CompoundParameter("speed", .1, 0, .5);
  CompoundParameter dsp = new CompoundParameter("dsp", 1, 0, 2);

  ArrayList <Arc> arcs;
  float f, fT, fTIncre;
  float l1 = 600, l2 = 600, l3 = 600;
  float rotX, rotXT, rotY, rotYT, rotZ, rotZT, dspmt, dspmtT;
  boolean autoNoise;
  int switchCount;

  public RKPattern02(LX lx) {

    super((P3LX) lx, new PVector(lx.model.cx, lx.model.cy, lx.model.cz), new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange), 100);

    arcs = new ArrayList<Arc>();
    Arc parentArc = new Arc(0, 0, 16, 0, PI, 0, TWO_PI);
    arcs.add(parentArc);

    for (int i=0; i<8; i++) {
      for (int j=arcs.size()-1; j>-1; j--) {
        Arc arc = arcs.get(j);
        if (!arc.hasChildren) arc.splitUp();
      }
    }

    addParameter(rX);
    addParameter(rY);
    addParameter(rZ);
    addParameter(noise);
    addParameter(speed);
    addParameter(dsp);
  }

  void run(double deltaMs, PGraphics pg) {

    rotXT = rX.getValuef();
    rotYT = rY.getValuef();
    rotZT = rZ.getValuef();
    autoNoise = noise.getValueb();
    dspmtT = dsp.getValuef();
    fTIncre = speed.getValuef();
    rotX = lerp(rotX, rotXT, .1);
    rotY = lerp(rotY, rotYT, .1);
    rotZ = lerp(rotZ, rotZT, .1);
    dspmt = lerp(dspmt, dspmtT, .1);

    fT += fTIncre*.1;
    f = lerp(f, fT, .1);

    if (frameCount%90==0 && !autoNoise) {
      switchCount = (switchCount+1)%2;
      if (switchCount == 0) {
        for (int i=arcs.size()-1; i>-1; i--) {
          Arc arc = arcs.get(i);
          if (arc.hasChildren) arc.reset();
        }
      } else {
        for (int i=arcs.size()-1; i>-1; i--) {
          Arc arc = arcs.get(i);
          if (arc.hasChildren) arc.randomize();
        }
      }
    }

    for (int i=0; i<arcs.size(); i++) {
      Arc arc = arcs.get(i);
      if (arc.hasChildren) {
        if (autoNoise) {
          float scalar = map(arc.lv, 0, 8, dspmt, dspmt*1.5);
          arc.splitRatio = .5+(noise(arc.idx*.1+f)-.5)*scalar;
        }
        arc.update();
      }
    }
    updateCubeMaps();

    pg.beginDraw();
    pg.background(0);
    pg.image(pgL, 0, 100);
    pg.image(pgR, 200, 100);
    pg.image(pgD, 100, 0);
    pg.image(pgU, 100, 200);
    pg.image(pgF, 100, 100);
    pg.image(pgB, 300, 100);
    pg.endDraw();
  }

  void updateCubeMap(PGraphics pg, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
    pg.beginDraw();
    pg.background(0);
    pg.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    pg.frustum(-10, 10, -10, 10, 10, 1000);
    pg.rotateX(rotX+HALF_PI);
    pg.rotateY(rotY);
    pg.rotateZ(rotZ);
    drawScene(pg);
    pg.endDraw();
  }

  void updateCubeMaps() {
    updateCubeMap(pgF, 0, 0, 0, 0, 0, 1, 0, 1, 0);
    updateCubeMap(pgL, 0, 0, 0, 1, 0, 0, 0, 1, 0);
    updateCubeMap(pgR, 0, 0, 0, -1, 0, 0, 0, 1, 0);
    updateCubeMap(pgB, 0, 0, 0, 0, 0, -1, 0, 1, 0);
    updateCubeMap(pgD, 0, 0, -.001, 0, -1, 0, 0, 1, 0);
    updateCubeMap(pgU, 0, 0, -.001, 0, 1, 0, 0, 1, 0);
  }

  void drawScene(PGraphics pg) {
    pg.noFill();
    pg.stroke(255);
    pg.strokeWeight(2);
    for (int i=0; i<arcs.size(); i++) {
      Arc arc = arcs.get(i);
      if (arc.hasChildren) arc.display(pg);
    }
    pg.beginShape();
    for (int i=0; i<16; i++) {
      float phi = 0;
      float theta = i*PI/15;
      pg.vertex(
        sin(theta)*cos(phi)*l1*.5, 
        cos(theta)*l2*.5, 
        sin(theta)*sin(phi)*l3*.5
        );
    }
    pg.endShape();
  }

  class Arc {

    Arc cArc1, cArc2;
    boolean hasChildren;
    int segAmt, lv, idx;

    PVector [] vts;

    float splitRatio, splitRatioT;
    float sTheta, eTheta, sPhi, ePhi;
    float l1 = 600, l2 = 600, l3 = 600;

    Arc(int lv, int idx, int segAmt, float sTheta, float eTheta, float sPhi, float ePhi) {

      this.lv = lv;
      this.idx = idx;
      this.segAmt = segAmt;
      this.sTheta = sTheta;
      this.eTheta = eTheta;
      this.sPhi = sPhi;
      this.ePhi = ePhi;
    }

    void update() {
      if (!autoNoise) splitRatio = lerp(splitRatio, splitRatioT, .125);
      if (lv%2==0) {
        for (int i=0; i<segAmt; i++) {
          float phi = sPhi + i*(ePhi-sPhi)/(vts.length-1);
          float theta = lerp(sTheta, eTheta, splitRatio);

          vts[i].set(
            sin(theta)*cos(phi)*l1*.5, 
            cos(theta)*l2*.5, 
            sin(theta)*sin(phi)*l3*.5
            );
        }
        cArc1.sTheta = sTheta;
        cArc1.eTheta = lerp(sTheta, eTheta, splitRatio);
        cArc2.sTheta = lerp(sTheta, eTheta, splitRatio);
        cArc2.eTheta = eTheta;
        cArc1.sPhi = sPhi;
        cArc1.ePhi = ePhi;
        cArc2.sPhi = sPhi;
        cArc2.ePhi = ePhi;
      } else {
        for (int i=0; i<segAmt; i++) {
          float phi = lerp(sPhi, ePhi, splitRatio);
          float theta = sTheta + i*(eTheta-sTheta)/(vts.length-1);

          vts[i].set(
            sin(theta)*cos(phi)*l1*.5, 
            cos(theta)*l2*.5, 
            sin(theta)*sin(phi)*l3*.5
            );
        }
        cArc1.sTheta = sTheta;
        cArc1.eTheta = eTheta;
        cArc2.sTheta = sTheta;
        cArc2.eTheta = eTheta;
        cArc1.sPhi = sPhi;
        cArc1.ePhi = lerp(sPhi, ePhi, splitRatio);
        cArc2.sPhi = lerp(sPhi, ePhi, splitRatio);
        cArc2.ePhi = ePhi;
      }
    }

    void randomize() {
      splitRatioT = random(1);
    }

    void reset() {
      splitRatioT = .5;
    }

    void splitUp() {

      hasChildren = true;
      splitRatio = .5;
      splitRatioT = random(1);

      vts = new PVector[segAmt];

      if (lv%2==0) {
        cArc1 = new Arc(lv+1, arcs.size(), segAmt, sTheta, lerp(sTheta, eTheta, splitRatio), sPhi, ePhi);
        arcs.add(cArc1);
        cArc2 = new Arc(lv+1, arcs.size(), segAmt, lerp(sTheta, eTheta, splitRatio), eTheta, sPhi, ePhi);
        arcs.add(cArc2);

        for (int i=0; i<segAmt; i++) {
          float phi = sPhi + i*(ePhi-sPhi)/(vts.length-1);
          float theta = lerp(sTheta, eTheta, splitRatio);

          vts[i] = new PVector(
            sin(theta)*cos(phi)*l1*.5, 
            cos(theta)*l2*.5, 
            sin(theta)*sin(phi)*l3*.5
            );
        }
      } else {
        cArc1 = new Arc(lv+1, arcs.size(), segAmt, sTheta, eTheta, sPhi, lerp(sPhi, ePhi, splitRatio));
        arcs.add(cArc1);
        cArc2 = new Arc(lv+1, arcs.size(), segAmt, sTheta, eTheta, lerp(sPhi, ePhi, splitRatio), ePhi);
        arcs.add(cArc2);

        for (int i=0; i<segAmt; i++) {
          float phi = lerp(sPhi, ePhi, splitRatio);
          float theta = sTheta + i*(eTheta-sTheta)/(vts.length-1);

          vts[i] = new PVector(
            sin(theta)*cos(phi)*l1*.5, 
            cos(theta)*l2*.5, 
            sin(theta)*sin(phi)*l3*.5
            );
        }
      }
    }

    void display(PGraphics pg) {
      pg.beginShape();
      for (int i=0; i<segAmt; i++) {
        pg.vertex(vts[i].x, vts[i].y, vts[i].z);
      }
      pg.endShape();
    }
  }
}

public class RKPattern03 extends P3CubeMapPattern {
  
  CompoundParameter rX = new CompoundParameter("rX", 0, -PI, PI);
  CompoundParameter rY = new CompoundParameter("rY", 0, -PI, PI);
  CompoundParameter rZ = new CompoundParameter("rZ", 0, -PI, PI);
  CompoundParameter speed = new CompoundParameter("speed", .01, 0, .05);
  CompoundParameter fragment = new CompoundParameter("fragment", .5, 0, 1);

  Vtx [][] rootVts;
  int iCSCols = 5, iCSRows = 4;
  float l1 = 600, l2 = 600, l3 = 600;
  float gF, thetaF, phiF, fragMid;
  ArrayList <Fct> fctList;
  
  float rotX, rotXT, rotY, rotYT, rotZ, rotZT, gFIncre, gFIncreT;

  public RKPattern03(LX lx) {

    super((P3LX) lx, new PVector(lx.model.cx, lx.model.cy, lx.model.cz), new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange), 100);

    gF = random(100);
    thetaF = random(100);
    phiF = random(100);

    initRootVts();
    initFctList();

    for (int j=0; j<2; j++) {
      for (int i=fctList.size()-1; i>-1; i--) {
        Fct fct = fctList.get(i);
        if (!fct.hasChildren && fct.lv<3) fct.splitUp();
      }
    }
    
    addParameter(rX);
    addParameter(rY);
    addParameter(rZ);
    addParameter(speed);
    addParameter(fragment);
  }

  void run(double deltaMs, PGraphics pg) {
    
    rotXT = rX.getValuef();
    rotYT = rY.getValuef();
    rotZT = rZ.getValuef();
    gFIncreT = speed.getValuef();
    fragMid = fragment.getValuef();
    
    rotX = lerp(rotX, rotXT, .1);
    rotY = lerp(rotY, rotYT, .1);
    rotZ = lerp(rotZ, rotZT, .1);
    gFIncre = lerp(gFIncre, gFIncreT, .1);
    
    gF += gFIncre;
    for (int i=0; i<fctList.size(); i++) {
      Fct fct = fctList.get(i);
      fct.update();
    }

    updateCubeMaps();

    pg.beginDraw();
    pg.background(0);
    pg.image(pgL, 0, 100);
    pg.image(pgR, 200, 100);
    pg.image(pgD, 100, 0);
    pg.image(pgU, 100, 200);
    pg.image(pgF, 100, 100);
    pg.image(pgB, 300, 100);
    pg.endDraw();
  }

  void updateCubeMaps() {
    updateCubeMap(pgF, 0, 0, 0, 0, 0, 1, 0, 1, 0);
    updateCubeMap(pgL, 0, 0, 0, 1, 0, 0, 0, 1, 0);
    updateCubeMap(pgR, 0, 0, 0, -1, 0, 0, 0, 1, 0);
    updateCubeMap(pgB, 0, 0, 0, 0, 0, -1, 0, 1, 0);
    updateCubeMap(pgD, 0, 0, -.001, 0, -1, 0, 0, 1, 0);
    updateCubeMap(pgU, 0, 0, -.001, 0, 1, 0, 0, 1, 0);
  }

  void updateCubeMap(PGraphics pg, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
    pg.beginDraw();
    pg.background(0);
    pg.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    pg.frustum(-10, 10, -10, 10, 10, 1000);
    pg.rotateX(rotX);
    pg.rotateY(rotY);
    pg.rotateZ(rotZ);
    drawScene(pg);
    pg.endDraw();
  }

  void drawScene(PGraphics pg) {
    pg.beginShape(TRIANGLES);
    pg.noStroke();
    for (int i=fctList.size()-1; i>-1; i--) {
      Fct fct = fctList.get(i);
      if (!fct.hasChildren) fct.display(pg);
    }
    pg.endShape();
  }

  void initFctList() {
    fctList = new ArrayList<Fct>();

    for (int j=0; j<rootVts[1].length; j++) {
      int k = (j+1)%rootVts[1].length;
      Fct fct = new Fct(0, rootVts[0][0], rootVts[1][j], rootVts[1][k]);
      fctList.add(fct);
    }

    for (int j=0; j<rootVts[2].length; j++) {
      int k = (j+1)%rootVts[2].length;
      Fct fct = new Fct(0, rootVts[3][0], rootVts[2][j], rootVts[2][k]);
      fctList.add(fct);
    }

    for (int j=0; j<rootVts[1].length; j++) {
      int k = (j+1)%rootVts[1].length;
      Fct fct1 = new Fct(0, rootVts[1][j], rootVts[1][k], rootVts[2][j]);
      fctList.add(fct1);
      Fct fct2 = new Fct(0, rootVts[2][j], rootVts[1][k], rootVts[2][k]);
      fctList.add(fct2);
    }
  }

  class Fct {

    int lv;
    Vtx v1, v2, v3;
    float brt, fragRatio;
    PVector ctr, nrml;
    boolean hasChildren;

    Fct(int lv, Vtx v1, Vtx v2, Vtx v3) {
      this.lv = lv;
      this.v1 = v1;
      this.v2 = v2;
      this.v3 = v3;

      ctr = new PVector();
    }

    void splitUp() {
      hasChildren = true;
      Vtx v4 = new Vtx(lv+1, v1, v2);
      Vtx v5 = new Vtx(lv+1, v2, v3);
      Vtx v6 = new Vtx(lv+1, v3, v1);

      Fct cFct1 = new Fct(lv+1, v1, v4, v6);
      Fct cFct2 = new Fct(lv+1, v4, v2, v5);
      Fct cFct3 = new Fct(lv+1, v6, v5, v3);
      Fct cFct4 = new Fct(lv+1, v5, v4, v6);

      fctList.add(cFct1);
      fctList.add(cFct2);
      fctList.add(cFct3);
      fctList.add(cFct4);
    }

    void update() {
      v1.update();
      v2.update();
      v3.update();
      ctr.set(
        (v1.pos.x + v2.pos.x + v3.pos.x)/3, 
        (v1.pos.y + v2.pos.y + v3.pos.y)/3, 
        (v1.pos.z + v2.pos.z + v3.pos.z)/3
        );

      nrml = PVector.sub(v1.pos, v2.pos).cross(PVector.sub(v3.pos, v2.pos));
      brt = map(abs(HALF_PI-PVector.angleBetween(nrml, ctr)), 0, HALF_PI, 0, 255);

      fragRatio = (noise(ctr.x*.005, ctr.y*.005, ctr.z*.005)-.5)*2.5+fragMid;
      fragRatio = constrain(fragRatio, 0, 1);
    }

    void display(PGraphics pg) {
      pg.fill(brt);
      pg.vertex(lerp(v1.pos.x, ctr.x, fragRatio), lerp(v1.pos.y, ctr.y, fragRatio), lerp(v1.pos.z, ctr.z, fragRatio));
      pg.vertex(lerp(v2.pos.x, ctr.x, fragRatio), lerp(v2.pos.y, ctr.y, fragRatio), lerp(v2.pos.z, ctr.z, fragRatio));
      pg.vertex(lerp(v3.pos.x, ctr.x, fragRatio), lerp(v3.pos.y, ctr.y, fragRatio), lerp(v3.pos.z, ctr.z, fragRatio));
    }
  }

  void initRootVts() {

    rootVts = new Vtx[iCSRows][];
    rootVts[0] = new Vtx[1];
    rootVts[1] = new Vtx[iCSCols];
    rootVts[2] = new Vtx[iCSCols];
    rootVts[3] = new Vtx[1];

    for (int i=0; i<rootVts.length; i++) {
      for (int j=0; j<rootVts[i].length; j++) {
        float phi = (j+i*.5)*TWO_PI/float(rootVts[i].length);
        float theta;//=i*PI/float(rootVts.length-1);
        if (i==0)theta = 0;
        else if (i==1)theta = HALF_PI-atan(.5);
        else if (i==2)theta = HALF_PI+atan(.5);
        else theta = PI;
        float r = 300;

        rootVts[i][j] = new Vtx(0, theta, phi, r);
      }
    }
  }

  class Vtx {

    PVector pos, tgt, ofst;
    float theta, phi, r;
    int lv;

    Vtx(int lv, Vtx v1, Vtx v2) {

      this.lv = lv;

      if (abs(v1.phi-v2.phi)>PI) {
        this.phi = lerp(v1.phi, v2.phi+TWO_PI, .5);
      } else {
        this.phi = lerp(v1.phi, v2.phi, .5);
      }
      this.theta = lerp(v1.theta, v2.theta, .5);

      if (v1.theta == 0 || v1.theta == PI)this.phi = v2.phi;
      if (v2.theta == 0 || v2.theta == PI)this.phi = v1.phi;

      this.r = lerp(v1.r, v2.r, .5);

      tgt = new PVector(sin(theta)*cos(phi)*r, cos(theta)*r, sin(theta)*sin(phi)*r);
      pos = new PVector(lerp(v1.pos.x, v2.pos.x, .5), lerp(v1.pos.y, v2.pos.y, .5), lerp(v1.pos.z, v2.pos.z, .5));
      ofst = new PVector();
    }

    Vtx(int lv, float theta, float phi, float r) {

      this.lv = lv;
      this.theta = theta;
      this.phi = phi;
      this.r = r;

      tgt = new PVector(sin(theta)*cos(phi)*r, cos(theta)*r, sin(theta)*sin(phi)*r);
      pos = new PVector(tgt.x, tgt.y, tgt.z);
      ofst = new PVector();
    }

    void update() {
      updatePos();
    }

    void updatePos() {
      float thetaOfst = (noise(gF+tgt.x*.0025+thetaF, -gF+tgt.y*.0025+thetaF, gF+tgt.z*.0025+thetaF)-.5)*TWO_PI*2;
      float phiOfst = (noise(-gF+tgt.x*.0025+phiF, gF+tgt.y*.0025+phiF, -gF+tgt.z*.0025+phiF)-.5)*TWO_PI*2;
      float rOfst = noise(tgt.x*.005+gF, tgt.y*.005+gF, tgt.z*.005+gF)*r*.25;
      ofst.set(sin(thetaOfst)*cos(phiOfst)*rOfst, cos(thetaOfst)*rOfst, sin(thetaOfst)*sin(phiOfst)*rOfst);

      float rDsp = (noise(sin(theta+thetaF)*sin(phi), cos(theta)*cos(phi+phiF))-.5)*r;
      tgt.set(sin(theta)*cos(phi)*(rDsp+r), cos(theta)*(rDsp+r), sin(theta)*sin(phi)*(rDsp+r));

      pos.set(lerp(pos.x, (tgt.x+ofst.x), .125), lerp(pos.y, (tgt.y+ofst.y), .125), lerp(pos.z, (tgt.z+ofst.z), .125));
    }

    void display() {
      vertex(pos.x, pos.y, pos.z);
    }
  }
}