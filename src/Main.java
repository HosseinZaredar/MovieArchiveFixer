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
    private static int count = 0;

    public static void main(String[] args) {
        System.out.println("Enter the main directory:");
        Scanner scanner = new Scanner(System.in);
        String mainDir = scanner.next();
        fixDirectory(mainDir);
        System.out.println(count + " Folders fixed!");
    }

    private static String[] getAllDirectories(String dir) {
        File file = new File(dir);
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
    }

    private static void fixDirectory(String mainDir) {
        String[] movieDirs = getAllDirectories(mainDir);
        for (String movieDir : movieDirs) {
            fixName(mainDir, movieDir);
        }
    }

    private static void fixName(String mainDir, String movieDir) {
//        if (isOk(movieDir)) {
//            return;
//        }
        String cleanName = cleanName(movieDir);
        String res = null;

        for (int i = 0; i < MAXIMUM_RETRY; i++) {
            res = searchMovie(cleanName);
            if (res != null)
                break;
        }

        if (res == null) {
            System.out.println(mainDir + "\\" + movieDir + ": Search failed!");
        } else {
            renameFolder(mainDir, movieDir, res);
            count++;
            System.out.println(mainDir + "\\" + res + ": Done!");
        }
    }

    private static void renameFolder(String mainDir, String oldName, String newName) {
        File folder = new File(mainDir + "\\" + oldName);
        File newFolder = new File(mainDir + "\\" + newName);
        boolean renamed = folder.renameTo(newFolder);
        if (!renamed) {
            System.out.println(mainDir + "\\" + oldName + ": Unable to rename!");
        }
    }

    private static String cleanName(String movieDir) {
        String year = null;
        String regEx = "[0-9]{4}";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(movieDir);
        if (matcher.find()){
            year = matcher.group();
        }
        String correctedName =  movieDir.replaceAll("[0-9]{4}", "").replaceAll("[\u0627-\u0649]", "")
                .replaceAll("[()_-]", " ").replaceAll(" {2}", " ").trim();

        if (year == null)
            return correctedName + " movie";
        else
            return correctedName + " " + year + " movie";
    }


    private static boolean isOk(String movieDir) {
        return movieDir.matches("[0-9]{4} - .+");
    }

    private static String searchMovie(String movieTitle) {
        String searchURL = GOOGLE_SEARCH_URL + "?q=" + movieTitle + "&num=10";
        Document doc = null;
        try {
            doc = Jsoup.connect(searchURL).userAgent("Mozilla/5.0").get();
        } catch (IOException e) {
            return null;
        }

        if (doc == null)
            return null;

        Elements nameElements = doc.select(".deIvCb");
        Elements yearElements = doc.select(".tAd8D");

        if (nameElements.size() == 0 || yearElements.size() == 0)
            return null;

        String name = nameElements.get(0).text();
        if (yearElements.get(0).text().length() < 4)
            return null;
        String year = yearElements.get(0).text().substring(0, 4);
        if (!year.matches("[0-9]{4}"))
            return null;

        return (year + " - " + name).replaceAll(":", " -");
    }

}

