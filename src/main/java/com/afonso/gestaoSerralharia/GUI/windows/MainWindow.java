package com.afonso.gestaoSerralharia.GUI.windows;

import javax.swing.*;

public class MainWindow extends JFrame {
    public MainWindow() {
        setSize(800,500);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void init() {
        setVisible(true);
    }
}
