import javafx.scene.control.skin.ToolBarSkin;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;

class GUI
{
    GUI() throws FileNotFoundException {
        VectorQuantization vq = new VectorQuantization();


        JFrame f= new JFrame("Vector Quantization");




        //buttons
        JButton b1=new JButton("Compress");
        b1.setBounds(5,10,100,30);
        JButton b2=new JButton("Decompress");
        b2.setBounds(5,120,100,30);





        //compress
        b1.addActionListener(e -> {

            vq.CreateBlocks();
            vq.quantization();
            try {
                vq.compressImage();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        });


        //decompress
        b2.addActionListener(e -> {
            try {
                vq.CreateImage();
                vq.Reconstructed();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });










        f.add(b1);         f.add(b2);

        f.setSize(400,500);
        f.setResizable(false);
        f.setLayout(null);
        f.setVisible(true);
    }
}