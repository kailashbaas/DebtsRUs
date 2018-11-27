package com.cs174a.kbaas;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ATMAppLogin
{
    private JFrame frame;

    public static void main(String[] args)
    {
        ATMAppLogin gui = new ATMAppLogin();
        gui.run();
    }

    // On listener for login button, verify customer, and if
    // verified, launch new class for actual ui
    public void run()
    {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel title = new JLabel("DebtsRUs ATM App Login", SwingConstants.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JPanel id_panel = new JPanel();
        JPanel pin_panel = new JPanel();

        JLabel tax_id_label = new JLabel("Tax ID:");
        final JTextField tax_id_entry = new JTextField(20);
        tax_id_label.setLabelFor(tax_id_entry);

        JLabel pin_label = new JLabel("PIN:");
        final JPasswordField pin_entry = new JPasswordField(20);
        pin_label.setLabelFor(pin_entry);

        JButton login_button = new JButton("Login");
        login_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (!validate_credentials(tax_id_entry.getText(), pin_entry.getPassword()))
                {
                    // display error msg
                    JOptionPane.showMessageDialog(frame, "Please enter your tax ID and PIN");
                }
                else
                {
                    int tax_id = Integer.parseInt(tax_id_entry.getText());
                    int hashed_pin = (new String(pin_entry.getPassword())).hashCode();
                    DatabaseAccessor db = new DatabaseAccessor();
                    if (db.verifyPin(tax_id, hashed_pin))
                    {
                        ATMAppGUI gui = new ATMAppGUI();
                        gui.run();
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(frame, "Incorrect Tax ID/PIN");
                    }
                }
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(title, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(tax_id_label, c);

        c.gridx = 1;
        panel.add(tax_id_entry, c);

        c.gridx = 0;
        c.gridy = 2;
        panel.add(pin_label, c);

        c.gridx = 1;
        panel.add(pin_entry, c);

        c.gridx = 1;
        c.gridy = 3;
        panel.add(login_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setTitle("DebtsRUs ATM App Login");
        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    private boolean validate_credentials(String tax_id, char[] pin)
    {
        for (int i = 0; i < pin.length; i++)
        {
            if (!Character.isDigit(pin[i]))
            {
                return false;
            }
        }
        return !(tax_id.equals("") || pin.length != 4 || tax_id.matches("[0-9]+"));
    }
}