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
    private static int count = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String mainDir = scanner.next();
        System.out.println("Ok, wait for some time...");
        String[] directorDirs = getAllDirectories(mainDir);
        for (String directorDir : directorDirs) {
            fixDirectory(mainDir + "\\" + directorDir);
        }
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

    private static void fixDirectory(String directorDir) {
        String[] movieDirs = getAllDirectories(directorDir);

        for (String movieDir : movieDirs) {
            fixName(directorDir, movieDir);
            fixImages(directorDir + "\\" + movieDir);
        }
    }

    private static void fixImages(String movieDir) {

        File file = new File(movieDir);
        String[] images = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
            }
        });
        if (images == null || images.length ==0)
            return;
        for (int i = 0; i < images.length; i++) {
            String image = images[i];
            String format = image.substring(image.lastIndexOf('.'), image.length());
            File folder = new File(movieDir + '\\' + image);
            File newFolder = new File(movieDir + "\\" + (i + 1) + format);
            folder.renameTo(newFolder);
        }
    }

    private static void fixName(String directorDir, String movieDir) {
//        if (isOk(movieDir)) {
//            return;
//        }
        String correctedName = correctName(movieDir);
        String res = searchMovie(correctedName);
        if (res == null) {
            res = searchMovie(correctedName);
            if (res == null)
                System.out.println("Unable to find in Google: " + directorDir + "\\" + movieDir);
            else
                renameFolder(directorDir, movieDir, res);
        } else {
            renameFolder(directorDir, movieDir, res);
        }
    }

    private static void renameFolder(String directorDir, String movieDir, String res) {
        File folder = new File(directorDir + "\\" + movieDir);
        File newFolder = new File(directorDir + "\\" + res);
        boolean renamed = folder.renameTo(newFolder);
        if (!renamed) {
            System.out.println("Unable to rename: " + directorDir + "\\" + movieDir);
            return;
        }
        count();
    }

    private static void count() {
        count++;
        if (count % 100 == 0)
            System.out.println(count + " Folders fixed!");
    }

    private static String correctName(String movieDir) {
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
            e.printStackTrace();
        }
        if (doc == null)
            return null;

        Elements names = doc.select("div.FSP1Dd");
        Elements years = doc.select("div.Rlw09");
        if (names.size() == 0 || years.size() == 0)
            return null;

        String finalResult = years.get(0).text().substring(0, 4) + " - " + names.get(0).text();
        return finalResult.replaceAll(":", " -");
    }
}

