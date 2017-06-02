import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author xiaor
 */
public class Hw3 extends javax.swing.JFrame {
    Connection connection = null;
    String query = null;
    String logicRelation = "AND";
    List<JCheckBox> checkedGenreCheckBox = new ArrayList<>();
    List<JCheckBox> checkedCountryCheckBox = new ArrayList<>();
    List<JCheckBox> checkedTagCheckBox = new ArrayList<>();
    Set<String> checkedMovie = new HashSet<>();
    ReleaseYearQueryGroup releaseYearQueryGroup = new ReleaseYearQueryGroup();
    CountriesQueryGroup countriesQueryGroup = new CountriesQueryGroup();
    ActorsQueryGroup actorsQueryGroup = new ActorsQueryGroup();
    DirectorsQueryGroup directorsQueryGroup = new DirectorsQueryGroup();
    TagsQueryGroup tagsQueryGroup = new TagsQueryGroup();
    CountriesToCastsTagsQueryGroup countriesToCastsTagsQueryGroup = new CountriesToCastsTagsQueryGroup();
    ActorsToDirectorsTagsQueryGroup actorsToDirectorsTagsQueryGroup = new ActorsToDirectorsTagsQueryGroup();
    DirectorsToTagsQueryGroup directorsToTagsQueryGroup = new DirectorsToTagsQueryGroup();
    FinalQueryGroup finalQueryGroup = new FinalQueryGroup();
    FinalUserQueryGroup finalUserQueryGroup = new FinalUserQueryGroup();

    ActionListener releaseYearActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            initialOthers(yearFromComboBox.getSelectedItem().toString(), yearToComboBox.getSelectedItem().toString());
            finalQueryGroup.make();
            queryText.setText(finalQueryGroup.show());
        }
    };

    ActionListener actorActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            actorToDirectorTag();
            finalQueryGroup.make();
            queryText.setText(finalQueryGroup.show());
        }
    };

    ActionListener directorActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            directorToTag();
            finalQueryGroup.make();
            queryText.setText(finalQueryGroup.show());
        }
    };

    //actionlistener for jtable to show retrieved movie data
    TableModelListener tableModelListener = new TableModelListener(){
        @Override
        public void tableChanged(TableModelEvent e) {
            TableModel tableModel = (TableModel) e.getSource();
            int row = e.getFirstRow();
            int maxRow = tableModel.getRowCount();
            String MID = tableModel.getValueAt(row, 1).toString();
            //update the pool of selected movie's MIDs for user query
            if (tableModel.getValueAt(row,0).toString().equals("true")){
                checkedMovie.add(MID);
                int temp = row;
                //if mark on movie, all columns of this movie will be marked
                while (row > 0 && tableModel.getValueAt(row - 1, 1).toString().equals(tableModel.getValueAt(row, 1).toString()) && tableModel.getValueAt(row - 1, 0).toString().equals("false")){
                    tableModel.setValueAt(true, --row, 0);
                }
                while (temp < maxRow - 1 && tableModel.getValueAt(temp + 1, 1).toString().equals(tableModel.getValueAt(temp, 1).toString()) && tableModel.getValueAt(temp + 1, 0).toString().equals("false")){
                    tableModel.setValueAt(true, ++temp, 0);
                }
            }
            else{
                checkedMovie.remove(MID);
                int temp = row;
                //if mark on movie, all columns of this movie will be marked
                while (row > 0 && tableModel.getValueAt(row - 1, 1).toString().equals(tableModel.getValueAt(row, 1).toString()) && tableModel.getValueAt(row - 1, 0).toString().equals("true")){
                    tableModel.setValueAt(false, --row, 0);
                }
                while (temp < maxRow - 1 && tableModel.getValueAt(temp + 1, 1).toString().equals(tableModel.getValueAt(temp, 1).toString()) && tableModel.getValueAt(temp + 1, 0).toString().equals("true")){
                    tableModel.setValueAt(false, ++temp, 0);
                }
            }
            finalUserQueryGroup.make();
            queryText.setText(finalUserQueryGroup.show());
        }
    };

    //query used to get release years from selected genres
    private class ReleaseYearQueryGroup{
        private final String query1 = "SELECT DISTINCT movies.ReleaseYear\nFROM movies, movie_genres\nWHERE movies.MID IN\n(";
        private final String query2 = "SELECT DISTINCT MID FROM movie_genres WHERE movie_genres.Genre LIKE '";
        private final String query3 = "'";
        private final String query4 = ")";
        private String yearsQuery = "";
    }

    //query used to get countires from selected genres and years
    private class CountriesQueryGroup{
        private final String query1 = "SELECT DISTINCT movie_countries.Country\n" +
                                        "FROM movies, movie_countries\n" +
                                        "WHERE movie_countries.MID = movies.MID AND movies.MID IN\n(";
        private final String query2 = "SELECT DISTINCT MID FROM movie_genres WHERE movie_genres.Genre LIKE '";
        private final String query3 = "'";
        private final String query4 = ")";
        private String countriesQuery = "";
    }

    //query used to get actors from selected genres and years
    private class ActorsQueryGroup{
        private String actorsQuery = "";
    }

    //query used to get directors from selected genres and years
    private class DirectorsQueryGroup{
        private String directorsQuery = "";
    }

    //query used to get tags from selected genres and years
    private class TagsQueryGroup{
        private String tagsQuery = "";
    }

    //query used to get casts and tags from selected genres, years and countries
    private class CountriesToCastsTagsQueryGroup{
        private final String query1 = "SELECT DISTINCT MID FROM movie_countries WHERE movie_countries.Country LIKE '";
        private final String query2 = "'";
        private final String query3 = ")";
        private String countriesToActorsQuery = "";
        private String countriesToDirectorsQuery = "";
        private String countriesToTagsQuery = "";
    }

    //query used to get directors and tags from selected genres, years, countries and actors
    private class ActorsToDirectorsTagsQueryGroup{
        private final String query1 = "SELECT DISTINCT MID FROM movie_actors WHERE movie_actors.ActorName LIKE '";
        private final String query2 = "'";
        private final String query3 = ")";
        private String actorsToDirectorsQuery = "";
        private String actorsToTagsQuery = "";
    }

    //query used to get tags from selected genres, years, countries, actors and directors
    private class DirectorsToTagsQueryGroup{
        private final String query1 = "SELECT DISTINCT MID FROM movie_directors WHERE movie_directors.DirectorName LIKE '";
        private final String query2 = "'";
        private final String query3 = ")";
        private String directorsToTagsQuery = "";
    }

    //final query to get target movie data
    private class FinalQueryGroup{
        private final String select = "SELECT DISTINCT movies.MID, movies.Title, movie_genres.Genre, movies.ReleaseYear, movie_countries.Country, movies.RAR, movies.RAN\n";
        private final String from = "FROM movies, movie_genres, movie_countries\n";
        private final String where = "WHERE movies.MID = movie_genres.MID AND movies.MID = movie_countries.MID AND movies.MID IN\n";
        private String genres = "";
        private String countries = "";
        private String actors = "";
        private String director = "";
        private String tags = "";
        private String finalQuery = "";

        //method to build the query based on selected items
        private void make() {
            genres = "";
            countries = "";
            actors = "";
            director = "";
            tags = "";
            
            if (checkedGenreCheckBox.isEmpty()) {
                return;
            }
            genres = "SELECT DISTINCT MID FROM movie_genres WHERE movie_genres.Genre LIKE '" + checkedGenreCheckBox.get(0).getText() + "'";
            for (int i = 1; i < checkedGenreCheckBox.size(); i++) {
                genres += (logicRelation.equals("AND") ? "\nINTERSECT\n" : "\nUNION\n") + "SELECT DISTINCT MID FROM movie_genres WHERE movie_genres.Genre LIKE '" + checkedGenreCheckBox.get(i).getText() + "'";
            }
            genres = "(" + genres + ")";
            if (!checkedCountryCheckBox.isEmpty()){
                countries = "SELECT DISTINCT MID FROM movie_countries WHERE movie_countries.Country LIKE '" + checkedCountryCheckBox.get(0).getText() + "'";
                for (int i = 1; i < checkedCountryCheckBox.size(); i++) {
                    countries += (logicRelation.equals("AND") ? "\nINTERSECT\n" : "\nUNION\n") + "SELECT DISTINCT MID FROM movie_countries WHERE movie_countries.Country LIKE '" + checkedCountryCheckBox.get(i).getText() + "'";
                }
                countries = "\nINTERSECT\n(" + countries + ")";
            }
            Set<String> actorSet = new HashSet<>();
            actorSet.add(actorComboBox1.getSelectedItem().toString());
            actorSet.add(actorComboBox2.getSelectedItem().toString());
            actorSet.add(actorComboBox3.getSelectedItem().toString());
            actorSet.add(actorComboBox4.getSelectedItem().toString());
            actorSet.remove("");
            List<String> actorList = new ArrayList<>(actorSet);
            if (!actorList.isEmpty()){
                actors = "SELECT DISTINCT MID FROM movie_actors WHERE movie_actors.ActorName LIKE '" + actorList.get(0) + "'";
                for (int i = 1; i < actorList.size(); i++) {
                    actors += (logicRelation.equals("AND") ? "\nINTERSECT\n" : "\nUNION\n") + "SELECT DISTINCT MID FROM movie_actors WHERE movie_actors.ActorName LIKE '" + actorList.get(i) + "'";
                }
                actors = "\nINTERSECT\n(" + actors + ")";
            }
            if (!directorComboBox.getSelectedItem().toString().equals("")){
                director = "\nINTERSECT\nSELECT DISTINCT MID FROM movie_directors WHERE movie_directors.DirectorName LIKE '" + directorComboBox.getSelectedItem().toString() + "'";
            }
            if (!checkedTagCheckBox.isEmpty()){
                String[] temp = checkedTagCheckBox.get(0).getText().split(" ");
                tags = "SELECT DISTINCT MID FROM movie_tags WHERE movie_tags.TID = " + temp[0];
                if (!tagWeightTextField.getText().equals("")){
                    tags += " AND movie_tags.TagWeight " + tagWeightComboBox.getSelectedItem().toString() + " " + tagWeightTextField.getText();
                }
                for (int i = 1; i < checkedTagCheckBox.size(); i++) {
                    temp = checkedTagCheckBox.get(i).getText().split(" ");
                    tags += (logicRelation.equals("AND") ? "\nINTERSECT\n" : "\nUNION\n") + "SELECT DISTINCT MID FROM movie_tags WHERE movie_tags.TID = " + temp[0];
                    if (!tagWeightTextField.getText().equals("")){
                        tags += " AND movie_tags.TagWeight " + tagWeightComboBox.getSelectedItem().toString() + " " + tagWeightTextField.getText();
                    }
                }
                tags = "\nINTERSECT\n(" + tags + ")";
            }
            String constrain = "(" + genres + countries + actors + director + tags + ")";
            String year = (yearFromComboBox.getItemCount() == 0) ? "" : ("\nAND movies.ReleaseYear >= " + yearFromComboBox.getSelectedItem().toString() + " AND movies.ReleaseYear <= " + yearToComboBox.getSelectedItem().toString());
            finalQuery = select + from + where + constrain + year + "\nORDER BY movies.Title";
        }

        private String show(){
            return finalQuery;
        }

    }

    //final query to get target user data
    private class FinalUserQueryGroup{
        private final String select = "SELECT DISTINCT user_taggedmovies.USID\n";
        private final String from = "FROM user_taggedmovies\n";
        private final String where = "WHERE user_taggedmovies.MID in (";
        private final String and = ")\nAND user_taggedmovies.TID in (";
        private String mid = "";
        private String tid = "";
        private String finalQuery = "";
        
        private void make(){
            mid = "";
            tid = "";
            finalQuery = "";
            if (checkedMovie.isEmpty() || checkedTagCheckBox.isEmpty()){
                finalQuery = "PLEASE SELECT ITEMS IN BOTH MOVIE TABLE AND TAG LIST!!!";
            }
            else{
                for (String s : checkedMovie){
                    mid += s + ", ";
                }
                mid = mid.substring(0, mid.length() - 2);
                for (JCheckBox c : checkedTagCheckBox){
                    String[] temp  = c.getText().split(" ");
                    tid += temp[0] + ", ";
                }
                tid = tid.substring(0, tid.length() - 2);
                finalQuery = select + from + where + mid + and + tid + ")";
            }
        }
        
        private String show(){
            return finalQuery;
        }
    }

    /**
     * Creates new form hw3
     */
    public Hw3() {
        //connect to database
        connection = Populate.guiToDatabaseConnect();
        //build GUI
        initComponents();
        //get data from database
        initialGenres();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        genrePanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        yearFromComboBox = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        yearToComboBox = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        countryPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        actorComboBox1 = new javax.swing.JComboBox<>();
        actorComboBox2 = new javax.swing.JComboBox<>();
        actorComboBox3 = new javax.swing.JComboBox<>();
        actorComboBox4 = new javax.swing.JComboBox<>();
        jPanel7 = new javax.swing.JPanel();
        directorComboBox = new javax.swing.JComboBox<>();
        jPanel8 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        tagWeightComboBox = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        tagWeightTextField = new javax.swing.JTextField();
        jScrollPane8 = new javax.swing.JScrollPane();
        tagPanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        queryText = new javax.swing.JTextArea();
        jLabel11 = new javax.swing.JLabel();
        logicalComboBox = new javax.swing.JComboBox<>();
        userQueryButton = new javax.swing.JButton();
        movieQueryButton = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        movieTable = new javax.swing.JTable();
        jPanel12 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        usersTextArea = new javax.swing.JTextArea();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        internalQueryText = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IMDB MOVIE DATA QUERY SYSTEM");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Movie Attributes");
        jLabel1.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel2.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Genres");
        jLabel2.setToolTipText("");

        javax.swing.GroupLayout genrePanelLayout = new javax.swing.GroupLayout(genrePanel);
        genrePanel.setLayout(genrePanelLayout);
        genrePanelLayout.setHorizontalGroup(
            genrePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 159, Short.MAX_VALUE)
        );
        genrePanelLayout.setVerticalGroup(
            genrePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 169, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(genrePanel);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                    .addGap(0, 37, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel4.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel4.setText("From");

        jLabel8.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel8.setText("To");

        jLabel3.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Movie Year");
        jLabel3.setToolTipText("");
        jLabel3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(yearFromComboBox, 0, 91, Short.MAX_VALUE)
                    .addComponent(yearToComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(yearFromComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(yearToComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 17, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel7.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Country");
        jLabel7.setToolTipText("");

        countryPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        javax.swing.GroupLayout countryPanelLayout = new javax.swing.GroupLayout(countryPanel);
        countryPanel.setLayout(countryPanelLayout);
        countryPanelLayout.setHorizontalGroup(
            countryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 156, Short.MAX_VALUE)
        );
        countryPanelLayout.setVerticalGroup(
            countryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 286, Short.MAX_VALUE)
        );

        jScrollPane3.setViewportView(countryPanel);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                    .addGap(0, 37, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel6.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Casts");
        jLabel6.setToolTipText("");
        jLabel6.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Actor/Actress", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("BankGothic Lt BT", 0, 12))); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(actorComboBox1, 0, 126, Short.MAX_VALUE)
                    .addComponent(actorComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(actorComboBox3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(actorComboBox4, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(actorComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(actorComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(actorComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(actorComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Director", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("BankGothic Lt BT", 0, 12))); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(directorComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(directorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel5.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Tag Ids and Values");
        jLabel5.setToolTipText("");

        jLabel9.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel9.setText("Tag Weight");

        tagWeightComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", ">", ">=", "<", "<=" }));
        tagWeightComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagWeightComboBoxActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel10.setText("Value");

        tagWeightTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tagWeightTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagWeightTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tagPanelLayout = new javax.swing.GroupLayout(tagPanel);
        tagPanel.setLayout(tagPanelLayout);
        tagPanelLayout.setHorizontalGroup(
            tagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 165, Short.MAX_VALUE)
        );
        tagPanelLayout.setVerticalGroup(
            tagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 219, Short.MAX_VALUE)
        );

        jScrollPane8.setViewportView(tagPanel);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tagWeightTextField)
                    .addComponent(tagWeightComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jScrollPane8)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(tagWeightComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(tagWeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(14, 14, 14))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Query", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("BankGothic Lt BT", 0, 12))); // NOI18N

        queryText.setEditable(false);
        queryText.setColumns(20);
        queryText.setRows(5);
        jScrollPane4.setViewportView(queryText);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4)
                .addContainerGap())
        );

        jLabel11.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel11.setText("Logic Value");

        logicalComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AND", "OR" }));
        logicalComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logicalComboBoxActionPerformed(evt);
            }
        });

        userQueryButton.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        userQueryButton.setText("Execute User Query");
        userQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userQueryButtonActionPerformed(evt);
            }
        });

        movieQueryButton.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        movieQueryButton.setText("Execute Movie Query");
        movieQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                movieQueryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                        .addGap(0, 506, Short.MAX_VALUE)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(logicalComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                                .addComponent(movieQueryButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(userQueryButton)))))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(logicalComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userQueryButton)
                    .addComponent(movieQueryButton))
                .addContainerGap())
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel12.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Movie Searching Result");

        movieTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "MID", "Title", "Genre", "ReleaseYear", "Country", "RT Rating", "RT Rating Number"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Double.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(movieTable);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane5)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel13.setFont(new java.awt.Font("BankGothic Lt BT", 0, 12)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("User Searching Result");

        usersTextArea.setEditable(false);
        usersTextArea.setColumns(20);
        usersTextArea.setRows(5);
        jScrollPane1.setViewportView(usersTextArea);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1))
        );

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "InternalQuery", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("BankGothic Lt BT", 0, 12))); // NOI18N

        internalQueryText.setEditable(false);
        internalQueryText.setColumns(20);
        internalQueryText.setRows(5);
        jScrollPane7.setViewportView(internalQueryText);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7)
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tagWeightComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagWeightComboBoxActionPerformed
        // TODO add your handling code here:
        finalQueryGroup.make();
        queryText.setText(finalQueryGroup.show());
    }//GEN-LAST:event_tagWeightComboBoxActionPerformed

    private void tagWeightTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagWeightTextFieldActionPerformed
        // TODO add your handling code here:
        finalQueryGroup.make();
        queryText.setText(finalQueryGroup.show());
    }//GEN-LAST:event_tagWeightTextFieldActionPerformed

    private void logicalComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logicalComboBoxActionPerformed
        // TODO add your handling code here:
        logicRelation = logicalComboBox.getSelectedItem().toString();
        if (!checkedGenreCheckBox.isEmpty()){
            String[] strings = initialReleaseYears();
            initialOthers(strings[0], strings[1]);
            finalQueryGroup.make();
            queryText.setText(finalQueryGroup.show());
        }
    }//GEN-LAST:event_logicalComboBoxActionPerformed

    private void movieQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_movieQueryButtonActionPerformed
        // TODO add your handling code here:
        try{
            //reset the table
            checkedMovie.clear();
            DefaultTableModel model = (DefaultTableModel) movieTable.getModel();
            model.removeTableModelListener(tableModelListener);
            model.setRowCount(0);
            //build the query
            finalQueryGroup.make();
            queryText.setText(finalQueryGroup.show());
            //querying
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(finalQueryGroup.finalQuery);
            //insert data to table
            while (resultSet.next()) {
                model.addRow(new Object[] {false, resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getInt(4), resultSet.getString(5), resultSet.getDouble(6), resultSet.getInt(7)});
            }
            model.addTableModelListener(tableModelListener);
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_movieQueryButtonActionPerformed

    private void userQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userQueryButtonActionPerformed
        // TODO add your handling code here:
        try{
            //reset the text area
            usersTextArea.setText("");
            Statement statement = connection.createStatement();
            //invalid query
            if (!finalUserQueryGroup.finalQuery.startsWith("SELECT")){
                usersTextArea.setText("INVALID QUERY!!!");
                return;
            }
            //valid query
            ResultSet resultSet = statement.executeQuery(finalUserQueryGroup.finalQuery);
            //insert data
            while (resultSet.next()) {
                usersTextArea.append(resultSet.getString(1));
                usersTextArea.append("\n");
            }
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_userQueryButtonActionPerformed

    //close connection on exit
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        Populate.guiToDatabaseDisconnect(connection);
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Hw3().setVisible(true);
            }
        });
    }

    private void initialGenres(){
        try{
            query = "SELECT DISTINCT Genre\nFROM movie_genres\nORDER BY Genre";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            internalQueryText.setText(query);
            genrePanel.setLayout(new GridLayout(0,1));
            //insert genres as checkboxes
            while (resultSet.next()){
                JCheckBox genreCheckBox = new JCheckBox();
                genreCheckBox.setText(resultSet.getString(1));
                genrePanel.add(genreCheckBox);
                genreCheckBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (genreCheckBox.isSelected()) {
                            checkedGenreCheckBox.add(genreCheckBox);
                        }
                        else {
                            checkedGenreCheckBox.remove(genreCheckBox);
                        }
                        //get corresponding years fit selected genres
                        String[] strings = initialReleaseYears();
                        //initial data of country, casts and tags based on selected years and genres
                        initialOthers(strings[0], strings[1]);
                        finalQueryGroup.make();
                        queryText.setText(finalQueryGroup.show());
                    }
                });
            }
            resultSet.close();
            statement.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //initial releaseyears based on selected genres
    private String[] initialReleaseYears(){
        //build query
        releaseYearQueryGroup.yearsQuery= releaseYearQueryGroup.query1 + releaseYearQueryGroup.query2 + (checkedGenreCheckBox.isEmpty() ? "" : checkedGenreCheckBox.get(0).getText()) + releaseYearQueryGroup.query3;
        if (checkedGenreCheckBox.size() <= 1){
            releaseYearQueryGroup.yearsQuery += releaseYearQueryGroup.query4;
        }
        else {
            for (int i = 1; i < checkedGenreCheckBox.size(); i++) {
                String temp = (logicRelation == "AND" ? "\nINTERSECT\n" : "\nUNION\n") + releaseYearQueryGroup.query2 + checkedGenreCheckBox.get(i).getText() + releaseYearQueryGroup.query3;
                releaseYearQueryGroup.yearsQuery += temp;
            }
            releaseYearQueryGroup.yearsQuery += releaseYearQueryGroup.query4;
        }
        releaseYearQueryGroup.yearsQuery += "\nORDER BY movies.ReleaseYear";
        //querying
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(releaseYearQueryGroup.yearsQuery);
            internalQueryText.setText(releaseYearQueryGroup.yearsQuery);
           //reset comboboxes of releaseyear
            yearFromComboBox.removeActionListener(releaseYearActionListener);
            yearToComboBox.removeActionListener(releaseYearActionListener);
            yearFromComboBox.removeAllItems();
            yearToComboBox.removeAllItems();
            while (resultSet.next()) {
                yearFromComboBox.addItem(resultSet.getString(1));
                yearToComboBox.addItem(resultSet.getString(1));
            }
            if (yearFromComboBox.getItemCount() == 0){
                return new String[] {"-1", "-1"};
            }
            yearFromComboBox.setSelectedIndex(0);
            yearToComboBox.setSelectedIndex(yearToComboBox.getItemCount() - 1);
            yearFromComboBox.addActionListener(releaseYearActionListener);
            yearToComboBox.addActionListener(releaseYearActionListener);
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{yearFromComboBox.getSelectedItem().toString(),yearToComboBox.getSelectedItem().toString()};
    }

    //initial data of countries, casts and tags based on selected years and genres
    private void initialOthers(String yearFrom, String yearTo){
        //reset panels of countries casts and tags.
        checkedCountryCheckBox.clear();
        countryPanel.removeAll();
        countryPanel.revalidate();
        countryPanel.repaint();
        //if no data fits selected genres
        if (yearFrom.equals("-1")){
            actorComboBox1.removeActionListener(actorActionListener);
            actorComboBox1.removeAllItems();
            actorComboBox1.addItem("");
            actorComboBox1.setSelectedIndex(actorComboBox1.getItemCount() - 1);
            actorComboBox2.removeActionListener(actorActionListener);
            actorComboBox2.removeAllItems();
            actorComboBox2.addItem("");
            actorComboBox2.setSelectedIndex(actorComboBox1.getItemCount() - 1);
            actorComboBox3.removeActionListener(actorActionListener);
            actorComboBox3.removeAllItems();
            actorComboBox3.addItem("");
            actorComboBox3.setSelectedIndex(actorComboBox1.getItemCount() - 1);
            actorComboBox4.removeActionListener(actorActionListener);
            actorComboBox4.removeAllItems();
            actorComboBox4.addItem("");
            actorComboBox4.setSelectedIndex(actorComboBox1.getItemCount() - 1);
            directorComboBox.removeActionListener(directorActionListener);
            directorComboBox.removeAllItems();
            directorComboBox.addItem("");
            directorComboBox.setSelectedIndex(directorComboBox.getItemCount() - 1);
            checkedTagCheckBox.clear();
            tagPanel.removeAll();
            tagPanel.revalidate();
            tagPanel.repaint();
            return;
        }
        //build query
        countriesQueryGroup.countriesQuery= countriesQueryGroup.query1 + countriesQueryGroup.query2 + (checkedGenreCheckBox.isEmpty() ? "" : checkedGenreCheckBox.get(0).getText()) + countriesQueryGroup.query3;
        if (checkedGenreCheckBox.size() <= 1){
            countriesQueryGroup.countriesQuery += countriesQueryGroup.query4;
        }
        else {
            for (int i = 1; i < checkedGenreCheckBox.size(); i++) {
                String temp = (logicRelation == "AND" ? "\nINTERSECT\n" : "\nUNION\n") + countriesQueryGroup.query2 + checkedGenreCheckBox.get(i).getText() + countriesQueryGroup.query3;
                countriesQueryGroup.countriesQuery += temp;
            }
            countriesQueryGroup.countriesQuery += countriesQueryGroup.query4;
        }
        countriesQueryGroup.countriesQuery += "\nAND movies.ReleaseYear >= " + yearFrom + " AND movies.ReleaseYear <= " + yearTo + "\nORDER BY movie_countries.Country";
        try{
            //reset country panel
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            ResultSet resultSet = statement.executeQuery(countriesQueryGroup.countriesQuery);
            countryPanel.setLayout(new GridLayout(0,1));
            while (resultSet.next()){
                JCheckBox countryCheckBox = new JCheckBox();
                countryCheckBox.setText(resultSet.getString(1));
                countryPanel.add(countryCheckBox);
                countryCheckBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (countryCheckBox.isSelected()) {
                            checkedCountryCheckBox.add(countryCheckBox);
                        }
                        else {
                            checkedCountryCheckBox.remove(countryCheckBox);
                        }
                        //when certain country is selected, update following panels
                        countryToCastTag();
                        finalQueryGroup.make();
                        queryText.setText(finalQueryGroup.show());
                    }
                });
            }
            //update casts panel
            actorsQueryGroup.actorsQuery = countriesQueryGroup.countriesQuery.replaceAll("movie_countries", "movie_actors").replaceAll("Country", "ActorName");
            resultSet = statement.executeQuery(actorsQueryGroup.actorsQuery);
            updateActorComboBox(resultSet,actorComboBox1,actorActionListener);
            updateActorComboBox(resultSet,actorComboBox2,actorActionListener);
            updateActorComboBox(resultSet,actorComboBox3,actorActionListener);
            updateActorComboBox(resultSet,actorComboBox4,actorActionListener);
            directorsQueryGroup.directorsQuery = actorsQueryGroup.actorsQuery.replaceAll("movie_actors", "movie_directors").replaceAll("ActorName", "DirectorName");
            resultSet = statement.executeQuery(directorsQueryGroup.directorsQuery);
            updateActorComboBox(resultSet,directorComboBox,directorActionListener);
            //update tag panel
            tagsQueryGroup.tagsQuery = directorsQueryGroup.directorsQuery.replaceAll("movie_directors.DirectorName", "tags.TID, tags.TagText").replaceAll("movie_directors.MID = movies.MID", "movie_tags.MID = movies.MID AND movie_tags.TID = tags.TID").replaceAll("movie_directors", "tags, movie_tags");
            resultSet = statement.executeQuery(tagsQueryGroup.tagsQuery);
            updateTagPanel(resultSet);
            internalQueryText.setText(countriesQueryGroup.countriesQuery + "\n" + "\n" + actorsQueryGroup.actorsQuery + "\n" + "\n" + directorsQueryGroup.directorsQuery + "\n" + "\n" + tagsQueryGroup.tagsQuery);
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //initial data of casts and tags based on selected years, genres and countries
    private void countryToCastTag(){
        //build queries
        countriesToCastsTagsQueryGroup.countriesToActorsQuery = actorsQueryGroup.actorsQuery;
        countriesToCastsTagsQueryGroup.countriesToDirectorsQuery = directorsQueryGroup.directorsQuery;
        countriesToCastsTagsQueryGroup.countriesToTagsQuery = tagsQueryGroup.tagsQuery;
        if (!checkedCountryCheckBox.isEmpty()){
            String temp = "\nINTERSECT\n(" + countriesToCastsTagsQueryGroup.query1 + checkedCountryCheckBox.get(0).getText() + countriesToCastsTagsQueryGroup.query2;
            for (int i = 1; i < checkedCountryCheckBox.size(); i++){
                temp += (logicRelation == "AND" ? "\nINTERSECT\n" : "\nUNION\n") + countriesToCastsTagsQueryGroup.query1 + checkedCountryCheckBox.get(i).getText() + countriesToCastsTagsQueryGroup.query2;
            }
            temp += countriesToCastsTagsQueryGroup.query3 + countriesToCastsTagsQueryGroup.query3;
            countriesToCastsTagsQueryGroup.countriesToActorsQuery = countriesToCastsTagsQueryGroup.countriesToActorsQuery.replace(")", temp);
            countriesToCastsTagsQueryGroup.countriesToDirectorsQuery = countriesToCastsTagsQueryGroup.countriesToDirectorsQuery.replace(")", temp);
            countriesToCastsTagsQueryGroup.countriesToTagsQuery = countriesToCastsTagsQueryGroup.countriesToTagsQuery.replace(")", temp);
        }
        internalQueryText.setText(countriesToCastsTagsQueryGroup.countriesToActorsQuery + "\n" + "\n" + countriesToCastsTagsQueryGroup.countriesToDirectorsQuery + "\n" + "\n" + countriesToCastsTagsQueryGroup.countriesToTagsQuery);
        try{
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            //update casts panel
            ResultSet resultSet = statement.executeQuery(countriesToCastsTagsQueryGroup.countriesToActorsQuery);
            updateActorComboBox(resultSet, actorComboBox1, actorActionListener);
            updateActorComboBox(resultSet, actorComboBox2, actorActionListener);
            updateActorComboBox(resultSet, actorComboBox3, actorActionListener);
            updateActorComboBox(resultSet, actorComboBox4, actorActionListener);
            resultSet = statement.executeQuery(countriesToCastsTagsQueryGroup.countriesToDirectorsQuery);
            updateActorComboBox(resultSet, directorComboBox, directorActionListener);
            //update tag panel
            resultSet = statement.executeQuery(countriesToCastsTagsQueryGroup.countriesToTagsQuery);
            updateTagPanel(resultSet);
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //initial data of directors and tags based on selected years, genres, countries and actors
    private void actorToDirectorTag(){
        //build queries
        actorsToDirectorsTagsQueryGroup.actorsToDirectorsQuery = checkedCountryCheckBox.isEmpty() ? directorsQueryGroup.directorsQuery : countriesToCastsTagsQueryGroup.countriesToDirectorsQuery;
        actorsToDirectorsTagsQueryGroup.actorsToTagsQuery = checkedCountryCheckBox.isEmpty() ? tagsQueryGroup.tagsQuery : countriesToCastsTagsQueryGroup.countriesToTagsQuery;
        //check what items are selected and kill duplicates
        Set<String> set = new HashSet<>();
        set.add(actorComboBox1.getSelectedItem().toString());
        set.add(actorComboBox2.getSelectedItem().toString());
        set.add(actorComboBox3.getSelectedItem().toString());
        set.add(actorComboBox4.getSelectedItem().toString());
        set.remove("");
        if (set.size() > 0){
            List<String> list = new ArrayList<>(set);
            String temp = "\nINTERSECT\n(" + actorsToDirectorsTagsQueryGroup.query1 + list.get(0) + actorsToDirectorsTagsQueryGroup.query2;
            for (int i = 1; i < list.size(); i++){
                temp += (logicRelation == "AND" ? "\nINTERSECT\n" : "\nUNION\n") + actorsToDirectorsTagsQueryGroup.query1 + list.get(i) + actorsToDirectorsTagsQueryGroup.query2;
            }
            temp += actorsToDirectorsTagsQueryGroup.query3 + actorsToDirectorsTagsQueryGroup.query3 + "\nAND";
            actorsToDirectorsTagsQueryGroup.actorsToDirectorsQuery = actorsToDirectorsTagsQueryGroup.actorsToDirectorsQuery.replace(")\nAND",temp);
            actorsToDirectorsTagsQueryGroup.actorsToTagsQuery =actorsToDirectorsTagsQueryGroup.actorsToTagsQuery.replace(")\nAND",temp);
        }
        try{
            //reset director panel and tag panel
            directorComboBox.removeActionListener(directorActionListener);
            directorComboBox.removeAllItems();
            tagPanel.removeAll();
            tagPanel.revalidate();
            tagPanel.repaint();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            ResultSet resultSet = statement.executeQuery(actorsToDirectorsTagsQueryGroup.actorsToDirectorsQuery);
            //update director panle
            updateActorComboBox(resultSet, directorComboBox, directorActionListener);
            resultSet = statement.executeQuery(actorsToDirectorsTagsQueryGroup.actorsToTagsQuery);
            //update tag panel
            updateTagPanel(resultSet);
            internalQueryText.setText(actorsToDirectorsTagsQueryGroup.actorsToDirectorsQuery + "\n" + "\n" + actorsToDirectorsTagsQueryGroup.actorsToTagsQuery);
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //initial data of tags based on selected years, genres, countries and casts
    private void directorToTag(){
        //build query
        boolean noActorComboBoxChecked = actorComboBox1.getSelectedItem().toString().equals("") && actorComboBox2.getSelectedItem().toString().equals("") && actorComboBox3.getSelectedItem().toString().equals("") && actorComboBox4.getSelectedItem().toString().equals("");
        if (checkedCountryCheckBox.isEmpty() && noActorComboBoxChecked){
            directorsToTagsQueryGroup.directorsToTagsQuery = tagsQueryGroup.tagsQuery;
        }
        else if (!checkedCountryCheckBox.isEmpty() && noActorComboBoxChecked){
            directorsToTagsQueryGroup.directorsToTagsQuery = countriesToCastsTagsQueryGroup.countriesToTagsQuery;
        }
        else{
            directorsToTagsQueryGroup.directorsToTagsQuery = actorsToDirectorsTagsQueryGroup.actorsToTagsQuery;
        }
        if (!directorComboBox.getSelectedItem().toString().equals("")){
            String temp = "\nINTERSECT\n" + directorsToTagsQueryGroup.query1 + directorComboBox.getSelectedItem().toString() + actorsToDirectorsTagsQueryGroup.query2 + directorsToTagsQueryGroup.query3 + "\nAND";
            directorsToTagsQueryGroup.directorsToTagsQuery = directorsToTagsQueryGroup.directorsToTagsQuery.replace(")\nAND",temp);
        }
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(directorsToTagsQueryGroup.directorsToTagsQuery);
            updateTagPanel(resultSet);
            internalQueryText.setText(directorsToTagsQueryGroup.directorsToTagsQuery);
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //method to reset actor combox and director combobox
    private void updateActorComboBox(ResultSet resultSet,JComboBox<String> comboBox, ActionListener actionListener) throws Exception{
        comboBox.removeActionListener(actionListener);
        comboBox.removeAllItems();
        while (resultSet.next()) {
            comboBox.addItem(resultSet.getString(1));
        }
        comboBox.addItem("");
        comboBox.setSelectedIndex(comboBox.getItemCount() - 1);
        comboBox.addActionListener(actionListener);
        resultSet.beforeFirst();
    }

    //method to reset tag panel
    private void updateTagPanel(ResultSet resultSet) throws Exception{
        checkedTagCheckBox.clear();
        tagPanel.removeAll();
        tagPanel.revalidate();
        tagPanel.repaint();
        tagPanel.setLayout(new GridLayout(0,1));
        while (resultSet.next()){
            JCheckBox tagCheckBox = new JCheckBox();
            tagCheckBox.setText(resultSet.getString(1) + " " + resultSet.getString(2));
            tagPanel.add(tagCheckBox);
            tagCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (tagCheckBox.isSelected()) {
                        checkedTagCheckBox.add(tagCheckBox);
                    }
                    else {
                        checkedTagCheckBox.remove(tagCheckBox);
                    }
                    finalQueryGroup.make();
                    queryText.setText(finalQueryGroup.show());
                }
            });
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> actorComboBox1;
    private javax.swing.JComboBox<String> actorComboBox2;
    private javax.swing.JComboBox<String> actorComboBox3;
    private javax.swing.JComboBox<String> actorComboBox4;
    private javax.swing.JPanel countryPanel;
    private javax.swing.JComboBox<String> directorComboBox;
    private javax.swing.JPanel genrePanel;
    private javax.swing.JTextArea internalQueryText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JComboBox<String> logicalComboBox;
    private javax.swing.JButton movieQueryButton;
    private javax.swing.JTable movieTable;
    private javax.swing.JTextArea queryText;
    private javax.swing.JPanel tagPanel;
    private javax.swing.JComboBox<String> tagWeightComboBox;
    private javax.swing.JTextField tagWeightTextField;
    private javax.swing.JButton userQueryButton;
    private javax.swing.JTextArea usersTextArea;
    private javax.swing.JComboBox<String> yearFromComboBox;
    private javax.swing.JComboBox<String> yearToComboBox;
    // End of variables declaration//GEN-END:variables
}
