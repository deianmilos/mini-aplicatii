import net.proteanit.sql.DbUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Banca {
    private JTextField cnpField;
    private JTextField euroField;
    private JTextField ronField;
    private JButton createButton;
    private JTable table;
    private JPanel Banca;
    private JScrollPane base;
    private JButton closeButton;
    private JButton informationButton;
    private JTextField withdrawalField1;
    private JButton withdrawalButton1;
    private JButton withdrawalButton2;
    private JTextField withdrawalField2;
    private JButton depuneEuroButton;
    private JButton depuneRonButton;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Banca");
        frame.setContentPane(new Banca().Banca);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    Connection connection;
    PreparedStatement pst, pst1, pstFisc, pstVerificare;

    public void connect(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/banca", "root", "password");
            System.out.println("Success");

        }catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    void table_load(){
        try{
            pst=connection.prepareStatement("select * from clienti");
            ResultSet rs = pst.executeQuery();
            table.setModel(DbUtils.resultSetToTableModel(rs));
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public Banca() {
        connect();
        table_load();
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cnp, euro, ron;

                cnp = cnpField.getText();

                if(!(cnp.length()==13)){
                    JOptionPane.showMessageDialog(null,"CNP incorect");
                    return;
                }
                euro = euroField.getText();
                if(euro!=null && Double.parseDouble(euro)<1000)
                    {JOptionPane.showMessageDialog(null,"Soldul contului nu poate fi mai mic de 1000 EURO");
                        return;
                    }

                ron = ronField.getText();
                if(ron!=null && Double.parseDouble(ron)<1000)
                {JOptionPane.showMessageDialog(null,"Soldul contului nu poate fi mai mic de 1000 RON");
                    return;}

                try{

                    pst = connection.prepareStatement("insert into clienti(CNP,SoldEURO,SoldRON)values(?,?,?)");
                    pst.setString(1,cnp);
                    pst.setString(2,euro);
                    pst.setString(3,ron);
                    pst.executeUpdate();

                    JOptionPane.showMessageDialog(null, "S-a inceput monitorizarea clientului "+cnp);

                    pstFisc = connection.prepareStatement("insert into fisc(mesaj,cnp_client)values(?,?)");
                    pstFisc.setString(1,"S-au deschis conturile");
                    pstFisc.setString(2,cnp);
                    pstFisc.executeUpdate();

                    table_load();
                    cnpField.setText("");
                    euroField.setText("");
                    ronField.setText("");
                    cnpField.requestFocus();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
        });

        informationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    String cnp = cnpField.getText();
                    pst = connection.prepareStatement("select SoldEURO, SoldRON from clienti where cnp = ?");
                    pst.setString(1, cnp);

                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        String euro = rs.getString(1);
                        String ron = rs.getString(2);

                        euroField.setText(euro);
                        ronField.setText(ron);
                    } else {
                        euroField.setText("");
                        ronField.setText("");
                        JOptionPane.showMessageDialog(null, "Nu exista cont cu acest CNP");
                    }

                }catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cnp;
                double euro = 0, ron = 0;

                cnp = cnpField.getText();

                try{
                    pstVerificare = connection.prepareStatement("select SoldEURO,SoldRON from clienti where cnp = ?");
                    pstVerificare.setString(1,cnp);

                    ResultSet rs = pstVerificare.executeQuery();

                    if(rs.next())
                    {euro = Double.parseDouble(rs.getString(1));
                    ron = Double.parseDouble(rs.getString(2));}

                    if(ron==0 && euro==0){
                    pstFisc = connection.prepareStatement("delete from fisc where cnp_client = ?");
                    pstFisc.setString(1,cnp);
                    pstFisc.executeUpdate();
                    JOptionPane.showMessageDialog(null, "S-a oprit monitorizarea clientului "+cnp);

                    pst = connection.prepareStatement("delete from clienti where cnp = ?");
                    pst.setString(1, cnp);
                    pst.executeUpdate();
                    table_load();

                    cnpField.setText("");
                    euroField.setText("");
                    ronField.setText("");
                    cnpField.requestFocus();
                    }
                    else JOptionPane.showMessageDialog(null,"Lichidarea se face doar daca soldurile ambelor conturi sunt 0");

                }catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
        });

        withdrawalButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String cnp;
                cnp = cnpField.getText();

                double euror;
                euror = Double.parseDouble(withdrawalField1.getText());

                try {

                    pst1 = connection.prepareStatement("select SoldEuro from clienti where cnp =?");
                    pst1.setString(1, cnp);

                    ResultSet rs = pst1.executeQuery();

                    if (rs.next()) {
                        double euro =Double.parseDouble(rs.getString(1));

                        if(euror<=euro)euro=euro-euror;
                            else JOptionPane.showMessageDialog(null, "Fonduri insuficiente!!");
                        pst = connection.prepareStatement("update clienti set SoldEURO = ? where cnp = ?");
                        pst.setString(1,String.valueOf(euro));
                        pst.setString(2,cnp);
                        pst.executeUpdate();
                        table_load();

                        pstFisc = connection.prepareStatement("insert into fisc(mesaj,cnp_client)values(?,?)");
                        pstFisc.setString(1,"S-au retras "+euror+" euro");
                        pstFisc.setString(2,cnp);
                        pstFisc.executeUpdate();
                    } else {
                        euroField.setText("");
                        ronField.setText("");
                        JOptionPane.showMessageDialog(null, "Nu exista cont cu acest CNP");
                    }

                }catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
        });
        withdrawalButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cnp;
                cnp = cnpField.getText();

                double ronr;
                ronr = Double.parseDouble(withdrawalField2.getText());

                try {

                    pst1 = connection.prepareStatement("select SoldRON from clienti where cnp =?");
                    pst1.setString(1, cnp);

                    ResultSet rs = pst1.executeQuery();

                    if (rs.next()) {
                        double ron =Double.parseDouble(rs.getString(1));

                        if(ronr<=ron)ron=ron-ronr;
                        else JOptionPane.showMessageDialog(null, "Fonduri insuficiente!!");
                        pst = connection.prepareStatement("update clienti set SoldRON = ? where cnp = ?");
                        pst.setString(1,String.valueOf(ron));
                        pst.setString(2,cnp);
                        pst.executeUpdate();
                        table_load();

                        pstFisc = connection.prepareStatement("insert into fisc(mesaj,cnp_client)values(?,?)");
                        pstFisc.setString(1,"S-au retras "+ronr+" lei");
                        pstFisc.setString(2,cnp);
                        pstFisc.executeUpdate();

                    } else {
                        euroField.setText("");
                        ronField.setText("");
                        JOptionPane.showMessageDialog(null, "Nu exista cont cu acest CNP");
                    }

                }catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
        });
        depuneEuroButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cnp;
                cnp = cnpField.getText();

                double euror;
                euror = Double.parseDouble(withdrawalField1.getText());

                try {

                    pst1 = connection.prepareStatement("select SoldEuro from clienti where cnp =?");
                    pst1.setString(1, cnp);

                    ResultSet rs = pst1.executeQuery();

                    if (rs.next()) {
                        double euro =Double.parseDouble(rs.getString(1));

                        euro=euro+euror;
                        pst = connection.prepareStatement("update clienti set SoldEURO = ? where cnp = ?");
                        pst.setString(1,String.valueOf(euro));
                        pst.setString(2,cnp);
                        pst.executeUpdate();
                        table_load();

                        pstFisc = connection.prepareStatement("insert into fisc(mesaj,cnp_client)values(?,?)");
                        pstFisc.setString(1,"S-au depus "+euror+" euro");
                        pstFisc.setString(2,cnp);
                        pstFisc.executeUpdate();

                    } else {
                        euroField.setText("");
                        ronField.setText("");
                        JOptionPane.showMessageDialog(null, "Nu exista cont cu acest CNP");
                    }

                }catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
        });
        depuneRonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cnp;
                cnp = cnpField.getText();

                double ronr;
                ronr = Double.parseDouble(withdrawalField2.getText());

                try {

                    pst1 = connection.prepareStatement("select SoldRON from clienti where cnp =?");
                    pst1.setString(1, cnp);

                    ResultSet rs = pst1.executeQuery();

                    if (rs.next()) {
                        double ron =Double.parseDouble(rs.getString(1));

                        ron=ron+ronr;
                        pst = connection.prepareStatement("update clienti set SoldRON = ? where cnp = ?");
                        pst.setString(1,String.valueOf(ron));
                        pst.setString(2,cnp);
                        pst.executeUpdate();
                        table_load();

                        pstFisc = connection.prepareStatement("insert into fisc(mesaj,cnp_client)values(?,?)");
                        pstFisc.setString(1,"S-au depus "+ronr+" lei");
                        pstFisc.setString(2,cnp);
                        pstFisc.executeUpdate();

                    } else {
                        euroField.setText("");
                        ronField.setText("");
                        JOptionPane.showMessageDialog(null, "Nu exista cont cu acest CNP");
                    }

                }catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
        });

    }

}
