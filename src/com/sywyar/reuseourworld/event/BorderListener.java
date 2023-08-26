package com.sywyar.reuseourworld.event;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class BorderListener implements FocusListener {
    private final JTextField text;
    private final LineBorder lineBorder = new LineBorder(new Color(5, 103, 157), 2,true);
    private final Border defaultBorder;

    public BorderListener(JTextField jText){
        this.text=jText;
        this.defaultBorder=jText.getBorder();
        jText.setBorder(lineBorder);
    }

    @Override
    public void focusGained(FocusEvent e) {
        text.setBorder(defaultBorder);
    }

    @Override
    public void focusLost(FocusEvent e) {
        text.setBorder(lineBorder);
    }
}
