import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Created by xiaor on 2017/5/23.
 */
public class Populate {

    public static void main(String[] argv){
        JavaToDatabase javaToDatabase = new JavaToDatabase();
        Connection connection = javaToDatabase.newConnection();
        PopulateData populateData = new PopulateData(connection);
        populateData.populate(argv);
        javaToDatabase.closeConnection(connection);
    }
    
    //api for external to connect to database
    public static Connection guiToDatabaseConnect(){
       return JavaToDatabase.newConnection();
    }
    
    //api for external to disconnect from database
    public static void guiToDatabaseDisconnect(Connection connection){
        JavaToDatabase.closeConnection(connection);
    }

    //manage connection from java to oracle
    private static class JavaToDatabase{

        private static Connection newConnection(){
            Connection connection = null;
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "SYSTEM", "123");
                System.out.println("Connection Built");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return connection;
        }

        private static void closeConnection (Connection connection){
            try{
                connection.close();
                System.out.println("Connection Closed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //populate data to oracle
    private static class PopulateData {

        private Connection connection;

        private PopulateData (Connection connection){
            this.connection = connection;
        }

        //populate data to movies table
        private void populateMovies() throws Exception {
            System.out.println("Inserting Data into Movies...");
            DataInputStream dataInputStream = new DataInputStream(PopulateData.class.getResourceAsStream("data/movies.dat"));
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String string = bufferedReader.readLine();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO movies VALUES (?,?,?,?,?)");
            int count = 1;
            while ((string = bufferedReader.readLine()) != null) {
                String[] strings = string.trim().split("\t");
                preparedStatement.setString(1, strings[0]);
                preparedStatement.setString(2, strings[1]);
                preparedStatement.setInt(3, Integer.parseInt(strings[5]));
                if (strings[17].equals("\\N")){
                    preparedStatement.setDouble(4, 0);
                }
                else {
                    preparedStatement.setDouble(4, Double.parseDouble(strings[17]));
                }
                if (strings[18].equals("\\N")){
                    preparedStatement.setInt(5, 0);
                }
                else {
                    preparedStatement.setInt(5, Integer.parseInt(strings[18]));
                }
                preparedStatement.executeUpdate();
                System.out.println("Movies > Processing Line: " + (count++) + "...");
            }
            preparedStatement.close();
            System.out.println("Inserting Data into Movies...FINISH!");
        }

        //populate data to movie_genres table
        private void populateMovieGenres() throws Exception {
            System.out.println("Inserting Data into Movie_Genres...");
            DataInputStream dataInputStream = new DataInputStream(PopulateData.class.getResourceAsStream("data/movie_genres.dat"));
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String string = bufferedReader.readLine();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO movie_genres VALUES (?,?)");
            int count = 1;
            while ((string = bufferedReader.readLine()) != null) {
                String[] strings = string.trim().split("\t");
                preparedStatement.setString(1, strings[0]);
                preparedStatement.setString(2, strings[1]);
                preparedStatement.executeUpdate();
                System.out.println("Movie_Genres > Processing Line: " + (count++) + "...");
            }
            preparedStatement.close();
            System.out.println("Inserting Data into Movie_Genres...FINISH!");
        }

        //populate data to movie_directors table
        private void populateMovieDirectors() throws Exception {
            System.out.println("Inserting Data into Movie_Directors...");
            DataInputStream dataInputStream = new DataInputStream(PopulateData.class.getResourceAsStream("data/movie_directors.dat"));
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String string = bufferedReader.readLine();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO movie_directors VALUES (?,?,?)");
            int count = 1;
            while ((string = bufferedReader.readLine()) != null) {
                String[] strings = string.trim().split("\t");
                preparedStatement.setString(1, strings[0]);
                preparedStatement.setString(2, strings[1]);
                preparedStatement.setString(3, strings[2]);
                preparedStatement.executeUpdate();
                System.out.println("Movie_Directors > Processing Line: " + (count++) + "...");
            }
            preparedStatement.close();
            System.out.println("Inserting Data into Movie_Directors...FINISH!");
        }

        //populate data to movie_actors table
        private void populateMovieActors() throws Exception {
            System.out.println("Inserting Data into Movie_Actors...");
            DataInputStream dataInputStream = new DataInputStream(PopulateData.class.getResourceAsStream("data/movie_actors.dat"));
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String string = bufferedReader.readLine();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO movie_actors VALUES (?,?,?)");
            int count = 1;
            while ((string = bufferedReader.readLine()) != null) {
                String[] strings = string.trim().split("\t");
                preparedStatement.setString(1, strings[0]);
                preparedStatement.setString(2, strings[1]);
                if (strings[2].isEmpty()){
                    preparedStatement.setString(3, "NULL");
                }
                else{
                    preparedStatement.setString(3, strings[2]);
                }
                preparedStatement.executeUpdate();
                System.out.println("Movie_Actors > Processing Line: " + (count++) + "...");
            }
            preparedStatement.close();
            System.out.println("Inserting Data into Movie_Actors...FINISH!");
        }

        //populate data to movie_countries table
        private void populateMovieCountries() throws Exception {
            System.out.println("Inserting Data into Movie_Countries...");
            DataInputStream dataInputStream = new DataInputStream(PopulateData.class.getResourceAsStream("data/movie_countries.dat"));
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String string = bufferedReader.readLine();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO movie_countries VALUES (?,?)");
            int count = 1;
            while ((string = bufferedReader.readLine()) != null) {
                String[] strings = string.trim().split("\t");
                preparedStatement.setString(1, strings[0]);
                if (strings.length < 2){
                    preparedStatement.setString(2, "NULL");
                }
                else {
                    preparedStatement.setString(2, strings[1]);
                }

                preparedStatement.executeUpdate();
                System.out.println("Movie_Countries > Processing Line: " + (count++) + "...");
            }
            preparedStatement.close();
            System.out.println("Inserting Data into Movie_Countries...FINISH!");
        }

        //populate data to tags table
        private void populateTags() throws Exception {
            System.out.println("Inserting Data into Tags...");
            DataInputStream dataInputStream = new DataInputStream(PopulateData.class.getResourceAsStream("data/tags.dat"));
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String string = bufferedReader.readLine();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tags VALUES (?,?)");
            int count = 1;
            while ((string = bufferedReader.readLine()) != null) {
                String[] strings = string.trim().split("\t");
                preparedStatement.setString(1, strings[0]);
                preparedStatement.setString(2, strings[1]);
                preparedStatement.executeUpdate();
                System.out.println("Tags > Processing Line: " + (count++) + "...");
            }
            preparedStatement.close();
            System.out.println("Inserting Data into Tags...FINISH!");
        }

        //populate data to movie_tags table
        private void populateMovieTags() throws Exception {
            System.out.println("Inserting Data into Movie_Tags...");
            DataInputStream dataInputStream = new DataInputStream(PopulateData.class.getResourceAsStream("data/movie_tags.dat"));
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String string = bufferedReader.readLine();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO movie_tags VALUES (?,?,?)");
            int count = 1;
            while ((string = bufferedReader.readLine()) != null) {
                String[] strings = string.trim().split("\t");
                preparedStatement.setString(1, strings[0]);
                preparedStatement.setString(2, strings[1]);
                preparedStatement.setInt(3, Integer.parseInt(strings[2]));
                preparedStatement.executeUpdate();
                System.out.println("Movie_Tags > Processing Line: " + (count++) + "...");
            }
            preparedStatement.close();
            System.out.println("Inserting Data into Movie_Tags...FINISH!");
        }

        //populate data to user_taggedmovies table
        private void populateUserTaggedMovies() throws Exception {
            System.out.println("Inserting Data into User_TaggedMovies...");
            DataInputStream dataInputStream = new DataInputStream(PopulateData.class.getResourceAsStream("data/user_taggedmovies.dat"));
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String string = bufferedReader.readLine();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO user_taggedmovies VALUES (?,?,?)");
            int count = 1;
            while ((string = bufferedReader.readLine()) != null) {
                String[] strings = string.trim().split("\t");
                preparedStatement.setString(1, strings[0]);
                preparedStatement.setString(2, strings[1]);
                preparedStatement.setString(3, strings[2]);
                preparedStatement.executeUpdate();
                System.out.println("User_TaggedMovies > Processing Line: " + (count++) + "...");
            }
            preparedStatement.close();
            System.out.println("Inserting Data into User_TaggedMovies...FINISH!");
        }

        //clear one table
        private void deleteSingle(String tableName) throws Exception{
            System.out.println("Purging Data From " + tableName + " table...");
            Statement statement = connection.createStatement();
            statement.execute("DELETE FROM " + tableName);
            statement.close();
        }

        //clear all table
        private void deleteAll() throws Exception{
            System.out.println("Purging Data...");
            deleteSingle("movie_genres");
            deleteSingle("movie_directors");
            deleteSingle("movie_actors");
            deleteSingle("movie_countries");
            deleteSingle("movie_tags");
            deleteSingle("user_taggedmovies");
            deleteSingle("tags");
            deleteSingle("movies");
            System.out.println("Data Purged!");
        }

        //populate selected tables
        private void populate(String[] input){
            try{
                deleteAll();
                if (input.length < 1){
                    populateMovies();
                    populateTags();
                    populateMovieGenres();
                    populateMovieDirectors();
                    populateMovieActors();
                    populateMovieCountries();
                    populateMovieTags();
                    populateUserTaggedMovies();
                }
                else{
                    for (int i = 0; i < input.length; i++){
                        switch (input[i]){
                            case "movies.dat":
                                populateMovies();
                                break;
                            case "movie_genres.dat":
                                populateMovieGenres();
                                break;
                            case "movie_directors.dat":
                                populateMovieDirectors();
                                break;
                            case "movie_actors.dat":
                                populateMovieActors();
                                break;
                            case "movie_countries.dat":
                                populateMovieCountries();
                                break;
                            case "tags.dat":
                                populateTags();
                                break;
                            case "movie_tags.dat":
                                populateMovieTags();
                                break;
                            case "user_taggedmovies.dat":
                                populateUserTaggedMovies();
                                break;
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}