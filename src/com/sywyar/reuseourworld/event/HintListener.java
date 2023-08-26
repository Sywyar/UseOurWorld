package com.sywyar.reuseourworld.event;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class HintListener implements FocusListener {
    private final String hintText;
    private final JTextField text;

    public HintListener(JTextField jText, String hintText) {
        this.text = jText;
        this.hintText = hintText;
        jText.setText(hintText);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(text.getText().equals(hintText)) {
            text.setText("");
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if(text.getText().equals("")) {
            text.setText(hintText);
        }
    }
}
