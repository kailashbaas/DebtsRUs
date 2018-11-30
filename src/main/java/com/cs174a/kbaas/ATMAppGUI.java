package com.cs174a.kbaas;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

// TODO: check action listeners for 1 == 1 in if statement
public class ATMAppGUI {

    private JFrame frame;
    private DatabaseAccessor db;
    private Account acct;
    private Customer customer;

    public static void main(String[] args) {
        ATMAppGUI gui = new ATMAppGUI();
        gui.run_login();
    }

    public void run_login() {
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
                if (!validate_credentials(pin_entry.getPassword())) {
                    // display error msg
                    JOptionPane.showMessageDialog(frame, "Please enter your PIN");
                } else {
                    int hashed_pin = (new String(pin_entry.getPassword())).hashCode();
                    if (1 == 1 || ATMAppGUI.this.db.verifyPin(hashed_pin)) {
                        //String query = "SELECT * FROM Customer WHERE pin = " + String.valueOf(hashed_pin);
                        //Customer c = ATMAppGUI.this.db.query_customer(query, "pin").get(hashed_pin);
                        ATMAppGUI.this.customer = new Customer(1, 1, "a", "b"); // remove this and uncomment after db setup
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
        String query = "SELECT * FROM Accounts NATURAL JOIN Owners WHERE ownerid = " + String.valueOf(customer.getTaxId());
        final HashMap<Integer, Account> accounts = new HashMap<Integer, Account>();//db.query_acct(query);
        Account acct1 = new CheckingAccount();
        accounts.put(0, acct1);

        for (int i = 0; i < 10; i++) {
            accountids.add(i);
        }
        final JComboBox accountid_list = new JComboBox(accountids.toArray());

        JButton button = new JButton("Continue");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Integer accountid = (Integer) accountid_list.getSelectedItem();
                ATMAppGUI.this.acct = accounts.get(accountid);
                if (1 != 1 && ATMAppGUI.this.acct.getType().equals("Pocket")) {
                    ATMAppGUI.this.run_pocket_app();
                } else {
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

    private void run_main_app() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // TODO: in button handlers, check that account is open
        TransactionButtonListener listener = new TransactionButtonListener();
        String greeting = "Welcome, " + customer.getName();
        JLabel greeting_label = new JLabel(greeting);
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

        JLabel label = new JLabel("Please enter the amount you wish to deposit:");
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
                boolean result = t.deposit(deposit_amount, ATMAppGUI.this.acct, ATMAppGUI.this.customer);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your deposit");
                }
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

        c.gridx = 2;
        panel.add(back_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.validate();
    }

    private void run_top_up_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Please enter the amount you wish to add to this account:");
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
                boolean result = t.top_up(deposit_amount, ATMAppGUI.this.acct, ATMAppGUI.this.customer);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your top-up");
                }
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

        c.gridx = 2;
        panel.add(back_button, c);

        frame.getContentPane().add(panel);
        frame.validate();
    }

    private void run_withdrawal_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Please enter the amount you wish to withdraw:");
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
                boolean result = t.withdraw(withdrawal_amount, ATMAppGUI.this.acct, ATMAppGUI.this.customer);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your withdrawal");
                }
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

        c.gridx = 2;
        panel.add(back_button, c);

        frame.getContentPane().add(panel);
        frame.validate();
    }

    private void run_purchase_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Please enter the amount you wish to spend:");
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
                boolean result = t.purchase(purchase_amount, ATMAppGUI.this.acct, ATMAppGUI.this.customer);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your purchase");
                }
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

        c.gridx = 2;
        panel.add(back_button, c);

        frame.getContentPane().add(panel);
        frame.validate();
    }

    private void run_transfer_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Please enter the amount you wish to transfer:");
        final JTextField amount = new JTextField(20);
        JLabel dest_label = new JLabel("Please enter the accountid of the account you wish to transfer to:");
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
                boolean result = t.transfer(transfer_amount, ATMAppGUI.this.acct, dest, ATMAppGUI.this.customer);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your transfer");
                }
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
        panel.add(back_button, c);

        frame.getContentPane().add(panel);
        frame.validate();
    }

    private void run_collect_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Please enter the amount you wish to collect:");
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
                boolean result = t.collect(purchase_amount, ATMAppGUI.this.acct, ATMAppGUI.this.customer);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your collect");
                }
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

        c.gridx = 2;
        panel.add(back_button, c);

        frame.getContentPane().add(panel);
        frame.validate();
    }

    private void run_wire() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Please enter the amount you wish to wire:");
        final JTextField amount = new JTextField(20);
        JLabel dest_label = new JLabel("Please enter the accountid of the account you wish to wire:");
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
                boolean result = t.wire(transfer_amount, ATMAppGUI.this.acct, dest, ATMAppGUI.this.customer);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your wire");
                }
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
        panel.add(back_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.validate();
    }

    private void run_pay_friend() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel("Please enter the amount you wish to pay you friend:");
        final JTextField amount = new JTextField(20);
        JLabel dest_label = new JLabel("Please enter the accountid of the pocket account you wish to pay:");
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
                boolean result = t.pay_friend(transfer_amount, ATMAppGUI.this.acct, dest, ATMAppGUI.this.customer);
                if (!result) {
                    JOptionPane.showMessageDialog(frame, "There was an error processing your pay-friend request");
                }
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
        panel.add(back_button, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.validate();
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