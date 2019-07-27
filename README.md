# MovieArchiveFixer
A simple Java application to tidy up my friend's movie archive.

## The story behind
A friend of mine had a movie archive of more than 2000 movies, but the movie folders were not well-named.
Folder names were like this:
- A Streetcar Named Desire (1951)
- Strangers_on_a_Train
- The Matrix (دوبله)
- Apocalypse_Now_1979

He wanted all direcory names to be like this:
- 1951 - Strangers on a Train

So, I wrote a command-line application to take care of this. it extracts the possible movie name from directory name,
searches Google for it, finds the movie's full name and the production year and at last, rename the folder. 

## Running the program
0. install JDK and Set Environment Variables
1. clone the project
2. open cmd and cd to project's folder
3. compile the source code, using:
```
javac -cp "The_Path_To_Project_Directory\dependencies\jsoup-1.12.1.jar;" src\Main.java
```
4. cd to src folder, using:
```
cd src
```
5. run the program, using:
```
java -cp "The_Path_To_Project_Directory\dependencies\jsoup-1.12.1.jar;" Main
```
! remember to replace The_Path_To_Project_Directory.

## Built with:
* [Java](https://www.java.com/en/)
* [jsoup](https://jsoup.org) - The package used to parse HTML

## Authors
* **Hossein Zaredar** - [HosseinZaredar](https://github.com/HosseinZaredar)
