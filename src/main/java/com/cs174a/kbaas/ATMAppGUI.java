package com.cs174a.kbaas;

import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ATMAppGUI {

    private JFrame frame;
    private DatabaseAccessor db;
    private Account acct;
    private Customer customer;
    private CurrentTimeWrapper time;
    private Timestamp start;

    public static void main(String[] args) {
        ATMAppGUI gui = new ATMAppGUI();
        gui.run_login();
    }

    public void run_login() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        db = new DatabaseAccessor();
        time = new CurrentTimeWrapper();
        start = new Timestamp(System.currentTimeMillis());

        JLabel title = new JLabel("DebtsRUs ATM App Login", SwingConstants.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel pin_label = new JLabel("PIN:");
        final JPasswordField pin_entry = new JPasswordField(20);
        pin_label.setLabelFor(pin_entry);

        JButton login_button = new JButton("Login");
        login_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (!validate_credentials(pin_entry.getPassword())) {
                    // display error msg
                    JOptionPane.showMessageDialog(frame, "Please enter your PIN");
                } else {
                    int hashed_pin = (new String(pin_entry.getPassword())).hashCode();
                    if (ATMAppGUI.this.db.verifyPin(hashed_pin)) {
                        String query = "SELECT * FROM Customers WHERE pin = " + String.valueOf(hashed_pin);
                        ATMAppGUI.this.customer = ATMAppGUI.this.db.query_customer(query, "pin").get(hashed_pin);
                        ATMAppGUI.this.run_atm_app();
                    } else {
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

    private void run_atm_app() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Select an account");

        ArrayList<Integer> accountids = new ArrayList<Integer>();
        String query = "SELECT * FROM Accounts A NATURAL JOIN Owners O WHERE ownerid = " + String.valueOf(customer.getTaxId());
        final HashMap<Integer, Account> accounts = db.query_acct(query);

        Iterator it = accounts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Account acct = (Account) pair.getValue();
            accountids.add(acct.getAccountid());
        }

        final JComboBox accountid_list = new JComboBox(accountids.toArray());

        JButton button = new JButton("Continue");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Integer accountid = (Integer) accountid_list.getSelectedItem();
                ATMAppGUI.this.acct = accounts.get(accountid);
                if (ATMAppGUI.this.acct.getType().contains("Pocket")) {
                    ATMAppGUI.this.run_pocket_app();
                } else {
                    ATMAppGUI.this.run_main_app();
                }
            }
        });

        JButton change_pin = new JButton("Change PIN");
        change_pin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                frame.dispose();
                ATMAppGUI.this.run_change_pin();
            }
        });

        JButton return_to_login = new JButton("Return to login");
        return_to_login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ATMAppGUI.this.run_login();
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

        JPanel button_panel = new JPanel();
        button_panel.setLayout(new FlowLayout());
        button_panel.add(change_pin);
        button_panel.add(return_to_login);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(button_panel, BorderLayout.SOUTH);
        frame.validate();
    }

    private void run_main_app() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        TransactionButtonListener listener = new TransactionButtonListener();
        String greeting = "Welcome, " + customer.getName();
        JLabel greeting_label = new JLabel(greeting);
        String acct_msg = "Account " + String.valueOf(acct.getAccountid());
        JLabel acct_label = new JLabel(acct_msg);
        String balance = "Balance: " + String.valueOf(acct.getBalance());
        JLabel balance_label = new JLabel(balance);
        JButton deposit = new JButton("Deposit");
        deposit.addActionListener(listener);
        JButton withdraw = new JButton("Withdraw");
        withdraw.addActionListener(listener);
        JButton transfer = new JButton("Transfer");
        transfer.addActionListener(listener);
        JButton wire = new JButton("Wire");
        wire.addActionListener(listener);
        JButton back = new JButton("Back");
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ATMAppGUI.this.run_atm_app();
            }
        });

        panel.add(greeting_label);
        panel.add(acct_label);
        panel.add(balance_label);
        panel.add(deposit);
        panel.add(withdraw);
        panel.add(transfer);
        panel.add(wire);
        panel.add(back);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.validate();
    }

    private void run_pocket_app() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String greeting = "Welcome, " + customer.getName();
        JLabel greeting_label = new JLabel(greeting);
        String acct_msg = "Account " + String.valueOf(acct.getAccountid());
        JLabel acct_label = new JLabel(acct_msg);
        String balance = "Balance: " + String.valueOf(acct.getBalance());
        JLabel balance_label = new JLabel(balance);

        TransactionButtonListener listener = new TransactionButtonListener();
        JButton top_up = new JButton("Top-Up");
        top_up.addActionListener(listener);
        JButton purchase = new JButton("Purchase");
        purchase.addActionListener(listener);
        JButton collect = new JButton("Collect");
        collect.addActionListener(listener);
        JButton pay_friend = new JButton("Pay-Friend");
        pay_friend.addActionListener(listener);
        JButton back = new JButton("Back");
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ATMAppGUI.this.run_atm_app();
            }
        });

        panel.add(greeting_label);
        panel.add(acct_label);
        panel.add(balance_label);
        panel.add(top_up);
        panel.add(purchase);
        panel.add(collect);
        panel.add(pay_friend);
        panel.add(back);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.validate();

    }

    private void run_deposit_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Deposit Amount:");
        final JTextField amount = new JTextField(20);
        JButton submit_button = new JButton("Submit");
        submit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!ATMAppGUI.this.acct.isOpen() || !validate_amount(amount.getText())) {
                    return;
                }
                double deposit_amount = Double.parseDouble(amount.getText());
                System.out.println("deposit amount" + deposit_amount);
                TransactionHandler t = new TransactionHandler();
                Timestamp delta = new Timestamp(System.currentTimeMillis() - start.getTime());
                Timestamp transac_time = new Timestamp(time.getCurrent_time().getTime() + delta.getTime());
                boolean result = t.deposit(deposit_amount, ATMAppGUI.this.acct, ATMAppGUI.this.customer, transac_time);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your deposit");
                }
                ATMAppGUI.this.run_main_app();
            }
        });

        JButton back_button = new JButton("Back");
        TransactionBackButtonListener back_listener = new TransactionBackButtonListener();
        back_button.addActionListener(back_listener);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(label, c);

        c.gridy = 1;
        panel.add(amount, c);

        c.gridy = 2;
        panel.add(submit_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(back_button, BorderLayout.SOUTH);
        frame.validate();
    }

    private void run_top_up_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Top-Up Amount:");
        final JTextField amount = new JTextField(20);
        JButton submit_button = new JButton("Submit");
        submit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!ATMAppGUI.this.acct.isOpen() || !validate_amount(amount.getText())) {
                    return;
                }
                double deposit_amount = Double.parseDouble(amount.getText());
                TransactionHandler t = new TransactionHandler();
                Timestamp delta = new Timestamp(System.currentTimeMillis() - start.getTime());
                Timestamp transac_time = new Timestamp(time.getCurrent_time().getTime() + delta.getTime());
                boolean result = t.top_up(deposit_amount, ATMAppGUI.this.acct, ATMAppGUI.this.customer, transac_time);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your top-up");
                }
                ATMAppGUI.this.run_pocket_app();
            }
        });

        JButton back_button = new JButton("Back");
        TransactionBackButtonListener back_listener = new TransactionBackButtonListener();
        back_button.addActionListener(back_listener);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(label, c);

        c.gridy = 1;
        panel.add(amount, c);

        c.gridy = 2;
        panel.add(submit_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(back_button, BorderLayout.SOUTH);
        frame.validate();
    }

    private void run_withdrawal_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Withdrawal Amount:");
        final JTextField amount = new JTextField(20);
        JButton submit_button = new JButton("Submit");
        submit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!ATMAppGUI.this.acct.isOpen() || !validate_amount(amount.getText())) {
                    return;
                }
                double withdrawal_amount = Double.parseDouble(amount.getText());
                TransactionHandler t = new TransactionHandler();
                Timestamp delta = new Timestamp(System.currentTimeMillis() - start.getTime());
                Timestamp transac_time = new Timestamp(time.getCurrent_time().getTime() + delta.getTime());
                System.out.println("here");
                boolean result = t.withdraw(withdrawal_amount, ATMAppGUI.this.acct, ATMAppGUI.this.customer, transac_time);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your withdrawal");
                }
                ATMAppGUI.this.run_main_app();
            }
        });

        JButton back_button = new JButton("Back");
        TransactionBackButtonListener back_listener = new TransactionBackButtonListener();
        back_button.addActionListener(back_listener);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(label, c);

        c.gridy = 1;
        panel.add(amount, c);

        c.gridy = 2;
        panel.add(submit_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(back_button, BorderLayout.SOUTH);
        frame.validate();
    }

    private void run_purchase_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Spend Amount");
        final JTextField amount = new JTextField(20);
        JButton submit_button = new JButton("Submit");
        submit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!ATMAppGUI.this.acct.isOpen() || !validate_amount(amount.getText())) {
                    return;
                }
                double purchase_amount = Double.parseDouble(amount.getText());
                TransactionHandler t = new TransactionHandler();
                Timestamp delta = new Timestamp(System.currentTimeMillis() - start.getTime());
                Timestamp transac_time = new Timestamp(time.getCurrent_time().getTime() + delta.getTime());
                boolean result = t.purchase(purchase_amount, ATMAppGUI.this.acct, ATMAppGUI.this.customer, transac_time);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your purchase");
                }
                ATMAppGUI.this.run_pocket_app();
            }
        });

        JButton back_button = new JButton("Back");
        TransactionBackButtonListener back_listener = new TransactionBackButtonListener();
        back_button.addActionListener(back_listener);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(label, c);

        c.gridy = 1;
        panel.add(amount, c);

        c.gridy = 2;
        panel.add(submit_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(back_button, BorderLayout.SOUTH);
        frame.validate();
    }

    private void run_transfer_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Transfer Amount:");
        final JTextField amount = new JTextField(20);
        JLabel dest_label = new JLabel("Destination Accountid:");
        final JTextField  dest_account = new JTextField(20);
        JButton submit_button = new JButton("Submit");
        submit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!ATMAppGUI.this.acct.isOpen() || !validate_amount(amount.getText()) || !dest_account.getText().matches("[0-9]+")) {
                    return;
                }
                String sql = "SELECT * FROM Accounts WHERE accountid = " + dest_account.getText();
                Account dest = ATMAppGUI.this.db.query_acct(sql).get(Integer.parseInt(dest_account.getText()));
                double transfer_amount = Double.parseDouble(amount.getText());
                TransactionHandler t = new TransactionHandler();
                Timestamp delta = new Timestamp(System.currentTimeMillis() - start.getTime());
                Timestamp transac_time = new Timestamp(time.getCurrent_time().getTime() + delta.getTime());
                boolean result = t.transfer(transfer_amount, ATMAppGUI.this.acct, dest, ATMAppGUI.this.customer, transac_time);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your transfer");
                }
                ATMAppGUI.this.run_main_app();
            }
        });

        JButton back_button = new JButton("Back");
        TransactionBackButtonListener back_listener = new TransactionBackButtonListener();
        back_button.addActionListener(back_listener);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(label, c);

        c.gridx = 1;
        panel.add(amount, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(dest_label, c);

        c.gridx = 1;
        panel.add(dest_account, c);

        c.gridy = 2;
        panel.add(submit_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(back_button, BorderLayout.SOUTH);
        frame.validate();
    }

    private void run_collect_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Collect Amount:");
        final JTextField amount = new JTextField(20);
        JButton submit_button = new JButton("Submit");
        submit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!ATMAppGUI.this.acct.isOpen() || !validate_amount(amount.getText())) {
                    return;
                }
                double purchase_amount = Double.parseDouble(amount.getText());
                TransactionHandler t = new TransactionHandler();
                Timestamp delta = new Timestamp(System.currentTimeMillis() - start.getTime());
                Timestamp transac_time = new Timestamp(time.getCurrent_time().getTime() + delta.getTime());
                boolean result = t.collect(purchase_amount, ATMAppGUI.this.acct, ATMAppGUI.this.customer, transac_time);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your collect");
                }
                ATMAppGUI.this.run_pocket_app();
            }
        });

        JButton back_button = new JButton("Back");
        TransactionBackButtonListener back_listener = new TransactionBackButtonListener();
        back_button.addActionListener(back_listener);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(label, c);

        c.gridy = 1;
        panel.add(amount, c);

        c.gridy = 2;
        panel.add(submit_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(back_button, BorderLayout.SOUTH);
        frame.validate();
    }

    private void run_wire() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Wire Amount:");
        final JTextField amount = new JTextField(20);
        JLabel dest_label = new JLabel("Destination Accountid:");
        final JTextField  dest_account = new JTextField(20);
        JButton submit_button = new JButton("Submit");
        submit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!ATMAppGUI.this.acct.isOpen() || !validate_amount(amount.getText()) || !dest_account.getText().matches("[0-9]+")) {
                    return;
                }
                String sql = "SELECT * FROM Accounts WHERE accountid = " + dest_account.getText();
                Account dest = ATMAppGUI.this.db.query_acct(sql).get(Integer.parseInt(dest_account.getText()));
                double transfer_amount = Double.parseDouble(amount.getText());
                TransactionHandler t = new TransactionHandler();
                Timestamp delta = new Timestamp(System.currentTimeMillis() - start.getTime());
                Timestamp transac_time = new Timestamp(time.getCurrent_time().getTime() + delta.getTime());
                boolean result = t.wire(transfer_amount, ATMAppGUI.this.acct, dest, ATMAppGUI.this.customer, transac_time);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your wire");
                }
                ATMAppGUI.this.run_main_app();
            }
        });

        JButton back_button = new JButton("Back");
        TransactionBackButtonListener back_listener = new TransactionBackButtonListener();
        back_button.addActionListener(back_listener);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(label, c);

        c.gridx = 1;
        panel.add(amount, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(dest_label, c);

        c.gridx = 1;
        panel.add(dest_account, c);

        c.gridy = 2;
        panel.add(submit_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(back_button, BorderLayout.SOUTH);
        frame.validate();
    }

    private void run_pay_friend() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Amount:");
        final JTextField amount = new JTextField(20);
        JLabel dest_label = new JLabel("Destination Accountid:");
        final JTextField  dest_account = new JTextField(20);
        JButton submit_button = new JButton("Submit");
        submit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!ATMAppGUI.this.acct.isOpen() || !validate_amount(amount.getText()) || !dest_account.getText().matches("[0-9]+")) {
                    return;
                }
                String sql = "SELECT * FROM Accounts WHERE accountid = " + dest_account.getText();
                Account dest = ATMAppGUI.this.db.query_acct(sql).get(Integer.parseInt(dest_account.getText()));
                double transfer_amount = Double.parseDouble(amount.getText());
                TransactionHandler t = new TransactionHandler();
                Timestamp delta = new Timestamp(System.currentTimeMillis() - start.getTime());
                Timestamp transac_time = new Timestamp(time.getCurrent_time().getTime() + delta.getTime());
                boolean result = t.pay_friend(transfer_amount, ATMAppGUI.this.acct, dest, ATMAppGUI.this.customer, transac_time);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your pay-friend request");
                }
                ATMAppGUI.this.run_pocket_app();
            }
        });

        JButton back_button = new JButton("Back");
        TransactionBackButtonListener back_listener = new TransactionBackButtonListener();
        back_button.addActionListener(back_listener);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(label, c);

        c.gridx = 1;
        panel.add(amount, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(dest_label, c);

        c.gridx = 1;
        panel.add(dest_account, c);

        c.gridy = 2;
        panel.add(submit_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(back_button, BorderLayout.SOUTH);
        frame.validate();
    }

    private void run_change_pin() {
        JFrame frame1 = new JFrame();
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel1 = new JPanel(new GridBagLayout());

        JLabel old_pin_label = new JLabel("Old PIN");
        final JPasswordField old_pin_entry = new JPasswordField(20);

        JLabel new_pin_label = new JLabel("New PIN");
        final JPasswordField new_pin_entry = new JPasswordField(20);

        JButton change_pin = new JButton("Change PIN");
        change_pin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (!validate_credentials(old_pin_entry.getPassword())) {
                    // display error msg
                    JOptionPane.showMessageDialog(frame, "Please enter your old PIN");
                } else {
                    int hashed_pin = (new String(old_pin_entry.getPassword())).hashCode();
                    int hashed_new_pin = (new String(new_pin_entry.getPassword()).hashCode());
                    if (ATMAppGUI.this.db.verifyPin(hashed_pin)) {
                        String query = "SELECT * FROM Customers WHERE pin = " + String.valueOf(hashed_pin);
                        Customer c = ATMAppGUI.this.db.query_customer(query, "pin").get(hashed_pin);
                        c.setPin(hashed_new_pin);
                        System.out.println("new pin" + c.getPin());
                        ATMAppGUI.this.db.update_customer(c);
                        JOptionPane.showMessageDialog(null, "Updated PIN");
                    } else {
                        JOptionPane.showMessageDialog(null, "Incorrect PIN");
                    }
                }
                frame1.dispose();
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        panel1.add(old_pin_label, c);

        c.gridx = 1;
        panel1.add(old_pin_entry, c);

        c.gridx = 0;
        c.gridy = 1;
        panel1.add(new_pin_label, c);

        c.gridx = 1;
        panel1.add(new_pin_entry, c);

        c.gridy = 2;
        panel1.add(change_pin, c);

        frame1.getContentPane().add(panel1);
        frame1.setSize(400, 400);
        frame1.setVisible(true);
    }

    private boolean validate_credentials(char[] pin) {
        if (pin.length != 4) {
            return false;
        }
        for (int i = 0; i < pin.length; i++) {
            if (!Character.isDigit(pin[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean validate_amount(String amount) {
        return amount.matches("[0-9]+(.[0-9]+)?");
    }

    private class TransactionButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            String transaction = actionEvent.getActionCommand();
            switch (transaction) {
                case "Deposit":
                    ATMAppGUI.this.run_deposit_screen();
                    break;

                case "Top-Up":
                    ATMAppGUI.this.run_top_up_screen();
                    break;

                case "Withdraw":
                    ATMAppGUI.this.run_withdrawal_screen();
                    break;

                case "Purchase":
                    ATMAppGUI.this.run_purchase_screen();
                    break;

                case "Transfer":
                    ATMAppGUI.this.run_transfer_screen();
                    break;

                case "Collect":
                    ATMAppGUI.this.run_collect_screen();
                    break;

                case "Wire":
                    ATMAppGUI.this.run_wire();
                    break;

                case "Pay-Friend":
                    ATMAppGUI.this.run_pay_friend();
                    break;

                default:
                    System.out.println("Something is really wrong");
                    break;

            }
        }
    }

    private class TransactionBackButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ATMAppGUI.this.run_main_app();
        }
    }
}
