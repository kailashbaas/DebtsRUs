package com.cs174a.kbaas;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ATMAppGUI
{
    private JFrame frame;

    private DatabaseAccessor db;

    private Account acct;
    private Customer customer;

    public static void main(String[] args)
    {
        ATMAppGUI gui = new ATMAppGUI();
        gui.run_login();
    }

    public void run_login()
    {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        db = new DatabaseAccessor();

        JLabel title = new JLabel("DebtsRUs ATM App Login", SwingConstants.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel pin_label = new JLabel("PIN:");
        final JPasswordField pin_entry = new JPasswordField(20);
        pin_label.setLabelFor(pin_entry);

        JButton login_button = new JButton("Login");
        login_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (!validate_credentials(pin_entry.getPassword()))
                {
                    // display error msg
                    JOptionPane.showMessageDialog(frame, "Please enter your PIN");
                }
                else
                {
                    int hashed_pin = (new String(pin_entry.getPassword())).hashCode();
                    if (1 == 1 || ATMAppGUI.this.db.verifyPin(hashed_pin))
                    {
                        //String query = "SELECT * FROM Customer WHERE pin = " + String.valueOf(hashed_pin);
                        //Customer c = ATMAppGUI.this.db.query_customer(query, "pin").get(hashed_pin);
                        ATMAppGUI.this.customer = new Customer(1, 1, "a", "b"); // remove this and uncomment after db setup
                        ATMAppGUI.this.run_atm_app();
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(frame, "Incorrect PIN");
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
        c.gridy = 2;
        panel.add(pin_label, c);

        c.gridx = 1;
        panel.add(pin_entry, c);

        c.gridx = 1;
        c.gridy = 3;
        panel.add(login_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setTitle("DebtsRUs ATM App");
        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    private void run_atm_app()
    {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Select an account");

        ArrayList<Integer> accountids = new ArrayList<Integer>();
        String query = "SELECT * FROM Accounts NATURAL JOIN Owners WHERE ownerid = " + String.valueOf(customer.getTaxId());
        final HashMap<Integer, Account> accounts = new HashMap<Integer, Account>();//db.query_acct(query);
        Account acct1 = new CheckingAccount();
        accounts.put(0, acct1);

        /*Iterator it = accounts.entrySet().iterator();
        while (it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            Account acct = (Account) pair.getValue();
            accountids.add(acct.getAccountid());
        }

        accountids.clear();*/
        for (int i = 0; i < 10; i++)
        {
            accountids.add(i);
        }
        final JComboBox accountid_list = new JComboBox(accountids.toArray());

        JButton button = new JButton("Continue");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Integer accountid = (Integer) accountid_list.getSelectedItem();
                ATMAppGUI.this.acct = accounts.get(accountid);
                if (1 == 1 || ATMAppGUI.this.acct.getType().equals("Pocket"))
                {
                    ATMAppGUI.this.run_pocket_app();
                }
                else
                {
                    ATMAppGUI.this.run_main_app();
                }
            }
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        panel.add(label, constraints);

        constraints.gridx = 1;
        panel.add(accountid_list, constraints);

        constraints.gridy = 2;
        panel.add(button, constraints);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.validate();
    }

    private void run_main_app()
    {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String greeting = "Welcome, " + customer.getName();
        JLabel greeting_label = new JLabel(greeting);
        JButton deposit = new JButton("Deposit Money");
        JButton withdraw = new JButton("Withdraw Money");
        JButton transfer = new JButton("Transfer Money");
        JButton wire = new JButton("Wire Money");
        JButton back = new JButton("Back");

        panel.add(greeting_label);
        panel.add(deposit);
        panel.add(withdraw);
        panel.add(transfer);
        panel.add(wire);
        panel.add(back);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.validate();
    }

    private void run_pocket_app()
    {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String greeting = "Welcome, " + customer.getName();
        JLabel greeting_label = new JLabel(greeting);
        JButton top_up = new JButton("Top-Up");
        JButton purchase = new JButton("Purchase");
        JButton collect = new JButton("Collect");
        JButton pay_friend = new JButton("Pay-Friend");
        JButton back = new JButton("Back");

        panel.add(greeting_label);
        panel.add(top_up);
        panel.add(purchase);
        panel.add(collect);
        panel.add(pay_friend);
        panel.add(back);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.validate();

    }

    private boolean validate_credentials(char[] pin)
    {
        if (pin.length != 4)
        {
            return false;
        }
        for (int i = 0; i < pin.length; i++)
        {
            if (!Character.isDigit(pin[i]))
            {
                return false;
            }
        }
        return true;
    }
}