package com.cs174a.kbaas;

import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;

public class BankTellerGUI {
    private JFrame frame;
    private DatabaseAccessor db;
    private Timestamp time;

    public static void main(String[] args) {
        BankTellerGUI gui = new BankTellerGUI();
        gui.run();
    }

    public void run() {
        time = new Timestamp(System.currentTimeMillis());
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        db = new DatabaseAccessor();

        JLabel title = new JLabel("DebtsRUs Bank Teller Interface");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(title, BorderLayout.NORTH);

        JLabel date = new JLabel(time.toString());
        panel.add(date, BorderLayout.SOUTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        ButtonHandler listener = new ButtonHandler();
        JButton enter_check = new JButton("Enter Check Transaction");
        enter_check.addActionListener(listener);
        content.add(enter_check);

        JButton monthly_statement = new JButton("Generate Monthly Statement");
        monthly_statement.addActionListener(listener);
        content.add(monthly_statement);

        JButton closed_accts = new JButton("List Closed Accounts");
        closed_accts.addActionListener(listener);
        content.add(closed_accts);

        JButton dter = new JButton("Generate DTER");
        dter.addActionListener(listener);
        content.add(dter);

        JButton report = new JButton("Customer Report");
        report.addActionListener(listener);
        content.add(report);

        JButton add_interest = new JButton("Add Interest");
        add_interest.addActionListener(listener);
        content.add(add_interest);

        JButton create_acct = new JButton("Create Account");
        create_acct.addActionListener(listener);
        content.add(create_acct);

        JButton delete_closed_accts = new JButton("Delete Closed Accounts and Customers");
        delete_closed_accts.addActionListener(listener);
        content.add(delete_closed_accts);

        JButton delete_transactions = new JButton("Delete Transactions");
        delete_transactions.addActionListener(listener);
        content.add(delete_transactions);

        JButton change_date = new JButton("Change date");
        change_date.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFrame frame1 = new JFrame();
                frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel panel1 = new JPanel();
                panel1.setLayout(new GridBagLayout());
                JLabel date_label = new JLabel("Please enter the new date using the MM-dd-yyyy format");
                JTextField date_entry = new JTextField(11);

                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 0;
                c.anchor = GridBagConstraints.CENTER;
                panel1.add(date_label, c);

                c.gridy = 1;
                panel1.add(date_entry, c);

                JButton submit_change = new JButton("Change");
                submit_change.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
                        try {
                            Date new_date = formatter.parse(date_entry.getText());
                            time = new Timestamp(new_date.getTime());
                            frame1.dispose();
                            date.setText(time.toString());
                            frame.validate();
                        } catch (ParseException e) {
                            JOptionPane.showMessageDialog(frame1, "Please use the correct date format (MM-dd-yyyy)");
                        }
                    }
                });

                c.gridy = 2;
                panel1.add(submit_change, c);

                frame1.getContentPane().add(panel1);
                frame1.setSize(400, 400);
                frame1.setVisible(true);
            }
        });
        content.add(change_date);

        panel.add(content, BorderLayout.CENTER);
        frame.getContentPane().add(panel);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }

    private void run_check_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel title = new JLabel("Enter Check Transaction");

        JLabel select_acct_label = new JLabel("Accountid: ");
        final JTextField acct_entry = new JTextField(20);

        JLabel amt_label = new JLabel("Amount: ");
        final JTextField amt_entry = new JTextField(20);

        JLabel memo_label = new JLabel("Memo: ");
        final JTextField memo_entry = new JTextField(20);

        JButton submit = new JButton("Submit");
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (validate_acct(acct_entry.getText()) && validate_amount(amt_entry.getText())) {
                    int accountid = Integer.parseInt(acct_entry.getText());
                    double amt = Double.parseDouble(amt_entry.getText());
                    String sql = "SELECT * FROM Accounts WHERE accountid = " + String.valueOf(accountid);
                    Account acct = db.query_acct(sql).get(accountid);
                    TransactionHandler t = new TransactionHandler();
                    t.write_check(amt, acct, memo_label.getText());
                    run();
                }
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(select_acct_label, c);

        c.gridx = 1;
        panel.add(acct_entry, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(amt_label, c);

        c.gridx = 1;
        panel.add(amt_entry, c);

        c.gridx = 0;
        c.gridy = 2;
        panel.add(memo_label, c);

        c.gridx = 1;
        panel.add(memo_entry, c);

        frame.getContentPane().add(title, BorderLayout.NORTH);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(submit, BorderLayout.SOUTH);
        frame.validate();
    }

    private void run_monthly_statement_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel title = new JLabel("Generate Monthly Statement");

        JLabel select_customer_label = new JLabel("Customer tax_id: ");
        final JTextField customer_entry = new JTextField(20);

        JButton generate = new JButton("Generate");
        generate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (validate_acct(acct_entry.getText())) {
                    int tax_id = Integer.parseInt(customer_entry.getText());
                    String sql = "SELECT * FROM Owners WHERE tax_id = " + String.valueOf(tax_id);
                    ArrayList<String> accts = db.query_owners(sql);
                    TransactionHandler t = new TransactionHandler();
                    t.generate_monthly_statement(acct);
                    // TODO: display results in new window and send frame back to original
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
        panel.add(select_acct_label, c);

        c.gridx = 1;
        panel.add(acct_entry, c);

        c.gridx = 1;
        c.gridy = 2;
        panel.add(generate, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.validate();
    }

    private void run_closed_accts_screen() {
        String sql = "SELECT * FROM Accounts WHERE open = false";
        HashMap<Integer, Account> closed_accts = db.query_acct(sql);

        ArrayList<JLabel> acct_labels = new ArrayList<>();
        Iterator it = closed_accts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Account acct = (Account) pair.getValue();
            String text = String.valueOf(acct.getAccountid());
            acct_labels.add(new JLabel(text));
        }

        display_labels(acct_labels, "Closed Accounts");
    }

    private void run_dter_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        frame.validate();
    }

    private void run_customer_report_screen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JLabel title = new JLabel("Generate Customer Report");

        JLabel select_customer_label = new JLabel("Taxid: ");
        final JTextField customer_entry = new JTextField(20);

        JButton generate = new JButton("Generate");
        generate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (validate_acct(customer_entry.getText())) {
                    int tax_id = Integer.parseInt(customer_entry.getText());
                    String sql = "SELECT * FROM Accounts NATURAL JOIN Owners WHERE tax_id= " + String.valueOf(tax_id);
                    HashMap<Integer, Account> accts = db.query_acct(sql);
                    ArrayList<JLabel> acct_labels = new ArrayList<>();
                    Iterator it = accts.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        Account acct = (Account) pair.getValue();
                        String text = String.valueOf(acct.getAccountid()) + "\t" +
                                acct.getType() + "\t" + acct.isOpen();
                        acct_labels.add(new JLabel(text));
                    }
                    String heading = "Accountid\tType\tOpen";
                    display_labels(acct_labels, heading);
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
        panel.add(select_customer_label, c);

        c.gridx = 1;
        panel.add(customer_entry, c);

        c.gridx = 1;
        c.gridy = 2;
        panel.add(generate, c);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.validate();
    }

    private void run_add_interest_screen() {
        String sql = "SELECT * FROM Accounts WHERE open = true AND interest_added = false";
        //generateAvgDailyBalance();
        HashMap<Integer, Account> valid_accts = db.query_acct(sql);
        if (valid_accts.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No available accounts to add interest to");
            return;
        }

        ArrayList<Account> accts = new ArrayList<>();
        Iterator it = valid_accts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Account acct = (Account) pair.getValue();
            accts.add(acct);
        }

        TransactionHandler t = new TransactionHandler();
        for (int i = 0; i < accts.size(); i++) {
            if (!t.accrue_interest(accts.get(i))) {
                JOptionPane.showMessageDialog(frame, "There was error adding interest to account " + String.valueOf(accts.get(i).getAccountid()));
                return;
            }
        }
        JOptionPane.showMessageDialog(frame, "Interest was successfully added to all open accounts");
    }


    private void run_delete_closed_accts_screen() {
        String accts_sql = "SELECT * FROM Accounts WHERE open = false";
        HashMap<Integer, Account> closed_accts = db.query_acct(accts_sql);

        ArrayList<JLabel> deleted_accts = new ArrayList<>();

        Iterator it = closed_accts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Account acct = (Account) pair.getValue();
            deleted_accts.add(new JLabel(String.valueOf(acct.getAccountid())));
            db.delete_acct(acct);
        }
        display_labels(deleted_accts, "The following accounts were deleted:");
    }

    private void run_delete_transactions_screen() {
        db.delete_transactions();
        JOptionPane.showMessageDialog(frame, "Deleted all transactions from the database");
    }


    private class ButtonHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String button = actionEvent.getActionCommand();

            switch (button) {
                case "Enter Check Transaction":
                    BankTellerGUI.this.run_check_screen();
                    break;

                case "Generate Monthly Statement":
                    BankTellerGUI.this.run_monthly_statement_screen();
                    break;

                case "List Closed Accounts":
                    BankTellerGUI.this.run_closed_accts_screen();
                    break;

                case "Generate DTER":
                    BankTellerGUI.this.run_dter_screen();
                    break;

                case "Customer Report":
                    BankTellerGUI.this.run_customer_report_screen();
                    break;

                case "Add Interest":
                    BankTellerGUI.this.run_add_interest_screen();
                    break;

                case "Create Account":
                    AccountCreationGUI accountCreationGUI = new AccountCreationGUI();
                    accountCreationGUI.run_create_acct_screen(time);
                    break;

                case "Delete Closed Accounts and Customers":
                    BankTellerGUI.this.run_delete_closed_accts_screen();
                    break;

                case "Delete Transactions":
                    BankTellerGUI.this.run_delete_transactions_screen();
                    break;

                default:
                    System.out.println("Something is really wrong, bank teller edition");
                    break;
            }
        }
    }

    private boolean validate_amount(String amount) {
        return amount.matches("[0-9]+(.[0-9]+)?");
    }

    private boolean validate_acct(String acctid) {
        return acctid.matches("[0-9]+");
    }

    private void display_labels(ArrayList<JLabel> labels, String heading) {
        JFrame label_frame = new JFrame();
        label_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel label_panel = new JPanel();
        label_panel.setLayout(new BoxLayout(label_panel, BoxLayout.Y_AXIS));

        JLabel heading_label = new JLabel(heading);
        label_panel.add(heading_label);

        for (int i = 0; i < labels.size(); i++) {
            label_panel.add(labels.get(i));
        }

        label_frame.getContentPane().add(label_panel);
        label_frame.setSize(400, 400);
        label_frame.setVisible(true);
    }
}
