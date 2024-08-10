import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Backend {

  private List<University> dataset = new ArrayList<>();

  public static void main(String[] args) throws IOException {

    Backend backend = new Backend();
    backend.parseData();
    // Enter query parameters here
    for(University uni: backend.findTarget("Show All", "Show All", 1400, "E",
        60000.0)) {
      double[] displayAttributes = uni.getAllAttributes();
      System.out.print(uni.getName());
      // Choose to display other attributes here
      System.out.println();
    }
    

    // try {
    // CombineCSV.combineCSVFiles();
    // } catch (FileNotFoundException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
  }

  /**
   * Loads the individual rows from universitiesData.csv into an ArrayList as a value sepearated by
   * commas in a double[].
   */
  public void parseData() throws IOException {
    Scanner scanner = new Scanner(new File("universitiesData.csv"));
    String line;
    double[] datapoint;
    // Skip the column header line
    scanner.nextLine();
    int itr = 0;
    while (scanner.hasNextLine()) {
      line = scanner.nextLine();
      int quoteCount = 0;
      char quote = '"';
      String name = "";
      int beg = 0;
      for (int j = 0; j < line.length(); j++) {
        beg = j;
        if (line.charAt(j) == quote)
          quoteCount++;
        else
          name += line.charAt(j);
        if (quoteCount == 6)
          break;
      }
      line = line.substring(beg + 2);
      String[] values = line.split(",");
      datapoint = new double[30];
      for (int i = 3; i < values.length; i++) {
        if (!values[i].equals("")) {
          datapoint[i - 3] = Double.parseDouble(values[i]);
        }

      }

      // 18 - 29

      String fourYear = values[1];
      String state = values[0];
      String strType = values[2];
      Boolean type = false;
      if (strType.equals("Public"))
        type = true;
      // Filter out all of the universities that are not "Four year or more" institutions
      if (fourYear.equals("Four or more years")) {
        // Filter out all of the universities that don't have an assigned ranking for at least one
        // subject
        Boolean atLeastOne = false;
        for (int i = 0; i < datapoint.length; i++) {
          if (i >= 15 && i <= 26 && datapoint[i] != 0.0) {
            atLeastOne = true;
          }
        }
        if (atLeastOne) {
          itr++;
          dataset.add(new University(datapoint, name, state, type));
        }


      }
    }
  }

  public double calculateDistance(double[] p1, double[] p2) {
    double sum = 0;
    for (int i = 0; i < p1.length; i++) {
      sum += Math.pow(p1[i] - p2[i], 2);
    }
    return Math.sqrt(sum);
  }

  public List<University> find5NearestNeighbors(double[] queryPoint, boolean aidFilter) {

    PriorityQueue<University> pQ = new PriorityQueue<>((a, b) -> Double
        .compare(calculateDistance(a.getAttributes(), queryPoint), calculateDistance(b.getAttributes(), queryPoint)));

    for (University uni : dataset) {
      pQ.add(uni);
    }
    ArrayList<University> toReturn = new ArrayList<University>();
    pQ.poll(); // The first finding will be the same as the queryPoint
    int findingSize = pQ.size();
    for(int i = 0; i<Math.min(5, findingSize); i++) {
      toReturn.add(pQ.poll());
    }
    
    return toReturn;

  }



  /*
   * Here are the inputs from the Frontend: 1. Public/Private university -- boolean (true if
   * Public), dropdown list 2. State of residence (for financial aid purposes, not for filters) --
   * String, dropdown list 3. State filter -- String, dropdown list 4. SAT or ACT or test optional
   * score -- Integer, 0 if test optional 5. Desired major -- String, dropdown list of many items,
   * which is then categorized in some categories 7. Optional -- Desired net price (tuition and cost
   * of room and board) -- Integer 8. Optional -- Income category -- String (A, B, C, D, E)
   * 
   * List of All Types of Majors Arts and Humanities, Business and Economics, Clinical and Health,
   * Computer Science, Education, Engineering, Law, Life Sciences, Physical Sciences, Psychology,
   * Social Sciences
   */

  /**
   * Returns an array of 5 safety University objects according to the user's preferences.
   * 
   * @return a String array of 5 personalized University objectss
   * @param type        a filter for the type of institution (public or private )
   * @param stateFilter the state filter for the applicant
   * @param score       the applicants ACT or SAT standardized test score
   * @param category    the applicant's income category (A-E)
   * @param price       the applicant's desired net price
   */
  public List<University> findTarget(String type, String stateFilter, Integer score, String category,
      Double price) {

    // 2. Remove unnecessary University objects from the dataset
    // 2. a) Filter out University objects from the dataset according to stateFilter
    if (!stateFilter.equals("Show All")) {
      ArrayList<University> filteredDataset = new ArrayList<University>();
      for (University uni : dataset)
        if (uni.getState().equals(stateFilter))
          filteredDataset.add(uni);
      this.dataset = filteredDataset;
    }
    
    

    // 2. b) Filter out University objects from the dataset according to institution type
    if (!type.equals("Show All")) {
      Boolean requiredType = false;
      if (type.equals("true"))
        requiredType = true;
      ArrayList<University> filteredDataset = new ArrayList<University>();
      for (University uni : dataset)
        if (uni.isType() == requiredType)
          filteredDataset.add(uni);
      this.dataset = filteredDataset;
    }

    // 2. c) Remove all University objects where the user's test score is out of the middle 50%
    // If not provided, Remove University objects in accordance with the national rankings
    // Indices 9, 10 and 11 (75th percentile score) SAT
    // Indices 12, 13, 14 ACT
    // Index 15 National Ranking
    // Index 8 Percent Admitted
    ArrayList<University> filteredDataset = new ArrayList<University>();
    ArrayList<University> emDataset = new ArrayList<University>();
    for (University uni : dataset) {
      double[] attributes = uni.getAttributes();

      if (score % 100 == score) {
        // The score is an ACT composite score
        if (attributes[12] != 0.0 && attributes[14] != 0.0) {
          if (score >= attributes[12] && score <= attributes[14]) {
            filteredDataset.add(uni);
          }
          else {
            emDataset.add(uni);
          }
          
        } else {

          // If the score is above 33, target the top 30 institutions
          // If the score is between 29 and 33, target the top 60 institutions except the top 25
          // institutions
          // If the score is below 30, target all of the institutions ranked above 45

          if (attributes[15] != 0.0) {
            if (score >= 33) {
              if (attributes[15] <= 30.0)
                filteredDataset.add(uni);
            } else if (score >= 29 && score < 33) {
              if (attributes[15] <= 60 && attributes[15] >= 25)
                filteredDataset.add(uni);
            } else {
              if (attributes[15] >= 45) {
                filteredDataset.add(uni);
              }
            }
          }
          else {
            emDataset.add(uni);
          }
        }
        


      } else {
        // The score is an SAT composite score
        if (attributes[9] != 0.0 && attributes[11] != 0.0) {
          if (score >= attributes[9] && score <= attributes[11]) {
            filteredDataset.add(uni);
          }
          else {
            emDataset.add(uni);
          }
          
          
        } else {
          // If the score is above 1500, target the top 30 institutions
          // If the score is between 1400 and 1500, target the top 60 institutions except the top 25
          // institutions
          // If the score is below 1400, target all of the institutions ranked above 45
          
          if (attributes[15] != 0.0) {
            if (score >= 1500) {
              if (attributes[15] <= 30.0)
                filteredDataset.add(uni);
            } else if (score >= 1400 && score < 1500) {
              if (attributes[15] <= 60 && attributes[15] >= 25)
                filteredDataset.add(uni);
            } else {
              if (attributes[15] >= 45) {
                filteredDataset.add(uni);
              }
            }
          }
          
          else {
            emDataset.add(uni);
          }
        }

      }
      this.dataset = filteredDataset;

    }
    
    if(dataset.size()==0) {
      this.dataset = emDataset;
    }
    

    
    // 1. Remove unnecessary columns
    // 1. a) Depending on the type of test score provided, filter out the other type of test score
    // from
    // the dataset and the 25th and 75th percentile for the current type of test score for KNN
    // fitting
    if (score % 100 == score) {
      // The score is an ACT composite score, so filter out SAT statistics and the 25th and 75th
      // percentile ACT statistics from the results

      filterOutTestScore(true);
    } else {
      // The score is an SAT composite score, so filter out ACT statistics and the 25th and 75th
      // percentile SAT statistics from the results
      filterOutTestScore(false);
    }

    
    // 1. b) Remove all subject-wise ranking measures statistics
    for (University uni : dataset) {
      double[] attributes = uni.getAttributes();
      double[] newAttributes = new double[attributes.length - 15];
      for (int i = 0; i < newAttributes.length; i++) {
        newAttributes[i] = attributes[i];
      }
      uni.setAttributes(newAttributes);
    }
    
    // 1. c) Remove total price for in-state students, total price for out-state students, and
    // percent admitted
    for (University uni : dataset) {
      double[] attributes = uni.getAttributes();
      double[] newAttributes = new double[attributes.length - 3];
      for (int i = 0; i < newAttributes.length; i++) {
        if (i < 6)
          newAttributes[i] = attributes[i];
        else
          newAttributes[i] = attributes[i + 3];
      }
      uni.setAttributes(newAttributes);
    }

    // 1. d) Remove average net-price for other income categories
    int toLeave = 0;
    if (category.equals("A"))
      toLeave = 1;
    else if (category.equals("B"))
      toLeave = 2;
    else if (category.equals("C"))
      toLeave = 3;
    else if (category.equals("D"))
      toLeave = 4;
    else
      toLeave = 5;
    for (University uni : dataset) {
      double[] attributes = uni.getAttributes();
      double[] newAttributes = new double[attributes.length - 4];
      for (int i = 0; i < newAttributes.length; i++) {
        if(i==0) newAttributes[i] = attributes[i];
        else if (i < toLeave)
          try {
            newAttributes[i] = attributes[i + 5];
          }
          catch(IndexOutOfBoundsException e){
            newAttributes[i] = attributes[toLeave];
            break;
          }
        else if (i == toLeave)
          if(i==1) {
            newAttributes[i] = attributes[6];
          }
          else{
            newAttributes[i] = attributes[i];
          }
        else
          if(i==2) {
            newAttributes[i] = attributes[1];
          }
          else {
            newAttributes[i] = attributes[i + 5 - toLeave];
          }
          
      }
      uni.setAttributes(newAttributes);
    }

//    System.out.println(dataset.size());
//    int l = 1;
//    for (University uni : dataset) {
//      double[] attributes = uni.getAttributes();
//      String name = uni.getName();
//      Boolean type1 = uni.isType();
//      String state = uni.getState();
//      System.out.print(l + " " + name + " "+ state + " " + type1 + ": ");
//      for(double at: attributes) {
//        System.out.print(at + ", ");
//      }
//      System.out.println();
//      l++;
//    }   
    
    double[] queryPoint = {97.0, score, price};
    boolean aidFilter = true;
    if(price==0) {
      aidFilter = false;
      for(University uni: dataset) {
        double[] attributes = uni.getAttributes();
        double[] newAttributes = new double[attributes.length - 1];
        for (int i = 0; i < newAttributes.length; i++) {
          newAttributes[i] = attributes[i+1];
        }
        uni.setAttributes(newAttributes);
      }
    }
    
    return find5NearestNeighbors(queryPoint, aidFilter);
  }

  /**
   * Filters out SAT or ACT score from the dataset for all the universities, and the 25th and 75th
   * percentile metrics for the current type of score (SAT or ACT) for KNN fitting
   * 
   * @param sat true if the caller want to filter out SAT scores, false if ACT scores
   */
  private void filterOutTestScore(Boolean sat) {
    if (sat) {
      for (University uni : dataset) {
        double[] attributes = uni.getAttributes();
        double[] newAttributes = new double[attributes.length - 3];
        for (int i = 0; i < newAttributes.length; i++) {
          if (i < 9) {
            newAttributes[i] = attributes[i];
          } else {
            newAttributes[i] = attributes[i + 3];
          }
        }

        double[] newAttributes1 = new double[newAttributes.length - 1];
        for (int i = 0; i < newAttributes1.length; i++) {
          if (i < 9) {
            newAttributes1[i] = newAttributes[i];
          } else {
            newAttributes1[i] = newAttributes[i + 1];
          }
        }

        double[] newAttributes2 = new double[newAttributes1.length - 1];
        for (int i = 0; i < newAttributes2.length; i++) {
          if (i < 10) {
            newAttributes2[i] = newAttributes1[i];
          } else {
            newAttributes2[i] = newAttributes1[i + 1];
          }
        }

        uni.setAttributes(newAttributes2);
      }
    } else {
      for (University uni : dataset) {
        double[] attributes = uni.getAttributes();
        double[] newAttributes = new double[attributes.length - 3];
        for (int i = 0; i < newAttributes.length; i++) {
          if (i < 12) {
            newAttributes[i] = attributes[i];
          } else {
            newAttributes[i] = attributes[i + 3];
          }
        }

        double[] newAttributes1 = new double[newAttributes.length - 1];
        for (int i = 0; i < newAttributes1.length; i++) {
          if (i < 9) {
            newAttributes1[i] = newAttributes[i];
          } else {
            newAttributes1[i] = newAttributes[i + 1];
          }
        }

        double[] newAttributes2 = new double[newAttributes1.length - 1];
        for (int i = 0; i < newAttributes2.length; i++) {
          if (i < 10) {
            newAttributes2[i] = newAttributes1[i];
          } else {
            newAttributes2[i] = newAttributes1[i + 1];
          }
        }

        uni.setAttributes(newAttributes2);
      }
    }
  }

  /**
   * Returns an array of 5 safety schools according to the user's preferences.
   * 
   * @return a String array of 5 personalized safety schools
   */
  public String[] findSafety() {
    return null;
  }

  /**
   * Returns an array of 5 reach schools according to the user's preferences.
   * 
   * @return a String array of 5 personalized reach schools
   */
  public String[] findReach() {
    return null;
  }


  public class University {
    // Attributes used for the KNN fit
    private double[] attributes;
    // All the attributes, some of which are provided for display to the Frontend
    private double[] allAttributes;
    private String name;
    // true: public, false: private
    private boolean type;
    private String state;


    public University(double[] attributes, String name, String state, Boolean type) {
      this.attributes = attributes;
      this.setAllAttributes(attributes);
      this.name = name;
      this.state = state;
      this.type = type;
    }

    public boolean isType() {
      return type;
    }

    public void setType(boolean type) {
      this.type = type;
    }

    public double[] getAttributes() {
      return attributes;
    }

    public void setAttributes(double[] attributes) {
      this.attributes = attributes;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getState() {
      return state;
    }

    public void setState(String state) {
      this.state = state;
    }

    public double[] getAllAttributes() {
      return allAttributes;
    }

    public void setAllAttributes(double[] allAttributes) {
      this.allAttributes = allAttributes;
    }


  }

}
