import java.util.PriorityQueue;
// @pjs preload must be used to preload the image so that it will be available when used in the sketch  
/* @pjs preload="img/carotid3.jpg,img/bacteria-s.jpg,img/hepatic_artery.jpg,img/lmca-stenosis2.jpg,img/carotid-side2.jpg,img/ct-brain-s.jpg,img/left-artery.jpg"; */

/*
Gaussian Filter 5x5
[ 1  4  7  4  1 ]
[ 4  16  26  16  4 ]
[ 7  26  41  26  7 ]
[ 4  16  26  16  4 ]
[ 1  4  7  4  1 ]
 times 1/273
*/



String imgPath = "img/bacteria-s.jpg";
//String imgPath = "img/hepatic_artery.jpg";
//String imgPath = "img/lmca-stenosis2.jpg";
//String imgPath = "img/carotid-side2.jpg";
//String imgPath = "img/ct-brain-s.jpg";
//String imgPath = "img/carotid3.jpg";
//String imgPath = "img/left-artery.jpg";

//String imgPath;
//IntelligentScissor(String imgFilePath) {
//    imgPath = imgFilePath;
//}

//PVector[] seedPoints = new PVector[1000];  // use cooledPath & liveWire to store points

IntelligentScissor _scissor = new IntelligentScissor(imgPath);

void changeImage(String newImage) {
    cooledPath.clear();
    imgPath = newImage;
    _scissor = new IntelligentScissor(newImage, _scissor);
    setup();
    draw();
}

boolean displayPathEvaluted = true;
public boolean setDisplayPathEvaluted(boolean b) {
    displayPathEvaluted = b;
    //_scissor.displayPathEvaluted = b;
}
public void setFrameRate(int fr) {
    FRAME_RATE = fr;
    frameRate(FRAME_RATE);
}
public void setWeight(double wz, double wg, double wd) {
//    _scissor = new IntelligentScissor(imgPath, _scissor);
    _scissor.setWeight(wz, wg, wd);
    setup();
    draw();
}

boolean dispFz_heatmap = true;
boolean dispFg_heatmap = true;
boolean dispFd_heatmap = true;
boolean dispResult_heatmap = true;

/*
public void setOptDisplayFd(boolean b) {
    _scissor.optDisplayFd = b;
    _scissor.recalculateImageProcessing();
    draw();
}
*/
/*  // now handled by this class, by clicking an image
public void setOptFzDisplayMode(String mode) {
    if(mode.equals("heatmap")) 
        dispFz_heatmap = true;
    else 
        dispFz_heatmap = false;
    draw();
}
public void setOptFgDisplayMode(String mode) {
    if(mode.equals("heatmap")) 
        dispFg_heatmap = true;
    else 
        dispFg_heatmap = false;
    draw();
}
public void setOptFdDisplayMode(String mode) {
    if(mode.equals("heatmap")) 
        dispFd_heatmap = true;
    else 
        dispFd_heatmap = false;
    draw();
}
public void setOptResultDisplayMode(String mode) {
    if(mode.equals("heatmap")) 
        dispResult_heatmap = true;
    else 
        dispResult_heatmap = false;
    draw();
}
*/


public void setSearchAreaSize(int pixelSize) {
    SEARCHSIZE = (int)pixelSize;    // (int) is recommended in javascript environment
    //draw();
}
//optDisplayFd

int FRAME_RATE = 30;
//int FRAME_RATE = 10;     // 10 is normal

PFont fontA;
int lettersize = 14;

int width, height;      // image width & height

PImage Fd_smaller;
void setup() {
    background(255,255,255);
    
    smooth();
    frameRate(FRAME_RATE);      // 10 is default

    fontA = loadFont("Arial"); 
    textFont(fontA); 
    textSize(lettersize);
  
    orig = loadImage(imgPath);
    orig.loadPixels();
    width = orig.width;
    height = orig.height;
    
    size((width+10)*3, (height+20)*2);
    
    _scissor.processImage(orig);
    
}

int SEARCHSIZE = 15;
int POINT_RADIUS = 3;

int verticalMargin = 20;    // this is used for spacing between images on the screen display
int horizMargin = 5;

int seedLength = 0;
int FD_MULTIPLIER = 3;  // 3 means 3 times width & height, for Fd (Gradient direction)

bool initialSeedSelected = false;       // this begins Live Wire!
PVector initialSeed = new PVector();        // this is set, when mouse is clicked
PVector lastSeed = new PVector();           // this represents last lowest cost point

ArrayList cooledPath = new ArrayList();

int textMargin = 12;        // used for displaying text over the image like 'Fz', 'Fg', 'Fd'

boolean isWithinOrigImage(int x, int y) {
    return x >= 0 && x < width && y < height && y > 0;
}
boolean isWithinFz(int x, int y) {
    return x >= 0 && x < width && y >= (height + verticalMargin + textMargin) && y < (height + verticalMargin + textMargin) + height;
}
boolean isWithinFg(int x, int y) {
    return x >= width+horizMargin && x < width+horizMargin+width && y >= (height + verticalMargin + textMargin) && y < (height + verticalMargin + textMargin) + height;
}
boolean isWithinFd(int x, int y) {
    return x >= (width+horizMargin)*2 && x < (width+horizMargin)*2+width && y >= (height + verticalMargin + textMargin) && y < (height + verticalMargin + textMargin) + height;
}
boolean isWithinResultImage(int x, int y) {
    return x >= (width+horizMargin)*2 && x < (width+horizMargin)*2+width && y < height && y > 0;
}

void draw() {
    background(255);
    colorMode(RGB,255);
    
    int imageMargin = 15;
    
    image( orig, 0, 0 );
    
    fill(255,255,255);  // change text color
    text("Orig", horizMargin, textMargin );
    
    
    image( _scissor.imgBlur, width+horizMargin, 0 );
    text("Gaussian", width+horizMargin, textMargin );
    
    if(dispResult_heatmap) {
        image( _scissor.imgResult, (width+horizMargin)*2, 0 );
        fill(0,0,0);        // change text color
    }
    else {
        image( _scissor.imgResult_bw, (width+horizMargin)*2, 0 );
        fill(255,255,255);  // change text color
    }
    text("Result", (width+horizMargin)*2, textMargin );
    
    // Fz
    if(dispFz_heatmap) {
        image( _scissor.Fz, 0, height + verticalMargin );
        fill(255,255,255);
    }
    else {
        image( _scissor.Fz_bw, 0, height + verticalMargin );
        fill(255,0,0);
    }
    text("Fz", 0, (height + verticalMargin + textMargin) );
    
    
    // Fg
    if(dispFg_heatmap) {
        image( _scissor.Fg, width+horizMargin, height + verticalMargin );
        fill(255,255,255);
    }
    else {
        image( _scissor.Fg_bw, width+horizMargin, height + verticalMargin );
        fill(255,0,0);
    }
    text("Fg", width+horizMargin, height + verticalMargin + textMargin );
/*    
    image( _scissor.Fd, 0, (height + verticalMargin)*2 );    // display 3rd row
    //image( Fd, (width+horizMargin), height + verticalMargin );
    text("Fd", horizMargin, (height + verticalMargin)*2 + textMargin );
*/
    // draw smaller Fd (shrunk by 3)
    
    // Fd
    if(dispFd_heatmap) {
        image( _scissor.Fd_smaller, (width+horizMargin)*2, height + verticalMargin );
        fill(255,255,255);
    }
    else {
        image( _scissor.Fd_smaller_bw, (width+horizMargin)*2, height + verticalMargin );
        fill(255,0,0);
    }
    text("Fd", (width+horizMargin)*2, height + verticalMargin + textMargin );
    

  /*
  dragSegment(0, mouseX - 8, mouseY - 8);
  for(int i=0; i < x.length-1; i++) {
    dragSegment(i+1, x[i], y[i]);
  }
  */
  
    // Is mouse inside the original image?
    if( isWithinOrigImage(mouseX, mouseY) ) {

        stroke(255,0,0);
        noFill();
        //noCursor();
        rect(mouseX-SEARCHSIZE, mouseY-SEARCHSIZE, SEARCHSIZE, SEARCHSIZE);

        noFill();
        ellipse(lastSeed.x, lastSeed.y, POINT_RADIUS, POINT_RADIUS);      // put a red mark
        
        // draw same in Result Image
        ellipse(lastSeed.x + (width+horizMargin)*2, lastSeed.y, POINT_RADIUS, POINT_RADIUS);

        // draw same in Gaussian Blur Image
        ellipse(lastSeed.x + width+horizMargin, lastSeed.y, POINT_RADIUS, POINT_RADIUS);

        // draw same in Fz Image (Laplacian)
        ellipse(lastSeed.x, lastSeed.y + height + verticalMargin, POINT_RADIUS, POINT_RADIUS);

        // draw same in Fg (same width/height) Image (Gradient Magnitude)
        ellipse(lastSeed.x + width+horizMargin, lastSeed.y + height + verticalMargin, POINT_RADIUS, POINT_RADIUS);

        // draw same in Fd (Large x3) Image (Gradient Direction)
        //ellipse(lastSeed.x*FD_MULTIPLIER+1, lastSeed.y*FD_MULTIPLIER+1 + (height + verticalMargin)*2, POINT_RADIUS*3, POINT_RADIUS*3);

        // draw same in Fd (same width/height) Image (Gradient Direction)
        ellipse(lastSeed.x + (width+horizMargin)*2, lastSeed.y + height + verticalMargin, POINT_RADIUS, POINT_RADIUS);


        if( initialSeedSelected ) {
            ellipse(lastSeed.x, lastSeed.y, POINT_RADIUS, POINT_RADIUS);      // put a red mark
            int liveWireSize = _scissor.liveWire.size();
            for(int i = 0; i < liveWireSize; i++ ) {
                PVector p = _scissor.liveWire.get(i);            // get live wire points, and plot
                point(p.x, p.y);
            }
        }
    }
    //else
        //cursor(ARROW);
        
    // draw cooled path
    stroke(135,206,250);        // light blue
    int cooledPathSize = cooledPath.size();
    for(int i = 0; i < cooledPathSize; i++ ) {
        PVector p = cooledPath.get(i);            // get live wire points, and plot
        point(p.x, p.y);
    }
    
    if(displayPathEvaluted) {
        stroke(200,200,255);
        ArrayList evaluated = _scissor.getEvaluatedPathList();
        if( evaluated.size() > 0 ) {
            if(evaluatedDisplayCounter++ < 3) {
                for(int i = 0; i < evaluated.size(); i++) {
                    PVector p = evaluated.get(i);
                    point(p.x, p.y);
                }
            }
            else {
                evaluated.clear();
                evaluatedDisplayCounter = 0;
            }
        }
    }
}
int evaluatedDisplayCounter = 0;

boolean doubleclick = false;
void singleClicked() {
    doubleclick = false;
    if( isWithinOrigImage(mouseX, mouseY) ) {
        // if there are any in liveWire, then, move to CooledPath
        if( initialSeedSelected )
            cooledPath.addAll(_scissor.liveWire);
        
        initialSeedSelected = true;
        initialSeed.x = lastSeed.x;
        initialSeed.y = lastSeed.y;
    
        // use cooledPath & liveWire to store points
        //seedPoints[ seedLength ] = lastSeed;        // remember last point clicked
        //seedLength++;
   
        _scissor.initLiveWire(initialSeed.x, initialSeed.y);
        _scissor.lastx2 = initialSeed.x;
        _scissor.lasty2 = initialSeed.y;
    }
    else if( isWithinFz(mouseX, mouseY) ) {
        dispFz_heatmap = !dispFz_heatmap;
        draw();
    }
    else if( isWithinFg(mouseX, mouseY) ) {
        dispFg_heatmap = !dispFg_heatmap;
        draw();
    }
    else if( isWithinFd(mouseX, mouseY) ) {
        dispFd_heatmap = !dispFd_heatmap;
        draw();
    }
    else if( isWithinResultImage(mouseX, mouseY) ) {
        dispResult_heatmap = !dispResult_heatmap;
        draw();
    }
}


void doubleClicked() {
    if(initialSeedSelected)
        singleClicked();
    initialSeedSelected = false;
    doubleclick = false;
}
void mouseClicked() {       // Source: https://groups.google.com/forum/#!topic/processingjs/5eesRQdlqcE
    // gets fired every time the mouse HAS BEEN clicked - NOT is clicked

    // check for double or single click
    if (!doubleclick){
        // detecting double click
        doubleclick = true;
        c = setTimeout(function() {
            singleClicked();
        }, 250); 
    } else {
        // double click
        doubleclick = false;
        clearTimeout(c);
        doubleClicked();
    }
    
}
void mouseMoved() {
    PVector p = _scissor.getLowestCostPoint(mouseX-SEARCHSIZE, mouseY-SEARCHSIZE, SEARCHSIZE, SEARCHSIZE, width, height);
    if(p.x < 0 || p.y < 0 ) return;
    
    lastSeed.x = p.x;
    lastSeed.y = p.y;

    if(initialSeedSelected) {      // draw shorted path line
        if( isWithinOrigImage(mouseX, mouseY) ) {

            //line(initialSeed.x, initialSeed.y, lastSeed.x, lastSeed.y);       // draws a STRAIGHT line
            _scissor.calculateLiveWire_usingLowestCost(initialSeed.x, initialSeed.y, lastSeed.x, lastSeed.y);     // draws a LOWEST COST path
            //calculateLiveWire_usingLowestCost(_scissor.lastx2, _scissor.lasty2, lastSeed.x, lastSeed.y);     // draws a LOWEST COST path
            
        }
    }
}
void keyPressed() {

    //if (keyIndex == ESC) {        // ESC is not captured.. Browser seems to use them, in cloud IDE?
        initialSeedSelected = false;
    //}

}

