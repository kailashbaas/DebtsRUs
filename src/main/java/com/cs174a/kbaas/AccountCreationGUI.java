package com.cs174a.kbaas;

import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.*;

public class AccountCreationGUI {

    private DatabaseAccessor db;
    private ArrayList<Customer> new_owners;

    public AccountCreationGUI() {
        db = new DatabaseAccessor();
        new_owners = new ArrayList<>();
    }

    public void run_create_acct_screen(Timestamp time) {
        JFrame frame1 = new JFrame();
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] account_types = {"Student-Checking", "Interest-Checking", "Savings", "Pocket"};
        JPanel type_panel = new JPanel(new FlowLayout());
        JLabel type_label = new JLabel("Account Type:");
        JComboBox type_choices = new JComboBox(account_types);
        type_panel.add(type_label);
        type_panel.add(type_choices);
        panel.add(type_panel);

        JPanel accountid_panel = new JPanel(new FlowLayout());
        JLabel accountid_label = new JLabel("Account ID:");
        JTextField accountid = new JTextField(20);
        accountid_panel.add(accountid_label);
        accountid_panel.add(accountid);
        panel.add(accountid_panel);

        JPanel branch_panel = new JPanel(new FlowLayout());
        JLabel branch_label = new JLabel("Branch:");
        JTextField branch = new JTextField(20);
        branch_panel.add(branch_label);
        branch_panel.add(branch);
        panel.add(branch_panel);

        JPanel balance_panel = new JPanel(new FlowLayout());
        JLabel balance_label = new JLabel("Initial Balance:");
        JTextField balance = new JTextField(20);
        balance_panel.add(balance_label);
        balance_panel.add(balance);
        panel.add(balance_panel);

        JPanel primary_owner_panel = new JPanel(new FlowLayout());
        JLabel primary_owner_label = new JLabel("Primary Owner Tax ID:");
        JTextField primary_owner = new JTextField(20);
        primary_owner_panel.add(primary_owner_label);
        primary_owner_panel.add(primary_owner);
        panel.add(primary_owner_panel);

        JPanel owners_panel = new JPanel(new FlowLayout());
        JLabel owners_label =  new JLabel("Other Owners (Tax IDs separated by commas):");
        JTextField owners = new JTextField(20);
        owners_panel.add(owners_label);
        owners_panel.add(owners);
        panel.add(owners_panel);

        JPanel initial_depositor_panel = new JPanel(new FlowLayout());
        JLabel initial_depositor_label = new JLabel("Initial Deposit Source");
        JTextField initial_depositor = new JTextField(20);
        initial_depositor_panel.add(initial_depositor_label);
        initial_depositor_panel.add(initial_depositor);
        panel.add(initial_depositor_panel);

        JPanel linked_acct_panel = new JPanel(new FlowLayout());
        JLabel linked_acct_label = new JLabel("Linked Account:");
        JTextField linked_acct = new JTextField(20);
        linked_acct_panel.add(linked_acct_label);
        linked_acct_panel.add(linked_acct);
        panel.add(linked_acct_panel);

        JButton create_acct = new JButton("Create Account");
        create_acct.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Account linked = null;
                int acct_id = Integer.parseInt(accountid.getText());
                String branch_name = branch.getText();
                String initial_balance = balance.getText();
                String primary_owner_id = primary_owner.getText();
                String linked_acct_id = linked_acct.getText();
                String initial_depositor_id = initial_depositor.getText();
                if (type_choices.getSelectedIndex() == -1 || branch_name.equals("") || initial_balance.equals("") || primary_owner_id.equals("")
                        || initial_depositor_id.equals("") || (type_choices.getSelectedItem().toString().equals("Pocket") && linked_acct_id.equals(""))) {
                    JOptionPane.showMessageDialog(frame1, "Please fill in all fields");
                    return;
                }
                Double deposit_amount = Double.parseDouble(balance.getText());
                Account acct = Account.instantiateAcct((String) type_choices.getSelectedItem());
                acct.setAccountid(acct_id);
                acct.setOpen(true);
                acct.setBranch(branch_name);
                acct.setBalance(0);
                acct.setInterest_added(false);
                acct.setType(((String) type_choices.getSelectedItem()));
                if (acct.getType().equals("Pocket")) {
                    String sql = "SELECT * FROM Accounts WHERE accountid = " + linked_acct_id;
                    linked = db.query_acct(sql).get(Integer.parseInt(linked_acct_id));
                }
                acct.setLinked_acct(linked);

                String owners_string = primary_owner_id + "," + owners.getText();
                String[] owners_arr = owners_string.split(",");
                for (int i = 0; i < owners_arr.length; i++) {
                    String sql = "SELECT COUNT(*) AS CC FROM Customers WHERE tax_id = " + owners_arr[i];
                    int result = (int) db.aggregate_query(sql, "CC");
                    if (result == 0) {
                        String message = "Customer " + owners_arr[i] + " does not exist. You will now be directed to customer creation.";
                        JOptionPane.showMessageDialog(frame1, message);
                        String customer_info = JOptionPane.showInputDialog(null, "Enter the customer info in the following format: Tax ID:PIN:Name:Address");
                        String[] info_array = customer_info.split(":");
                        int tax_id = Integer.parseInt(info_array[0]);
                        int hashed_pin = info_array[1].hashCode();
                        String name = info_array[2];
                        String address = info_array[3];
                        new_owners.add(new Customer(tax_id, hashed_pin, name, address));
                    }
                }
                String primary_owner_sql = "SELECT * FROM Customers WHERE tax_id = " + primary_owner_id;
                Customer primary_owner = new Customer(Integer.parseInt(primary_owner_id), 0, "", "");
                acct.setPrimary_owner(primary_owner);

                String other_owners = "";
                if (!owners.getText().equals("")) {
                    other_owners = ", " + owners.getText();
                }
                String owners_sql = "SELECT * FROM Customers WHERE tax_id IN (" + primary_owner_id + other_owners + ")";
                HashMap<Integer, Customer> owners_map = db.query_customer(owners_sql, "tax_id");
                ArrayList<Customer> owners_list = new ArrayList<>();
                if (!owners_map.isEmpty()) {
                    Iterator it = owners_map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        Customer c = (Customer) pair.getValue();
                        owners_list.add(c);
                    }
                }

                owners_list.addAll(new_owners);
                db.insert_new_acct(acct, owners_list, new_owners);
                Customer depositor = new Customer(Integer.parseInt(initial_depositor.getText()),0,"","");
                Transaction t = new Transaction();
                TransactionHandler transactionHandler = new TransactionHandler();
                if (acct.getType().equals("Pocket")) {
                    transactionHandler.top_up(deposit_amount, acct, depositor, time);
                }
                else {
                    transactionHandler.deposit(deposit_amount, acct, depositor, time);
                }
                frame1.dispose();
            }
        });
        panel.add(create_acct);

        frame1.getContentPane().add(panel);
        frame1.setSize(400, 400);
        frame1.setVisible(true);
    }

    private void run_customer_creation(String preferred_tax_id) {
        String customer_info = JOptionPane.showInputDialog(null, "Enter the customer info in the following format: Tax ID|PIN|Name|Address");
        String[] info_array = customer_info.split("|");
        int tax_id = Integer.parseInt(info_array[0]);
        int hashed_pin = info_array[1].hashCode();
        String name = info_array[2];
        String address = info_array[3];
        String msg = String.valueOf(tax_id) + " " + String.valueOf(hashed_pin) + " " + name + " " + address;
        new_owners.add(new Customer(tax_id, hashed_pin, name, address));
        /*JFrame frame1 = new JFrame();
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel tax_id_panel = new JPanel(new FlowLayout());
        JLabel tax_id_label = new JLabel("Tax ID:");
        JTextField tax_id = new JTextField(20);
        tax_id_panel.add(tax_id_label);
        tax_id_panel.add(tax_id);
        panel.add(tax_id_panel);

        JPanel pin_panel = new JPanel(new FlowLayout());
        JLabel pin_label = new JLabel("PIN:");
        JTextField pin = new JTextField(20);
        pin_panel.add(pin_label);
        pin_panel.add(pin);
        panel.add(pin_panel);

        JPanel name_panel = new JPanel(new FlowLayout());
        JLabel name_label = new JLabel("Name:");
        JTextField name = new JTextField(20);
        name_panel.add(name_label);
        name_panel.add(name);
        panel.add(name_panel);

        JPanel address_panel = new JPanel(new FlowLayout());
        JLabel address_label =  new JLabel("Address:");
        JTextField address = new JTextField(20);
        address_panel.add(address_label);
        address_panel.add(address);
        panel.add(address_panel);

        JButton create = new JButton("Create Customer");
        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String customer_name = name.getText();
                String customer_addr = address.getText();
                if (customer_addr.equals("") || customer_name.equals("") || pin.getText().equals("")) {
                    JOptionPane.showMessageDialog(frame1, "Please fill out every field");
                    return;
                }
                int customer_tax_id = Integer.parseInt(tax_id.getText());
                int hashed_pin = pin.getText().hashCode();
                new_owners.add(new Customer(customer_tax_id, hashed_pin, customer_name, customer_addr));
                frame1.dispose();
            }
        });
        panel.add(create);

        frame1.getContentPane().add(panel);
        frame1.setSize(400, 400);
        frame1.setVisible(true);*/
    }
}
