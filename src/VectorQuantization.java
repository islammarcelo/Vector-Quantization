import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

class VectorQuantization {
    static ArrayList<Integer> image = new ArrayList<>();
    static  ArrayList<ArrayList<Integer>> CodeBook = new ArrayList<>();
    static  ArrayList <ArrayList <Integer>> Blocks = new ArrayList<>();
    static  ArrayList<ArrayList<ArrayList<Integer>>> lastNearestVector = new ArrayList<>();
    private   static int vectorSize ;
    private   static int blockW ;
    private   static int blockH ;
    private static int width;
    private static int height;
    private static BufferedImage bufferedImage;


    VectorQuantization() throws FileNotFoundException {
        String FileName = "input.txt";
        File file = new File(FileName);
        Scanner in = new Scanner(file);
        vectorSize = in.nextInt();
        blockW = in.nextInt();
        blockH = in.nextInt();
    }

    public static int[][] readImage(String filePath) {

        File f = new File(filePath); //image file path

        int[][] imageMAtrix = null;

        try {
            BufferedImage img = ImageIO.read(f);
            int oldW = img.getWidth();
            int oldH = img.getHeight();

            imageMAtrix = new int[oldH][oldW];

            for (int y = 0; y < oldH; y++) {
                for (int x = 0; x < oldW; x++) {
                    int p = img.getRGB(x, y);
                    int a = (p >> 24) & 0xff;
                    int r = (p >> 16) & 0xff;
                    int g = (p >> 8) & 0xff;
                    int b = p & 0xff;

                    //because in gray image r=g=b  we will select r

                    imageMAtrix[y][x] = r;

                    //set new RGB value
                    p = (a << 24) | (r << 16) | (g << 8) | b;
                    img.setRGB(x, y, p);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return imageMAtrix;
    }

    public static void CreateBlocks() {

        int[][] Image = readImage("marcelo.JPG");
        width = height = Image.length;

        for (int i = 0; i < width; i += blockW) {
            for (int j = 0; j < height; j += blockH) {
                ArrayList arr = new ArrayList();
                for (int x = i; x < i + blockW; x++) {
                    for (int y = j; y < j + blockH; y++)
                        arr.add(Image[x][y]); //add elements in block
                }
                Blocks.add(arr); //add block in blocks --> blocks is arrayList of blocks and each block have elements
            }
        }
    }

    public static ArrayList<Float> getAverage(ArrayList<ArrayList<Integer>> data){
        ArrayList<Float> ave = new ArrayList();

        for(int i = 0 ; i < vectorSize ; i++){
            float sum = 0;
            float average = 0;
            for (int j = 0 ; j < data.size() ; j++) {
                int num = data.get(j).get(i);
                sum += num;
            }
            average = sum/data.size();
            ave.add(average);
        }
        return ave;
    }

    static ArrayList<ArrayList<Integer>> splite(ArrayList<Float> ave){
        ArrayList<ArrayList<Integer>> arr = new ArrayList<>();

        ArrayList<Integer> decArr = new ArrayList<>();
        ArrayList<Integer> incArr = new ArrayList<>();
        float number;
        int value;
        for(int i = 0 ; i < ave.size() ; i++){
            //decrement
            if((ave.get(i)%1)!=0) {
                number = ave.get(i);
                value = (int) number;
                decArr.add(value);
            }
            else {
                number = ave.get(i);
                number--;
                decArr.add((int) number);
            }
        }
        arr.add(decArr);

        for(int i = 0 ; i < ave.size() ; i++){
            //increment
            if((ave.get(i)%1)!=0) {
                number = ave.get(i);
                value = (int) number + 1;
                incArr.add(value);
            }
            else {
                number = ave.get(i);
                number++;
                incArr.add((int) number);
            }
        }
        arr.add(incArr);
        return arr;
    }

   static ArrayList<ArrayList<ArrayList<Integer>>> nearestVector (ArrayList<ArrayList<Integer>> data){
       //nearestVector // ex lw data( ArrayList Average) size = 2
       // na hana ha create 2 array List ha7ot fehm el nearest
        ArrayList<ArrayList<ArrayList<Integer>>> nearest = new ArrayList();
        for(int i = 0 ; i < data.size() ; i++){
            ArrayList<ArrayList<Integer>> arr = new ArrayList<>();
            nearest.add(arr);
        }
        for(int i = 0 ; i < Blocks.size() ; i++){
            int index = getNearest(data,Blocks.get(i));

            nearest.get(index).add(Blocks.get(i));
        }
        return nearest;
    }

    //get the index of the nearest distance
    static Integer getNearest(ArrayList<ArrayList<Integer>> data,ArrayList<Integer> block){
        ArrayList<Integer> distance = new ArrayList<>();
        for (int i = 0 ; i < data.size() ; i++){
            distance.add(calculateDistance(data.get(i),block));
        }

        int minDis = distance.get(0) , min = 0;;
        for(int i = 0 ; i < distance.size() ; i++) {
            if(distance.get(i) < minDis) {
                minDis = distance.get(i);
                min = i;
            }
        }
        return min;
    }

    private static Integer calculateDistance(ArrayList<Integer> integers, ArrayList<Integer> block) {
        int sum = 0;
        for(int i = 0 ; i < vectorSize ; i++){
            sum+=Math.pow((integers.get(i) - block.get(i)),2);
        }

        return sum;
    }

    public static void quantization (){
        ArrayList<ArrayList<ArrayList<Integer>>> nearVector = new ArrayList<>();
        ArrayList<ArrayList<Float>> ave = new ArrayList<>();
        ArrayList<ArrayList<Integer>> aveAfterConvert = new ArrayList<>();
        ArrayList<ArrayList<Integer>> splits = new ArrayList<>();
        boolean action = false;

        for(int i = 0 ; i < 3 ; i++){
            if (i == 0){
                ArrayList<Float> f = getAverage(Blocks);
                ArrayList<ArrayList<Integer>> listsSplit = splite(f);
                nearVector =  nearestVector(listsSplit);

            }
            else{
                for(int j = 0 ; j < nearVector.size() ; j ++){
                    ave.add(j,getAverage(nearVector.get(j)));
                }

                if(ave.size()== vectorSize){
                    aveAfterConvert = convert(ave);
                    ave.clear();
                    break;
                }

                for (int l = 0; l < ave.size(); l++) {
                    ArrayList<ArrayList<Integer>> tmp = splite(ave.get(l));
                     for (int n = 0; n < tmp.size(); n++) {
                         splits.add(tmp.get(n));
                     }
                }
                nearVector = nearestVector(splits);
                ave.clear();
            }
        }
        while(action == false) {
            ArrayList<ArrayList<Integer>> temp = aveAfterConvert;
            nearVector = nearestVector(temp);

            for (int j = 0; j < nearVector.size(); j++) {
                ave.add(j, getAverage(nearVector.get(j)));
            }
            aveAfterConvert = convert(ave);
            action = equal(aveAfterConvert, temp);
            if (action == true) {
                CodeBook = temp;
                lastNearestVector = nearVector;
                System.out.println("End");
                return;
            }
            else {
                temp = aveAfterConvert;
                ave.clear();
            }
        }

    }

    static ArrayList<ArrayList<Integer>> convert (ArrayList<ArrayList<Float>>ave) {
        ArrayList<ArrayList<Integer>> arr = new ArrayList<>();
        for (int i = 0 ; i < ave.size() ; i++) {
            ArrayList<Integer> decArr = new ArrayList<>();
            float number;
            int value;
            for (int j = 0; j < ave.get(i).size(); j++) {
                //decrement
                if ((ave.get(i).get(j) % 1) != 0) {
                    number = ave.get(i).get(j);
                    value = (int) number;
                    decArr.add(value);
                }
            }
            arr.add(decArr);
        }
        return arr;
    }
    static  boolean equal (ArrayList<ArrayList<Integer>>aveAfterConvert,ArrayList<ArrayList<Integer>>temp){

       for(int i = 0 ; i < temp.size() ; i++){
           boolean flag = aveAfterConvert.get(i).equals(temp.get(i));
           if(flag == true){
               continue;
           }
           else {
               return false;
           }
       }
       return true;
    }
    static void printData() throws FileNotFoundException {
        PrintStream file = new PrintStream("./out.txt");
        System.setOut(file);

        System.out.println("CodeBook");
        for (int i = 0 ; i < CodeBook.size() ; i++){
            for (int j = 0 ; j < CodeBook.get(i).size() ; j++){
                System.out.print(CodeBook.get(i).get(j) + " ");
            }
            System.out.println();
        }

        System.out.println("Compressed codes");
        for (int el : image)
            System.out.println(el);
    }

    static void compressImage() throws FileNotFoundException {
        System.out.println(vectorSize);
        System.out.println(blockH);
        System.out.println(blockW);
        System.out.println(width);
        System.out.println(height);
        System.out.println(Blocks.size());

        int index;

        for(int i = 0 ; i < Blocks.size() ; i++){
            index = search(Blocks.get(i));
            image.add(index);
        }

        System.out.println("End compress operation");

        printData();
    }

    private static int search(ArrayList<Integer> integers) {
        for (int i = 0 ; i < lastNearestVector.size() ; i++){
            for (int j = 0 ; j < lastNearestVector.get(i).size() ; j++){
               if(lastNearestVector.get(i).get(j).equals(integers)){
                   return i;
               }
            }
        }
        return -1;
    }


    public static void CreateImage() throws IOException {

         bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int count = 0;
        int pos = 0;
        int [][] PixelArray =new int[width][height];
        for (int i = 0; i < width; i += blockW) {
            for (int j = 0; j < height; j += blockH) {
                int a = image.get(count);
                for (int x = i; x < i + blockW; x++) {
                    for (int y = j; y < j + blockH; y++) {
                        bufferedImage.setRGB(y, x,(CodeBook.get(a).get(pos)<<16)|(CodeBook.get(a).get(pos)<<8)|CodeBook.get(a).get(pos) );
                        pos++;
                    }
                }
                count++;
                pos = 0;
            }
        }

        ImageIO.write(bufferedImage, "JPEG", new File("decompress.jpg"));
    }
    public static void Reconstructed () throws FileNotFoundException {
        PrintStream file = new PrintStream("./Reconstructed.txt");
        System.setOut(file);
        width = bufferedImage.getWidth();
        height= bufferedImage.getHeight();
        int[][] imageMAtrix = new int[width][height];
        for(int i = 0 ; i < width ; i++)
        {
            for(int j = 0 ; j < height ; j++)
            {
                int rgb=bufferedImage.getRGB(i, j);
                int alpha=(rgb >> 24) & 0xff;
                int r =   (rgb >> 16) & 0xff;
                int g =   (rgb >> 8) & 0xff;
                int b =   (rgb >> 0) & 0xff;

                imageMAtrix[j][i]=r;
            }
        }
        for (int i = 0; i < imageMAtrix.length; i++)

            // Loop through all elements of current row
            for (int j = 0; j < imageMAtrix[i].length; j++)
                System.out.print(imageMAtrix[i][j] + " ");

    }

}



