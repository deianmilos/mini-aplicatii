import net.proteanit.sql.DbUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Farmacie {
    private JComboBox orasField1;
    private JButton afiseazaFarmaciileDinButton;
    private JTextField farmacieField3;
    private JButton informatiiComenziButton;
    private JTextField categorieField;
    private JTextField anField;
    private JButton afiseazaNumarComenziButton;
    private JTextField anField2;
    private JButton afiseazaMaximulDeComenziButton;
    private JTextField farmacieField;
    private JTextField medicamentField;
    private JButton afiseazaFarmaciileCeAuButton;
    private JButton afiseazaCantitateaDeButton;
    private JTextField medicamentField2;
    private JPanel Farmacie;
    private JTable table1;
    private JScrollPane table_1;
    private JTextField cityField1;
    private JTextField cityField2;
    private JTextField lunaField;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Farmacie");
        frame.setContentPane(new Farmacie().Farmacie);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    Connection connection;
    PreparedStatement pst;

    public void connect(){
        try{

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/farmacie", "root", "password");
            System.out.println("Success");

        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void table_load(){
        try{
            pst=connection.prepareStatement("select * from farmacie");
            ResultSet rs = pst.executeQuery();
            table1.setModel(DbUtils.resultSetToTableModel(rs));
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public Farmacie() {
        connect();
        table_load();

        //afisarea tuturor farmaciilor dintr-un anumit oras (ex: Timisoara: Farmacia Vlad, Farmacia Dona, Farmacia Operei)
        afiseazaFarmaciileDinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String oras;

                oras = cityField1.getText();

                try{
                    pst = connection.prepareStatement("select nume, oras, nr_tel from farmacie where oras = ?" );
                    pst.setString(1,oras);

                    ResultSet rs = pst.executeQuery();

                    if(oras.equals(""))JOptionPane.showMessageDialog(null,"Introduceti un oras");
                   // else
                    //    if(!rs.next())JOptionPane.showMessageDialog(null,"Nu exista orasul introdus in baza de date");
                        else{
                            table1.setModel(DbUtils.resultSetToTableModel(rs));}
                            cityField1.setText("");

                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
        });

        //cate comenzi a primit farmacia Dona in August. suma totala, valoarea medie per comanda
        informatiiComenziButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String farmacie, luna;
                int nr_comenzi = 0;
                double suma_totala = 0;

                farmacie = farmacieField3.getText();
                luna = lunaField.getText();

                try{
                    pst = connection.prepareStatement("select sum(comanda_stoc.cantitate*pret),comanda.id_comanda from comanda " +
                                    "inner join comanda_stoc inner join stoc inner join farmacie inner join medicament " +
                            "on comanda_stoc.id_comanda = comanda.id_comanda and stoc.id_stoc = comanda_stoc.id_stoc and farmacie.cod = stoc.cod_farmacie and medicament.id_medicament = stoc.id_medicament " +
                            "and farmacie.nume = ? and comanda.data_livrare like '%.' ? '.%' " +
                            "group by comanda.id_comanda");

                    pst.setString(1,farmacie);
                    pst.setString(2,luna);

                    ResultSet rs = pst.executeQuery();

                    while(rs.next()){
                        nr_comenzi++;
                        suma_totala += Double.parseDouble(rs.getString(1));
                    }

                    JOptionPane.showMessageDialog(null,"Exista "+nr_comenzi+" comenzi cu suma totala in valoare de "+suma_totala+" si valoarea medie pe comanda "+(suma_totala/nr_comenzi));

                    medicamentField2.setText("");
                    cityField2.setText("");

                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
        });

        //cate comenzi de antibiotice a primit farmacia Vlad in 2020
        afiseazaNumarComenziButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String farmacie,categorie,an;

                farmacie = farmacieField.getText();
                categorie = categorieField.getText();
                an = anField.getText();

                try{
                    pst = connection.prepareStatement("select comanda.id_comanda from comanda " +
                            "inner join stoc inner join medicament inner join farmacie inner join categorie inner join comanda_stoc inner join medicament_categorie" +
                            " on stoc.id_stoc=comanda_stoc.id_stoc and farmacie.cod=stoc.cod_farmacie and medicament.id_medicament=stoc.id_medicament and medicament.id_medicament=medicament_categorie.id_medicament and categorie.id_categorie=medicament_categorie.id_categorie and comanda_stoc.id_stoc= stoc.id_stoc and comanda_stoc.id_comanda=comanda.id_comanda and farmacie.nume = ? and categorie.categorie = ? and comanda.data_livrare like '%' ? ");
                    pst.setString(1,farmacie);
                    pst.setString(2,categorie);
                    pst.setString(3,an);

                    ResultSet rs = pst.executeQuery();

                    int nr_comenzi = 0;

                    while(rs.next()){
                        nr_comenzi++;
                    }

                    JOptionPane.showMessageDialog(null,"Farmacia "+farmacie+ " a avut "+nr_comenzi+ " comenzi de "+categorie+ " in "+an );

                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
        });

        //care e farmacia care a comandat cel mai mult in 2020 , ca valoare absoluta
        afiseazaMaximulDeComenziButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String an;

                an = anField2.getText();

                try{
                    pst = connection.prepareStatement("select farmacie.nume as farm, max(numar_comenzi) from (select nume, count(com) as numar_comenzi from" +
                            "(select cod, farmacie.nume, comanda.id_comanda as com, nume_pacient from farmacie " +
                            "inner join stoc inner join comanda_stoc inner join comanda " +
                            "on stoc.cod_farmacie = farmacie.cod and comanda_stoc.id_stoc = stoc.id_stoc and comanda_stoc.id_comanda = comanda.id_comanda " +
                            "and comanda.data_livrare like '%' ?" +
                            "group by comanda.id_comanda)farmacie " +
                            "group by cod)farmacie");

                    pst.setString(1,an);

                    ResultSet rs = pst.executeQuery();

                    String farmacie = null ;
                    if(rs.next())farmacie=rs.getString(1);

                    JOptionPane.showMessageDialog(null,"Farmacia cu cel mai mare numar de comenzi din anul "+an+" este "+farmacie );

                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
        });

        //afisarea tuturor farmaciilor care au pe stoc un anumit medicament
        afiseazaFarmaciileCeAuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String medicament;

                medicament = medicamentField.getText();

                try{
                    pst = connection.prepareStatement("select farmacie.nume, oras, nr_tel from farmacie " +
                            "inner join stoc inner join medicament " +
                            "on stoc.cod_farmacie=farmacie.cod and medicament.id_medicament=stoc.id_medicament and medicament.nume_medicament = ?" );
                    pst.setString(1,medicament);

                    ResultSet rs = pst.executeQuery();
                    table1.setModel(DbUtils.resultSetToTableModel(rs));
                    medicamentField.setText("");

                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
        });

        //afisarea cantitatilor totale pentru un medicament selectat dintr-un anumit oras (ex: Algocalmin Cluj: 1000 bucati)
        afiseazaCantitateaDeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String medicament,oras;

                medicament = medicamentField2.getText();
                oras = cityField2.getText();

                try{
                    pst = connection.prepareStatement("select farmacie.nume, oras, stoc.cantitate from farmacie " +
                            "inner join stoc inner join medicament " +
                            "on stoc.cod_farmacie=farmacie.cod and medicament.id_medicament=stoc.id_medicament and medicament.nume_medicament = ? and farmacie.oras = ?" );
                    pst.setString(1,medicament);
                    pst.setString(2,oras);

                    ResultSet rs = pst.executeQuery();

                    int cantitate = 0;

                    while(rs.next()){
                        cantitate += Integer.parseInt(rs.getString(3));
                    }
                    JOptionPane.showMessageDialog(null,"In "+oras+" exista "+cantitate+" bucati de "+medicament);

                    medicamentField2.setText("");
                    cityField2.setText("");

                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
        });

    }
}
