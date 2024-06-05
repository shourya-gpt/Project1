import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class CombineCSV {

  public static String combineCSVFiles() throws FileNotFoundException {
    // TODO fix for 150s
    String[][] subjects = new String[][] {{"artsandhumanities.html", "135"},
        {"businessandeconomics.html", "143"}, {"clinicalandhealth.html", "122"},
        {"computerscience.html", "124"}, {"education.html", "124"}, {"engineering.html", "141"},
        {"law.html", "65"}, {"lifesciences.html", "150"}, {"physicalsciences.html", "150"},
        {"psychology.html", "150"}, {"socialsciences.html", "150"}};

    Map<String, String[]> map = new HashMap<String, String[]>();
    int subNumber = 0;
    for (String[] subject : subjects) {
      Scanner scanner = new Scanner(new File(subject[0]));
      String text = "";
      while (scanner.hasNextLine()) {
        text += "\n" + scanner.nextLine();
      }
      Pattern htmlPattern = Pattern
          .compile("data-position=\"title\"\sdata-mz=\"\">([^<]+)</a><div\sclass=\"location\">");
      Matcher htmlMatcher = htmlPattern.matcher(text);
      int count = 1;
      while (htmlMatcher.find() && count < Integer.parseInt(subject[1])) {
        if (map.containsKey(htmlMatcher.group(1))) {
          String[] mappedArray = map.get(htmlMatcher.group(1));
          mappedArray[subNumber] = "" + count;
        } else {
          String[] mappedArray = new String[11];
          mappedArray[subNumber] = "" + count;
          map.put(htmlMatcher.group(1), mappedArray);
        }
        count++;
      }

      subNumber++;
      scanner.close();
    }

    // Hard-coded function calls to rectify discrepancies in key names
    String[] val;
    for (String[] keys : new String[][] {
        {"Penn State (Main campus)", "Pennsylvania State University-Main Campus"},
        {"William &amp; Mary", "William & Mary"},
        {"University of California, San Diego", "University of California-San Diego"},
        {"Arizona State University (Tempe)", "Arizona State University Campus Immersion"},
        {"University of Minnesota", "University of Minnesota-Twin Cities"},
        {"University of Hawai’i at Mānoa", "University of Hawaii at Manoa"},
        {"University of California, Riverside", "University of California-Riverside"},
        {"University of Nevada, Las Vegas", "University of Nevada-Las Vegas"},
        {"Louisiana State University",
            "Louis" + "iana State University and Agricultural & Mechanical College"},
        {"University of California, Irvine", "University of California-Irvine"},
        {"University of California, Los Angeles", "University of California-Los Angeles"},
        {"Indiana University", "Indiana University-Bloomington"},
        {"University of California, Merced", "University of California-Merced"},
        {"University of Washington", "University of Washington-Seattle Campus"},
        {"Ohio University (Main campus)", "Ohio University-Main Campus"},
        {"University of Pittsburgh-Pittsburgh campus",
            "University of Pittsburgh-Pittsburgh Campus"},
        {"University of Maryland, College Park", "University of Maryland-College Park"},
        {"University of Virginia (Main campus)", "University of Virginia-Main Campus"},
        {"Oregon Health and Science University", "Oregon Health & Science University"},
        {"University of Colorado Denver/Anschutz Medical Campus",
            "University of Colorado Denver/Anschutz Medical Campus"},
        {"Purdue University West Lafayette", "Purdue University-Main Campus"},
        {"Bowling Green State University", "Bowling Green State University-Main Campus"},
        {"Mizzou - University of Missouri", "University of Missouri-Columbia"},
        {"North Carolina State University", "North Carolina State University at Raleigh"},
        {"SUNY Binghamton University", "Binghamton University"},
        {"Northeastern University, US", "Northeastern University"},
        {"University of Texas Rio Grande Valley", "The University of Texas Rio Grande Valley"},
        {"New Mexico State University (Main campus)", "New Mexico State University-Main Campus"},
        {"University of Texas at El Paso", "The University of Texas at El Paso"},
        {"University of California, Santa Barbara", "University of California-Santa Barbara"},
        {"Columbia University", "Columbia University in the City of New York"},
        {"The University of Chicago", "University of Chicago"},
        {"Tulane University", "Tulane University of Louisiana"},
        {"University of Texas at Dallas", "The University of Texas at Dallas"},
        {"Rutgers University – New Brunswick", "Rutgers University-New Brunswick"},
        {"University of California, Berkeley", "University of California-Berkeley"},
        {"University of Illinois at Urbana-Champaign", "University of Illinois Urbana-Champaign"},
        {"The University of Tulsa", "University of Tulsa"},
        {"Colorado State University, Fort Collins", "Colorado State University-Fort Collins"},
        {"University of Massachusetts", "University of Massachusetts-Amherst"},
        {"University of Texas at Arlington", "The University of Texas at Arlington"},
        {"Texas A&amp;M University", "Texas A & M University-College Station"},
        {"University of California, Santa Cruz", "University of California-Santa Cruz"},
        {"University of California, Davis", "University of California-Davis"},
        {"University of Maryland, Baltimore County", "University of Maryland-Baltimore County"},
        {"University of Texas at Austin", "The University of Texas at Austin"},
        {"Oklahoma State University", "Oklahoma State University-Main Campus"},
        {"Georgia Institute of Technology", "Georgia Institute of Technology-Main Campus"},
        {"Ohio State University (Main campus)", "Ohio State University-Main Campus"}}) {
      val = map.get(keys[0]);
      map.remove(keys[0]);
      map.put(keys[1], val);
    }


    Scanner scanner2 = new Scanner(new File("CSV_5302024-346.csv"));
    String text2 = scanner2.nextLine() + ",";
    int i = 0;
    for (String[] subject : subjects) {
      if (i == 10) {
        text2 += subject[0];
      } else {
        text2 += subject[0] + ",";
      }
    }
    // TODO learn

    Pattern uniPattern = Pattern.compile("^[^,]+(?:, [^,]+)?(?=,)");
    int numberMatches = 0;

    while (scanner2.hasNextLine()) {
      String line = scanner2.nextLine();
      Matcher uniMatcher = uniPattern.matcher(line);
      uniMatcher.find();
      String[] rankings = map.get(uniMatcher.group(0));

      if (rankings != null) {
        for (String ranking : rankings) {
        }
        numberMatches++;
        text2 += "\n" + line + ",";
        for (int j = 0; j < 11; j++) {
          if (rankings[j] == null) {
            rankings[j] = "";
          }
          text2 += (j == 10) ? rankings[j] : rankings[j] + ",";
        }

      } else {
        text2 += "\n" + line + ",,,,,,,,,,";
      }



    }

    scanner2.close();
    return text2;
  }



}
