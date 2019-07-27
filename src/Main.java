import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final String GOOGLE_SEARCH_URL = "https://www.google.com/search";
    private static final int MAXIMUM_RETRY = 5;
    private static int allCount = 0;
    private static int doneCount = 0;

    /*
    the main function that the program starts at
     */
    public static void main(String[] args) {
        System.out.println("Enter the main directory:");
        Scanner scanner = new Scanner(System.in);
        String mainDir = scanner.next();
        fixDirectory(mainDir);
        System.out.println(doneCount + " from " + allCount + " Folders fixed!");
    }

    /*
    this function returns all directories inside the given directory
     */
    private static String[] getAllDirectories(String dir) {
        File file = new File(dir);
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
    }

    /*
    the main function that is called once on the main directory
     */
    private static void fixDirectory(String mainDir) {
        String[] movieDirs = getAllDirectories(mainDir);

        //finding the total number of movie directories
        allCount = movieDirs.length;

        //calling fixName on every movie directory
        for (String movieDir : movieDirs) {
            fixName(mainDir, movieDir);
        }
    }

    /*
    the main function called on every movie directory to fix it's name
     */
    private static void fixName(String mainDir, String movieDir) {

        //if directory name is ok, we skip it
        if (isOk(movieDir)) {
            //this directory is considered fixed
            doneCount++;
            return;
        }

        //finding the best approximation of movie's name
        String cleanName = cleanName(movieDir);

        //trying to search Google for MAXIMUM_RETRY times
        String res = null;
        for (int i = 0; i < MAXIMUM_RETRY; i++) {
            res = searchMovie(cleanName);
            if (res != null)
                break;
        }

        if (res == null) {
            System.out.println(mainDir + "\\" + movieDir + ": Search failed!");
        } else {
            //renaming the directory with the year-name format made from Google search
            renameFolder(mainDir, movieDir, res);
            doneCount++;
            System.out.println(mainDir + "\\" + movieDir + " -> " + res + ": Done!");
        }
    }

    /*
    a function to rename movie directories
     */
    private static void renameFolder(String mainDir, String oldName, String newName) {
        File folder = new File(mainDir + "\\" + oldName);
        File newFolder = new File(mainDir + "\\" + newName);
        boolean renamed = folder.renameTo(newFolder);
        if (!renamed) {
            System.out.println(mainDir + "\\" + oldName + ": Unable to rename!");
        }
    }

    /*
    this function is used to take the possible movie name out of movie's directory name
     */
    private static String cleanName(String movieDir) {

        //checking if movie year can be found from directory's name
        String year = null;
        String regEx = "[0-9]{4}";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(movieDir);
        if (matcher.find()){
            year = matcher.group();
        }

        //removing unnecessary characters
        String cleanedName =  movieDir.replaceAll("[0-9]{4}", "").replaceAll("[\u0627-\u0649]", "")
                .replaceAll("[()_-]", " ").replaceAll(" {2}", " ").trim();

        if (year == null)
            return cleanedName + " movie";
        else
            return cleanedName + " " + year + " movie";
    }


    /*
    this function checks if the movie's directory name is already in the wanted format
     */
    private static boolean isOk(String movieDir) {
        return movieDir.matches("[0-9]{4} - .+");
    }

    /*
    this function is used to search Google for the movie and return the movie name and year in the wanted format
     */
    private static String searchMovie(String movieTitle) {
        String searchURL = GOOGLE_SEARCH_URL + "?q=" + movieTitle + "&num=10";
        Document doc = null;
        try {
            //sending request to Google
            doc = Jsoup.connect(searchURL).userAgent("Mozilla/5.0").get();
        } catch (IOException e) {
            return null;
        }

        //parsing HTML file to find the correct movie name and the year it is produced
        if (doc == null)
            return null;
        Elements nameElements = doc.select(".deIvCb");
        Elements yearElements = doc.select(".tAd8D");

        //checking if the needed elements are found correctly
        if (nameElements.size() == 0 || yearElements.size() == 0)
            return null;
        String name = nameElements.get(0).text();
        if (yearElements.get(0).text().length() < 4)
            return null;
        String year = yearElements.get(0).text().substring(0, 4);
        if (!year.matches("[0-9]{4}"))
            return null;

        //returning in the wanted format
        return (year + " - " + name).replaceAll(":", " -");
    }

}

