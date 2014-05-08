// import java.util.PriorityQueue;

public class IntelligentScissor {




//String imgPath = "images/bacteria-s.jpg";
//String imgPath = "images/hepatic_artery.jpg";
//String imgPath = "images/lmca-stenosis2.jpg";
//String imgPath = "images/carotid-side2.jpg";
//String imgPath = "images/ct-brain-s.jpg";
//String imgPath = "images/carotid3.jpg";
//String imgPath = "images/left-artery.jpg";

//String imgPath;
IntelligentScissor(String imgFilePath) {
    imgPath = imgFilePath;
}

IntelligentScissor(String imgFilePath, IntelligentScissor prevIS) {     // use same setting as before
    imgPath = imgFilePath;
    if(prevIS != null ) {
        //displayPathEvaluted = prevIS.displayPathEvaluted;
        usePriorityQueue = prevIS.usePriorityQueue;
        optFzDisplayMode = prevIS.optFzDisplayMode;
        optFgDisplayMode = prevIS.optFgDisplayMode;
        optFdDisplayMode = prevIS.optFdDisplayMode;
        optFdDisplayMode = prevIS.optFdDisplayMode;
        optDisplayFd = prevIS.optDisplayFd;
        optResultImageDisplayMode = prevIS.optResultImageDisplayMode;
        FRAME_RATE = prevIS.FRAME_RATE;
    }
}

ArrayList _evaluatedPathList = new ArrayList();
ArrayList getEvaluatedPathList() {
    return _evaluatedPathList;
}

//PVector[] seedPoints = new PVector[1000];  // use cooledPath & liveWire to store points

//public bool displayPathEvaluted = false;
bool usePriorityQueue = true;

//String optFzDisplayMode = "default";        // Fz - LaPlacian Image (grayscale)
String optFzDisplayMode = "heatmap";        // Fz - LaPlacian Heatmap color


//String optFgDisplayMode = "default";      // Fg - Gradient Image (grayscale)
String optFgDisplayMode = "heatmap";        // Fg - Gradient Image Heatmap color

//String optFdDisplayMode = "default";        // Fd - Gradient Direction Image (grayscale)
String optFdDisplayMode = "heatmap";        // Fd - Gradient Direction Image Heatmap color

public boolean optDisplayFd = true;     // display 3xwidth,3xheight of 'Fd' value. Otherwise, draws original image magnified 3 times width & height
//boolean optDisplayFd = false;


//String optResultImageDisplayMode = "default";       // for Resultant image (grayscale)
String optResultImageDisplayMode = "heatmap";       // for Resultant image (heatmap)


int FRAME_RATE = 3;
//int FRAME_RATE = 10;     // 10 is normal


PImage orig;
PImage imgGray;       // Grayscale version
PImage imgBlur;       // Gaussian blurred image
double[] laplacian;    // result of Laplacian calculation

PImage Fz;              // Zero crossing from Laplacian
PImage Fg;
PImage Fd;      // this width*3 and height*3
PImage Fd_smaller;  // this is original image width & size

PImage Fz_bw;       // black/white version
PImage Fg_bw;
PImage Fd_smaller_bw;
PImage imgResult_bw;

double wz = 0.43;
double wg = 0.14;
double wd = 0.43;

PImage imgResult;     // resultant image that combines all 3 costs

double[] Ix;      // Partials of an Image in x
double[] Iy;      // Partials of an Image in y
double[] G;       // Magnitude of Ix & Iy

double[] costFz;
double[] costFg;        
double[] costFd;   // Fd(p,q) expands 3 times both in x-direction and y-direction
double[] costFd_smaller;    // this is same width/height as original for Fd cost

// for Dijkstra algorithm
double[] cost;        // this is a cumulative cost from x1,y1 - uses pixel coordinates.
bool[] visited;     // uses pixel dimension, x, y
int[] prev_x;  // uses pixel dimension x, y
int[] prev_y;

ArrayList liveWire = new ArrayList();       // contains list of points for live wire

double[] kernel_gaussian_blur_3x3 = {
    1/16,  1/8, 1/16,
    1/8, 1/4, 1/8,
    1/16,  1/8, 1/16
};
double[] kernel_gaussian_blur_5x5 = {
    1/273,  4/273,  7/273,  4/273,  1/273,
    4/273,  16/273,  26/273,  16/273,  4/273,
    7/273,  26/273,  41/273,  26/273,  7/273,
    4/273,  16/273,  26/273,  16/273,  4/273,
    1/273,  4/273,  7/273,  4/273,  1/273
};
/*
double[] kernel_laplacian = {
    0,  -1, 0,
    -1, 4, -1,
    0,  -1, 0
};
*/

// matrix from http://www.fmwconcepts.com/imagemagick/laplacian/

double[] kernel_laplacian = {     // 7x7 laplacian kernel

-10, -5, -2, -1, -2, -5, -10,
-5, 0, 3, 4, 3, 0, -5,
-2, 3, 6, 7, 6, 3, -2,
-1, 4, 7, 8, 7, 4, -1,
-2, 3, 6, 7, 6, 3, -2,
-5, 0, 3, 4, 3, 0, -5,
-10, -5, -2, -1, -2, -5, -10,
};
/*
double[][] kernel_gradient_x_sobel_3x3 = {  // does not have perfect rotational symmetry
    -1, 0, 1,
    -2, 0, 2,
    -1, 0, 1
};
double[][] kernel_gradient_y_sobel_3x3 = {
     1,  2,  1,
     0,  0,  0,
    -1, -2, -1
};
*/
double[] kernel_gradient_x_scharr_3x3 = {     // scharr operator (rotational symmetry)
     3, 0, -3,
    10, 0, -10,
     3, 0, -3
};
double[] kernel_gradient_y_scharr_3x3 = {
     3,  10,  3,
     0,   0,  0,
    -3, -10, -3
};

public void setWeight(double wz_, double wg_, double wd_) {
    wz = wz_;
    wg = wg_;
    wd = wd_;
}


// colorMode(HSB, 1) should have been called
color getColorHeatmapInHSBMode(double v) {  // v=0, blue. v=1 means red.
    if(v > 1) {
        v = 0;
    }
    else if(v < 0 ) {
        v = 0;
    }
    return color((1-v)*.6667,1,1);      // hue goes 0-240 (red to blue)
}

int width;
int height;
int image_size;
int FD_MULTIPLIER = 3;

double getHueFromRGB(int intValue) {

    double r = (double)((intValue>>16) & 0xFF);
    double g = (double)((intValue>>8) & 0xFF);
    double b = (double)(intValue & 255);
    r /= 255.0, g /= 255.0, b /= 255.0;
    
    double max = Math.max(r, g, b), min = Math.min(r, g, b);
    double h, s, l = (max + min) / 2.0;

    if(max == min){
        h = s = 0; // achromatic
    }else{
        double d = max - min;
        s = l > 0.5 ? d / (2.0 - max - min) : d / (max + min);
        switch(max){
            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
            case g: h = (b - r) / d + 2; break;
            case b: h = (r - g) / d + 4; break;
        }
        h /= 6.0;
    }

    //return [h, s, l];
    return h;
}
    
public void processImage(PImage img) {
    queue = new ArrayList();        // this is cooled-path queue.
    orig = img;
    width = orig.width;
    height = orig.height;
    image_size = orig.width * orig.height;
    
    imgGray = createImage(width, height, RGB);
    imgBlur = createImage(width, height, RGB);
    laplacian = new double[image_size];
    Fz = createImage(width, height, RGB);
    Fg = createImage(width, height, RGB);
    Fd = createImage(width*FD_MULTIPLIER, height*FD_MULTIPLIER, RGB);
    Fd_smaller = createImage(width, height, RGB);
    
    Fz_bw = createImage(width, height, RGB);
    Fg_bw = createImage(width, height, RGB);
    Fd_smaller_bw = createImage(width, height, RGB);
    
    imgResult = createImage(width, height, RGB);
    imgResult_bw = createImage(width, height, RGB);
    
    Ix = new double[image_size];
    Iy = new double[image_size];
    G = new double[image_size];

    costFz = new double[image_size];
    costFg = new double[image_size];
    costFd = new double[image_size * FD_MULTIPLIER * FD_MULTIPLIER];     // *9 = 3x3. width expands 3 times, height expands 3 times
    costFd_smaller = new double[image_size];

    cost = new double[image_size];        // this is a cumulative cost from x1,y1 - uses pixel coordinates.
    visited = new bool[image_size];     // uses pixel dimension, x, y
    prev_x = new int[image_size];  // uses pixel dimension x, y
    prev_y = new int[image_size];
/*
    for(int row=0; row < height; row++)
        for(int i=0;i < image_size; i++)
            Ix[i] = 0;
            Iy[i] = 0;
            G[i] = 0;
            costFz[i] = 0;
            costFg[i] = 0;
        }
*/

    recalculateImageProcessing();
}

public void recalculateImageProcessing() {
    
    ///////////////////////////// GRAY SCALE
    
    for (int i=0; i<image_size; i++) { 
        imgGray.pixels[i] = orig.pixels[i] & 0xff;        // conver to Grayscale image
    }
    imgGray.updatePixels();

    ///////////////////////////// GAUSSIAN BLUR
    applyMatrixImageToImage(imgGray, imgBlur, kernel_gaussian_blur_3x3, false);     // Grayscale to Gaussian blur
    imgBlur.updatePixels();
    
    ///////////////////////////// LaPlacian Calculation using Kernel

    int row=0;
    int col=0;
    for(int i = 0; i < image_size; i++ ) {
        double v = convolution_grayscale_pimage_1dkernel(row, col, imgBlur, kernel_laplacian );
        //double v = convolution_grayscale_pimage_1dkernel(row, col, imgGray, kernel_laplacian, kernel_laplacian[0].length );
        
        laplacian[i] = v;
        if( ++col >= width ) {
            row++;
            col = 0;
        }
    }
    
    ///////////////////////////// Fz - Zero crossing on LaPlacian
    
    applyFz(laplacian, Fz, Fz_bw, costFz);      // (src, target, target_bw, cost)
    
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    
    ///////////////////////////// Ix, Iy calculation (Gradient)

    applyMatrixImageToDouble(imgBlur, Ix, kernel_gradient_x_scharr_3x3, false);     // Gradient operator
    applyMatrixImageToDouble(imgBlur, Iy, kernel_gradient_y_scharr_3x3, false);     // Gradient operator

    
    // Calculate Gradient magnitude
    double maxG = -1, minG = 256;
    for(int i = 0; i < image_size; i++ ) {
        double x = Ix[i];
        double y = Iy[i];
        double mag = Math.sqrt( x*x + y*y );
        G[i] = mag;
        if( mag > maxG )        // also get maximum G
            maxG = mag;
        if( mag < minG )        // also get minimum G
            minG = mag;
    }
    
    ///////////////////////////// Fg - Gradient Magnitude - produce heatmap image
    colorMode(HSB, 1);
    
    boolean IsFg_HeatmapMode = optFgDisplayMode.equals("heatmap");
    
    for(int i = 0; i < image_size; i++ ) {
        //int idx = row * width + col;
        double v = 1 - (((double)G[i]) - minG) / maxG;       // 1 is high
        
        if( IsFg_HeatmapMode ) {
            Fg.pixels[i] = getColorHeatmapInHSBMode(v);        // generates blue to red, heatmap
        }
        else {
            Fg.pixels[i] = color(1,0,v);      // generates black to white grayscale-heatmap
        }
        Fg_bw.pixels[i] = color(1,0,v);     // grayscale
        
        costFg[i] = v;        // [0] is its own magnitude of image gradient

    }
    Fg.updatePixels();
    
    ///////////////////////////// Fd - Gradient Direction Cost Function
    applyFd(imgBlur, Ix, Iy, Fd, costFd);

    // create a original width/height image of Fd, which is 3 times bigger in _scissor.Fd
    
    Fd_smaller.copy(_scissor.Fd,0,0,width*3,height*3,           // source image
                            0,0,orig.width,orig.height);        // destination
                            
    // produce bw copy from Fd_smaller (widthxheight)
    for(int i = 0; i < image_size; i++) {
        int pixel = Fd_smaller.pixels[i];
        int avg = ((pixel & 0x000000ff) + ((pixel & 0x0000ff00) >> 8) + ((pixel & 0x00ff0000)>>16)) / 3;
        int new_pixel = (avg << 16) + (avg << 8) + avg + 0xff000000;            // 0xff000000 is full alpha
        Fd_smaller_bw.pixels[i] = new_pixel;
    }
    Fd_smaller_bw.updatePixels();
                            
    for(int i = 0; i < image_size; i++ ) {
        costFd_smaller[i] = getHueFromRGB(Fd_smaller.pixels[i]) / 0.6667;
    }
    
    processResultantImage();

    colorMode(RGB);
    
}
public void processResultantImage() {
 ///////////////////////////// imgResult - generate resultant image
    colorMode(HSB, 1);
    boolean isHeatmap = optResultImageDisplayMode.equals("heatmap");
    
    for(int i = 0; i < image_size; i++ ) {
        //int idx = row * width + col;
        double v = calculateCost2(i) + costFd_smaller[i]*wd;
        
        if(isHeatmap) {
            imgResult.pixels[i] = getColorHeatmapInHSBMode(v);   // generates blue to red, heatmap
        }
        else {
            imgResult.pixels[i] = color(1,0,v);   // generates grayscale-heatmap
        }
        imgResult_bw.pixels[i] = color(1,0,v);
    }
    imgResult.updatePixels();
    imgResult_bw.updatePixels();
}

double calculateCost2(int idx) {  
//    if( row < 0 || row >= height || col < 0 || col >= width )       //if invalid
//        return 9999999999;
        
    return costFz[idx] * wz + costFg[idx] * wg;
}
// only returns cost of Fd, given a neighbor!
double calculateFdCost3(int idx3) {  // row_neighbor3 is 3 times row!
//    if(row_neighbor3 <0 || row_neighbor3>= height*3+1 || col_neighbor3 <0 || col_neighbor3 >= width*3+1 )
//        return 9999999999;
    return costFd[idx3] * wd;      // non-initialized costFd causes NaN
}

// passing by int[], seems to make a COPY not REFERENCE... Therefore, use PImage to pass argument by reference!
void applyMatrixImageToImage(PImage source, PImage target, double[] kernel, bool limitTo255) {
    int width = source.width;
    int size = width * source.height;
    
    int row = 0;
    int col = 0;

    for(int i = 0; i < size; i++ ) {
        double v = convolution_grayscale_pimage_1dkernel(row, col, source, kernel);
        if( limitTo255 )
            v = ((int)v) & 0xff;
        target.pixels[i] = color(v, v, v);
        if( ++col >= width ) {
            row++;
            col = 0;
        }
    }
    
}
void applyMatrixImageToDouble(PImage source, double[] target, double[] kernel, bool limitTo255) {
    int width = source.width;
    int size = width * source.height;
    
    int row = 0;
    int col = 0;

    for(int i = 0; i < size; i++ ) {
        double v = convolution_grayscale_pimage_1dkernel(row, col, source, kernel);
        if( limitTo255 )
            v = ((int)v) & 0xff;
        target[i] = v;
        if( ++col >= width ) {
            row++;
            col = 0;
        }
    }
    
}
void applyFz(double[] laplacian, PImage target, PImage target_bw, double[] costFz) {
    //////////////////////// Fz

    colorMode(RGB);     // rgb mode

    int width = target.width;
    int height = target.height;

    int LOW_COST = 0;
    int HIGH_COST = 1;
    int LOW_COST_COLOR, HIGH_COST_COLOR;
    int LOW_COST_COLOR_BW = color(0,0,0), HIGH_COST_COLOR_BW = color(255,255,255);
    
    if(optFzDisplayMode.equals("heatmap")) {     // heatmap mode
        LOW_COST_COLOR = color(0,0,255);      // blue
        HIGH_COST_COLOR = color(255,0,0);     // red    
    }
    else {              // grayscale mode
        LOW_COST_COLOR = color(0,0,0);          // black
        HIGH_COST_COLOR = color(255,255,255);   // white    
    }

    

    double v;
    int row = 0;
    int col = 0;
    for(int idx = 0; idx < image_size; idx++) {
        //int idx = row * width + col;
        int idx_right = idx + 1;

        v = laplacian[idx];

        target.pixels[idx] = HIGH_COST_COLOR;        // assume highest cost for each pixel
        target_bw.pixels[idx] = HIGH_COST_COLOR_BW;
        costFz[idx] = HIGH_COST;
        
        int IsZeroCrossing = 0;    // any thing other than 0, would be YES.
        
        if( v == 0 )
            IsZeroCrossing = 1;
        else if( idx_right < image_size && col < width-1 ) { // make sure within a rnage
            dobule v_right = laplacian[idx_right];
            if( v_right > 0 && v < 0 || v_right < 0 && v > 0 ) { // is changing + to -, or - to + ?
                if( Math.abs(v) < Math.abs(v_right) ) {     // if this pixel is closer to zero than right neighbor
                    target.pixels[idx] = LOW_COST_COLOR;
                    target_bw.pixels[idx] = LOW_COST_COLOR_BW;
                    costFz[idx] = LOW_COST;
                }
                else {
                    target.pixels[idx_right] = LOW_COST_COLOR;
                    target_bw.pixels[idx_right] = LOW_COST_COLOR_BW;
                    costFz[idx_right] = LOW_COST;
                }
            }
        }
        
        idx_down = idx + width; // (row+1)*width + col; // look at one pixel below
        if( idx_down < image_size ) {
            double v_down = laplacian[idx_down];
            if( v_down > 0 && v < 0 || v_down < 0 && v > 0 ) { // is changing + to -, or - to + ?
                if( Math.abs(v) < Math.abs(v_down) ) {     // if this pixel is closer to zero than down neighbor
                    target.pixels[idx] = LOW_COST_COLOR;
                    target_bw.pixels[idx] = LOW_COST_COLOR_BW;
                    costFz[idx] = LOW_COST;
                }
                else {
                    target.pixels[idx_down] = LOW_COST_COLOR;
                    target_bw.pixels[idx_down] = LOW_COST_COLOR_BW;
                    costFz[idx_down] = LOW_COST;
                }
            }
        }
        
        if( ++col >= width ) {
            row++;
            col = 0;
        }
    }
            
    target.updatePixels();
    target_bw.updatePixels();
/*    
    string s= "";
    for(int row = 0; row < 10; row++) {
        s = "";
         for(int col = 0; col < width; col++) {
            int idx = row * width + col;
            
            v = laplacian[idx];
            
            s += v + ",";
        }
        println(s);
    }
*/

}

double getNorm(px, py) {
    return Math.sqrt(px*px+py*py);
}
// px,py is a positional vector, and qx,qy is a neighboring vector
// dpx,dpy is a gradient vector of px,py
// dqx,dqy is a gradient vector of qx,qy
double calculateFdWithGradient(double px,double py,double qx,double qy,double dpx,double dpy,double dqx,double dqy) {

    if(dpx == 0 && dpy == 0 ) return 0;     // when gradient is zero, assume that's lowest cost path! otherwise, NaN error occurs.
    if(dqx == 0 && dqy == 0 ) return 0;     
    
    double dp_norm = getNorm(dpx,dpy);
    double Dpx = dpx / dp_norm;
    double Dpy = dpy / dp_norm;
    
    double D_prime_px =  Dpy;       // Normal vector to Dpx,Dpy
    double D_prime_py = -Dpx;

    double dq_norm = getNorm(dqx,dqy);
    double Dqx = dqx / dq_norm;
    double Dqy = dqy / dq_norm;

    double D_prime_qx =  Dqy;       // Normal vector to Dqx,Dqy
    double D_prime_qy = -Dqx;

    double L_pq_x, L_pq_y;      // Eq (6)
    double L_condition = D_prime_px * (qx-px) + D_prime_py * (qy-py);  // Eq (6) from long paper version
    if( L_condition <= 0 ) {
        L_pq_x = qx-px;
        L_pq_y = qy-py;
    }
    else {
        L_pq_x = px-qx;
        L_pq_y = py-qy;
    }
    double L_pq_norm = getNorm(L_pq_x, L_pq_y);
    L_pq_x /= L_pq_norm;
    L_pq_y /= L_pq_norm;
    
    double dp_pq = D_prime_px * L_pq_x + D_prime_py * L_pq_y;      // Eq (5) from long paper version
    double dq_pq = L_pq_x * D_prime_qx + L_pq_y * D_prime_qy;
    
    return (Math.acos(dp_pq) + Math.acos(dq_pq)) * 2 / (3 * Math.PI);
}
double calculateFd(double px,double py,double qx,double qy) {
    int p_idx = py*width+px;
    double dpx = Ix[p_idx];        // Ix[row][col].. row=py, col=px 
    double dpy = Iy[p_idx];
    
    int q_idx = qy*width+qx;
    double dqx = Ix[q_idx];
    double dqy = Iy[q_idx];
    
    double fd = calculateFdWithGradient(px, py, qx, qy, dpx, dpy, dqx, dqy);
    if(fd>1) fd=1;
    else if(fd<0) fd=0;
    return fd;
}
// value is from 0 to 1. 0 = blue, 1 = red.
int getCorrectColor(double value, String dispMode) {
    int r;
    if(dispMode.equals("heatmap")) {
        r = getColorHeatmapInHSBMode(value);     // must use colorMode(HSB,1) before using this function
    }
    else { // if(mode == 'default') {   // grayscale
        r = color(1, 0, value);     // must use colorMode(HSB,1). (1,0,value) = hue (white), 0=zero saturation, value=brightness
    }
    
    return r;
}
// Ix,Iy: Gradient Direction of Image 'source'
void applyFd(PImage source, double[] Ix, double[] Iy, PImage target, double[] costFd) {        // Gradient Direction
    //////////////////////// Fd
 
    colorMode(HSB, 1);
    //colorMode(RGB);

    int tgt_width = width * FD_MULTIPLIER;      // Fd image (target) is 3 times width & 3 times height
    
//int count=0;
    double px, py, qx, qy;      // px,py = position of pixel p. qx,qy = position of neighbor pixel q.
    double fd;      // fd value
    double v;
    double one_over_sqrt_of_2 = 1/Math.sqrt(2);        // horizontal & vertical neighbors have lengths of 'sqrt(2)', while diagonal have '1'.
    
    int row = 0;
    int col = 0;
    for(int i = 0; i < image_size; i++ ) {

        int tgt_row = row * FD_MULTIPLIER + 1;      // target image is 3 times bigger in both x & y direction
        int tgt_col = col * FD_MULTIPLIER + 1;
        int tgt_idx = tgt_row * tgt_width + tgt_col;

        costFd[tgt_idx] = 0;
        
        int src_idx = row * width + col;    // this is original image index (3 times smaller than target)
     
        px = col; py = row;  
//count++;


        if( px < width-1 ) {    // east
            int tgt_idx_e = tgt_idx + 1;            // neighbor q which is 'East' of 'p'. skip by 3, since 3 neighbor pixels wide per pixel
            fd = calculateFd(px, py, px+1, py);
            //if(count<100) println(px+","+py+",I(p)="+Ix[px][py]+","+Iy[px][py] + ", I(q)=" + Ix[px+1][py]+ "," + Iy[px+1][py] + fd);
            if(optDisplayFd) target.pixels[tgt_idx_e] = getCorrectColor(fd, optFdDisplayMode) * one_over_sqrt_of_2;
            costFd[tgt_row][tgt_col+1] = fd;
        }
       
        if( px > 1 ) {  // west
            int tgt_idx_w = tgt_idx - 1;            // neighbor q which is 'West' of 'p'
            fd = calculateFd(px, py, px-1, py) * one_over_sqrt_of_2;
            costFd[tgt_row][tgt_col-1] = fd;
            if(optDisplayFd) target.pixels[tgt_idx_w] = getCorrectColor(fd, optFdDisplayMode);
        }
        
        if( py > 1 ) {  // north
            int tgt_idx_n = tgt_idx - tgt_width;  // neighbor q which is 'North' of 'p'
            fd = calculateFd(px, py, px, py-1) * one_over_sqrt_of_2;
            costFd[tgt_idx - tgt_width] = fd;
            if(optDisplayFd) target.pixels[tgt_idx_n] = getCorrectColor(fd, optFdDisplayMode);
        }
        
        if( py < height - 1 ) {     // south
            int tgt_idx_s = tgt_idx + tgt_width;  // neighbor q which is 'South' of 'p'
            fd = calculateFd(px, py, px, py+1) * one_over_sqrt_of_2;
            costFd[tgt_idx + tgt_width] = fd;
            if(optDisplayFd) target.pixels[tgt_idx_s] = getCorrectColor(fd, optFdDisplayMode);
        }            
     
        if( px < width-1 && py > 1 ) {      // NorthEast
            int tgt_idx_ne = (tgt_idx+1) - tgt_width;  // neighbor q which is 'NorthEast' of 'p'
            fd = calculateFd(px+1, py, px, py-1);
            costFd[tgt_idx - tgt_width + 1] = fd;
            if(optDisplayFd) target.pixels[tgt_idx_ne] = getCorrectColor(fd, optFdDisplayMode);
        }
            
        if( px > 1 && py > 1 ) {      // NorthWest
            int tgt_idx_nw = (tgt_idx-1) - tgt_width;  // neighbor q which is 'NorthWest' of 'p'
            fd = calculateFd(px-1, py, px, py-1);
            costFd[tgt_idx - tgt_width - 1] = fd;
            if(optDisplayFd) target.pixels[tgt_idx_nw] = getCorrectColor(fd, optFdDisplayMode);
        }

        if( px > 1 && py < height - 1 ) {      // SouthWest
            int tgt_idx_sw = (tgt_idx-1) + tgt_width;  // neighbor q which is 'SouthWest' of 'p'
            fd = calculateFd(px-1, py, px, py+1);
            costFd[tgt_idx + tgt_width - 1] = fd;
            if(optDisplayFd) target.pixels[tgt_idx_sw] = getCorrectColor(fd, optFdDisplayMode);
        }
        if( px < width-1 && py < height - 1 ) {      // SouthEast
            int tgt_idx_se = (tgt_idx+1) + tgt_width;  // neighbor q which is 'SouthEast' of 'p'
            fd = calculateFd(px+1, py, px, py+1);
            costFd[tgt_idx + tgt_width + 1] = fd;
            if(optDisplayFd) target.pixels[tgt_idx_se] = getCorrectColor(fd, optFdDisplayMode);
        }            
        
        if(optDisplayFd) 
            target.pixels[tgt_idx] = 0;
        else
            target.pixels[tgt_idx] = source.pixels[src_idx]; // source[col][row];
         
        if( ++col >= width ) {
            row++;
            col = 0;
        }        
    }
            
    target.updatePixels();

}
PVector getLowestCostPoint(int x, int y, int w, int h, int img_width, int img_height) {

    PVector p = new PVector(x, y);
    double minCost = calculateCost2(y*img_width + x);       // y=row, x=col, set the min cost with first pixel
    for(int row = y; row < y+h; row++) {
        for(int col = x; col < x+w; col++) {
        
            int row2 = constrain(row, 0, img_height - 1 ); // don't modify 'row'.. it can go to infinite loop
            int col2 = constrain(col, 0, img_width - 1 );
            int idx = row2*img_width+col2;
        
            double v = calculateCost2(idx);
            if( v < minCost ) {
                minCost = v;
                p.y = row2;
                p.x = col2;
                
            }
            
        }
    }
    //println(x+","+y+","+w+","+h+".." + img_width + "," + img_height + ",...min=" + minCost);
    return p;
    
}


double INFINITY = 999999;
ArrayList queue = new ArrayList();      // priority queue
int lastx2;
int lasty2;     // this is used to start searching from this point when mouse moves
void initLiveWire(int x1, int y1) {

    for(int i = 0; i < image_size; i++) {
        cost[i] = INFINITY;
        visited[i] = false;
        prev_x[i] = -1;
        prev_y[i] = -1;
    }
//println("x1=" + x1 + ",y1=" + y1 + ",idx=" + (y1*width+x1));
    queue.add( new PVector(x1, y1) );       // enter initial start node
    cost[y1*width+x1] = 0;
    visited[y1*width+x1] = true;
}
void calculateLiveWire_usingLowestCost(int x1, int y1, int x2, int y2) {         // (Dijkstra)

    int fd_width = width * FD_MULTIPLIER;
/*
int xx=10*3;
println(costFd[xx]+","+costFd[1]+","+costFd[2]);
println(costFd[fd_width+xx]+","+costFd[fd_width+xx+1]+","+costFd[fd_width+xx+2]);
println(costFd[fd_width*2+xx]+","+costFd[fd_width*2+xx+1]+","+costFd[fd_width*2+xx+2]);

println(costFd[fd_width*3+xx]+","+costFd[fd_width*3+xx]+","+costFd[fd_width*3+xx+2]);
println(costFd[fd_width*4+xx]+","+costFd[fd_width*4+xx]+","+costFd[fd_width*4+xx+2]);
println(costFd[fd_width*5+xx]+","+costFd[fd_width*5+xx+1]+","+costFd[fd_width*5+xx+2]);

println(costFd[fd_width*6+xx]+","+costFd[fd_width*6+xx]+","+costFd[fd_width*6+xx+2]);
println(costFd[fd_width*7+xx]+","+costFd[fd_width*7+xx]+","+costFd[fd_width*7+xx+2]);
println(costFd[fd_width*8+xx]+","+costFd[fd_width*8+xx+1]+","+costFd[fd_width*8+xx+2]);

return;
*/
/*
lastx2=6; lasty2=6;
x1=3; y1=3; x2=6;y2=6;
queue.remove(0);
queue.add( new PVector(x1,y1));
cost[y1*width+x1]=0;
visited[y1*width+x1] = true;

println("pass 2.." + (y2*width+x2) + "," + cost[y2*width+x2] );
*/
    
    if( cost[y2*width+x2] >= INFINITY-1 ) {      // if not explored path. (-1 for just to ensure floating error)

//int counter = 100;

        queue.add( new PVector(lastx2, lasty2) );       // lastx2,lastxy contains last searched node

        while( queue.size() > 0 ) {
/*        
if(--counter <0) {
    println("too many iteration.");
    return;
}
*/
            // find the lowest cost from queue, and remove it. (simulates Priority Queue)
            // needs optimization.
            double minCost = INFINITY;
            int minIndex = -1;
            PVector minV;
            
            if(usePriorityQueue) {  // priority queue (faster)
                for(int i = 0; i < queue.size(); i++ ) {

                    PVector v = queue.get(i);
                    double newCost = cost[v.y*width+v.x];

                    if( minCost > newCost ) {
                        minCost = newCost;
                        minIndex = i;
                        minV = v;
                    }
                }
                queue.remove(minIndex);        // same as dequeue() using lowest priority
                
            }
            else {      // use regular queue (slow)
                minV = queue.get(0);        // remove 1st one
                minIndex = 0;
                queue.remove(0);
            }
//println("queue min cost=" + minCost + ",minIndex=" + minIndex + ":::minV.x&y=" + minV.x + "," + minV.y );

            if( minV.x == x2 && minV.y == y2 )
                break;

            // Now check neighbors, and add to queue if it needs to be explored further
            // look east
            ArrayList neighbors = new ArrayList();      // this is a potential neighbors
            ArrayList neighbors_cost_rowcol = new ArrayList();        // this is row,col vector to costFd[][] (row is 3 times larger than orig)
            
            int y3 = minV.y*FD_MULTIPLIER+1;      // access to costFd[]
            int x3 = minV.x*FD_MULTIPLIER+1;
            
            if( minV.x < width - 1 ) {
                neighbors.add( new PVector(minV.x+1, minV.y) );     // east
                neighbors_cost_rowcol.add( y3*fd_width+x3+1 );  
            }
            if( minV.x > 0 ) {
                neighbors.add( new PVector(minV.x-1, minV.y) );
                neighbors_cost_rowcol.add( y3*fd_width+x3-1 );      // west
            }
            if( minV.y > 0 ) {
                neighbors.add( new PVector(minV.x, minV.y-1) );     // north
                neighbors_cost_rowcol.add( (y3-1)*fd_width+x3 );
            }
            if( minV.y < height - 1 ) {
                neighbors.add( new PVector(minV.x, minV.y+1) );     // south
                neighbors_cost_rowcol.add( (y3+1)*fd_width+x3 );    
            }
            if( minV.x < width - 1 && minV.y > 0 ) {
                neighbors.add( new PVector(minV.x+1, minV.y-1) );
                neighbors_cost_rowcol.add((y3-1)*fd_width+x3+1 );   // northeast
            }
            if( minV.x > 0 && minV.y > 0 ) {
                neighbors.add( new PVector(minV.x-1, minV.y-1) );   // northwest
                neighbors_cost_rowcol.add( (y3-1)*fd_width+x3-1 );
            }
            if( minV.x < width - 1 && minV.y < height - 1 ) {
                neighbors.add( new PVector(minV.x+1, minV.y+1) );
                neighbors_cost_rowcol.add( (y3+1)*fd_width+x3+1 );  // southeast
            }
            if( minV.x > 0 && minV.y < height - 1 ) {
                neighbors.add( new PVector(minV.x-1, minV.y+1) );
                neighbors_cost_rowcol.add( (y3+1)*fd_width+x3-1 );   // southwest
            }

            // now check whether neighbor is worthy of exploration. (Not visited == ok!, Visited but cost is less == ok!)
            while( neighbors.size() > 0 ) {

                PVector n = neighbors.get(0);       // always remove first one to process
                neighbors.remove(0);
                
                int idx3 = neighbors_cost_rowcol.get(0);        // this contains correct neighbor's row & col to access costFd[][]
                neighbors_cost_rowcol.remove(0);
                
                //println("idx3=" + idx3);
                
                int n_idx = n.y*width + n.x;
                
                double neighbor_cost = calculateCost2(n_idx) + calculateFdCost3( idx3 );       // get neighbor's cost - x=row, y=col
                
//println("n.x=" + n.x + ",n.y=" + n.y +". minCost=" + minCost + ",..neighbor cost=" + neighbor_cost + ".. curr neighbor cost=" + cost[n_idx]);
                
                if( visited[n_idx] == false || (visited[n_idx] == true && cost[n_idx] > minCost + neighbor_cost )) {
//println(" *** need to visit: " + n.x + "," + n.y +". n_idx=" + n_idx + ",cost=" + cost[n_idx] + ".. new cost=" + (minCost + neighbor_cost));

                    cost[n_idx] = minCost + neighbor_cost;

                    visited[n_idx] = true;
                    prev_x[n_idx] = minV.x;
                    prev_y[n_idx] = minV.y;
                    /*
                    if(displayPathEvaluted)
                        point(n.x, n.y);
                    */
                    _evaluatedPathList.add(new PVector(n.x, n.y));
                    queue.add( n );
                    
                }
            }
    
        }
        
    
    }



    int px = prev_x[y2*width+x2];       // traverse with the destination
    int py = prev_y[y2*width+x2];

    if( px != -1 && py != -1 ) {
        lastx2 = px;        // this is saved, so that when mouse moves, search resumes from this point
        lasty2 = py;
    }
    else {
        //println("Error in px,py = " + px + "," + py);
        return;
    }


    liveWire.clear();
    


    //while(px != -1 && py != -1        // (-1,-1) is a serious error!! It's here, just for 
      while( (px != x1 || py != y1) ) {     // draw point until reaching the initial point x1, y1, then stop!
        int p_idx = py*width + px;
        liveWire.add( new PVector(px, py) );
//println("Path=" + px + "," + py);                
        px = prev_x[p_idx];
        py = prev_y[p_idx];
    }
    /*
println("");
println("");
println("");
println("");
println("===");
*/
}



}
  

