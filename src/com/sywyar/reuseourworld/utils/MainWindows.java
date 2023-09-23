package com.sywyar.reuseourworld.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainWindows extends JFrame {
    public JPanel background;
    private MainWindows(){
    }
    public MainWindows(int windowWidth,int windowHigh){
        this.setSize(windowWidth,windowHigh);
        this.setLocationRelativeTo(null);//窗口居中
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);//设置窗口关闭键
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.setResizable(false);//窗口大小不可变
        this.setLayout(null);
        background = new JPanel();
        background.setSize(windowWidth,windowHigh);
        background.setBackground(Color.WHITE);
        background.setLayout(null);
        this.add(background);
    }
}
