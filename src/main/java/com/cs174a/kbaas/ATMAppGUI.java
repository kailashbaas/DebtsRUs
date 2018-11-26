package com.cs174a.kbaas;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ATMAppGUI
{
    protected JFrame frame;

    public static void main(String[] args)
    {
        ATMAppGUI gui = new ATMAppGUI();
        gui.run();
    }

    // On listener for login button, verify customer, and if
    // verified, launch new class for actual ui
    public void run()
    {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
    }
}