// ref: http://processingjs.org/learning/topic/convolution/

double convolution_grayscale_pimage_1dkernel(int row, int col, PImage input, double[] kernel)
{
    int width = input.width;
    int height = input.height;
    double rtotal = 0.0;
    int matrixsize = kernel.length;
    int kernelwidth = (int)Math.sqrt(matrixsize);       // 9 ==> 3x3. kernelwidth = 3
    
    int offset = floor(kernelwidth / 2);
    
    for (int i = 0; i < kernelwidth; i++) {
        for (int j= 0; j < kernelwidth; j++) {
        
          int row2 = constrain(row+i-offset, 0, height-1);
          int col2 = constrain(col+j-offset, 0, width-1);
          int loc = col2 + width*row2;
        
          // Make sure we haven't walked off our image, we could do better here
          loc = constrain(loc,0,width*height-1);
            
          // Calculate the convolution
          rtotal += (input.pixels[loc] & 0xFF) * kernel[i*kernelwidth + j];  // 0xFF is needed to see only 1 channel (grayscale)
    
        }
    }
    //if( limitTo255 )        // for image, should be TRUE.  But, for mathematical calculation, FALSE
        //rtotal = constrain(rtotal,0,255);     // Make sure RGB is within range
    
    return rtotal;          // Return the resulting color
}


double convolution_grayscale_pimage_2dkernel(int row, int col, PImage input, double[][] kernel,int matrixsize)
{
    int width = input.width;
    int height = input.height;
    double rtotal = 0.0;
    int offset = (int)(matrixsize / 2);
    for (int i = 0; i < matrixsize; i++) {
        for (int j= 0; j < matrixsize; j++) {
        
          int row2 = constrain(row+i-offset, 0, height-1);
          int col2 = constrain(col+j-offset, 0, width-1);
          int loc = col2 + width*row2;
        
          // Make sure we haven't walked off our image, we could do better here
          loc = constrain(loc,0,width*height-1);
            
          // Calculate the convolution
          rtotal += (input.pixels[loc] & 0xFF) * kernel[i][j];  // 0xFF is needed to see only 1 channel (grayscale)
    
        }
    }
    //if( limitTo255 )        // for image, should be TRUE.  But, for mathematical calculation, FALSE
        //rtotal = constrain(rtotal,0,255);     // Make sure RGB is within range
    
    return rtotal;          // Return the resulting color
}


    /*
// test convolution
int[] test = [
   1,2,3,4,5,
   6,7,8,9,10,
   11,12,7,14,15,
   16,17,18,19,20,
   21,22,23,24,25
   ];
int[] result = new int[5*5];   
    string s = "";
    int row = 0;
    int col = 0;
    for(int i = 0; i < 5*5; i++ ) {
    
        double v = convolution_grayscale_1dinput(row, col, test, 5, 5, kernel_laplacian, 3);
        //int vi = ((int)v) & 0xffff);
        s += v + ",";
        //result[i] = v;
    
        if( ++col >= 5 ) {
            row++;
            col = 0;
            println(s);
            s = "";
        }
    }
    println(s);
*/

/*
// passing PImage.pixels doesn't work... because it doesn't pass by REFERENCE. (Makes a copy of array) 2014-03-01 (JR)
double convolution_grayscale_1dinput(int row, int col, double[] input, int width, int height, double[][] kernel,int matrixsize)
{
  double rtotal = 0.0;
  int offset = (int)(matrixsize / 2);
  for (int i = 0; i < matrixsize; i++){
    for (int j= 0; j < matrixsize; j++){

      int row2 = constrain(row+i-offset, 0, height-1);
      int col2 = constrain(col+j-offset, 0, width-1);
      int loc = col2 + width*row2;

//println(row2 + "," + col2 + "..." + loc);

      // Make sure we haven't walked off our image, we could do better here
      loc = constrain(loc,0,width*height-1);
        
      // Calculate the convolution
      rtotal += input[loc] * kernel[i][j];

    }

  }

  //rtotal = constrain(rtotal,0,255);     // Make sure RGB is within range

  return rtotal;          // Return the resulting color
}
*/
/*
double convolution_grayscale_2dinput(double[][] input, int width, int height, double[][] kernel,int matrixsize, int row, int col)
{

  double rtotal = 0.0;
  int offset = (int)(matrixsize / 2);
  for (int i = 0; i < matrixsize; i++){
    for (int j= 0; j < matrixsize; j++){

      int row2 = constrain(row+i-offset, 0, height-1);
      int col2 = constrain(col+j-offset, 0, width-1);

      
      rtotal += input[row2][col2] * kernel[i][j];       // Calculate the convolution

    }

  }

  // Make sure RGB is within range

  rtotal = constrain(rtotal,0,255);

  // Return the resulting color
  return rtotal;
}
*/
/*
// don't use this
color convolution(int x, int y, float[][] matrix,int matrixsize, PImage img)

{

  float rtotal = 0.0;
  float gtotal = 0.0;
  float btotal = 0.0;

  int offset = matrixsize / 2;
  for (int i = 0; i < matrixsize; i++){
    for (int j= 0; j < matrixsize; j++){

      // What pixel are we testing

      int xloc = x+i-offset;
      int yloc = y+j-offset;
      int loc = xloc + img.width*yloc;

      // Make sure we haven't walked off our image, we could do better here
      loc = constrain(loc,0,img.pixels.length-1);

      // Calculate the convolution
      rtotal += (red(img.pixels[loc]) * matrix[i][j]);
      gtotal += (green(img.pixels[loc]) * matrix[i][j]);
      btotal += (blue(img.pixels[loc]) * matrix[i][j]);

    }

  }

  // Make sure RGB is within range

  rtotal = constrain(rtotal,0,255);
  gtotal = constrain(gtotal,0,255);
  btotal = constrain(btotal,0,255);

  // Return the resulting color
  return color(rtotal,gtotal,btotal);
}
*/



/*
// this is straight from Java code.. use above for faster implementation
double singlePixelConvolution(double [][] input, 
					      int x, int y,
					      double [][] k,
					      int kernelWidth, 
					      int kernelHeight){
    double output = 0;
    for(int i=0;i<kernelWidth;++i){
      for(int j=0;j<kernelHeight;++j){
	    output = output + (input[x+i][y+j] * k[i][j]);
      }
    }
    return output;
  }

double [][] convolution2D(double [][] input, int width, int height, 
					      double [][] kernel, int kernelWidth, int kernelHeight)
{
    int smallWidth = width - kernelWidth + 1;
    int smallHeight = height - kernelHeight + 1; 
    
    double [][] output = new double [smallWidth][smallHeight];
    for(int i=0;i<smallWidth;++i){
      for(int j=0;j<smallHeight;++j){
        output[i][j]=0;
      }
    }
    for(int i=0;i<smallWidth;++i){
      for(int j=0;j<smallHeight;++j){
        output[i][j] = singlePixelConvolution(input,i,j,
                            kernel, kernelWidth, kernelHeight);
    //if (i==32- kernelWidth + 1 && j==100- kernelHeight + 1) System.out.println("Convolve2D: "+output[i][j]);
      }
    }
    return output;
}
double [][] convolution2DPadded(double [][] input, int width, int height, 
						double [][] kernel, int kernelWidth, int kernelHeight)
{

    int smallWidth = width - kernelWidth + 1;
    int smallHeight = height - kernelHeight + 1; 
    int top = (int)(kernelHeight/2);    // explicit int casting is required, in processingjs
    int left = (int)(kernelWidth/2);
    double small [][] = new double [smallWidth][smallHeight];

    small = convolution2D(input,width,height,
    		  kernel,kernelWidth,kernelHeight);

    double large [][] = new double [width][height];
    for(int j=0;j<height;++j){
      for(int i=0;i<width;++i){
        large[i][j] = 0;
      }
    }
    //println("test..");
    //println("left=" + left + ".. top=" + top);
    for(int j=0;j<smallHeight;++j){
      for(int i=0;i<smallWidth;++i){
    //if (i+left==32 && j+top==100) System.out.println("Convolve2DP: "+small[i][j]);
        large[i+left][j+top]=small[i][j];
      }
    }

    return large;
}
*/