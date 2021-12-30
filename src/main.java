import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class main {

    public static int TOTAL_NODE_SIZE;
    public static int POPULATION_SIZE = 150;
    static Random rand = new Random();
    public static Map<Double, Node> allNodes;
    public static double[][] population;

    public static void main(String[] args) throws Exception {
        rand.setSeed(System.currentTimeMillis());
        allNodes = initializeData("/Users/halilkaya/IdeaProjects/genetic_homework/src/Cities_Coordinates.tsp");
        TOTAL_NODE_SIZE = allNodes.size();
        population = initializePopulationRandom();
        calculatePath(population);
        //printMatrix();
        for(int i = 0;i<1000;i++){
            System.out.println("*********************"+i+".GENERATION *********************");
            int[] parentIndexes = findParentIndexes();
            printCurrentBestWay(parentIndexes);
            double[] child = createChild(parentIndexes);
            replaceWorstWayWithChild(child);
            mutate(parentIndexes);
            calculatePath(population);
        }
    }

    public static void mutate(int[] parentIndexes){
        int mutationCount = 30;
        for(int i = 0; i < mutationCount;i++){
            int randomWay = rand.nextInt(POPULATION_SIZE);
            if(randomWay == parentIndexes[0]){
                i--;
                continue;
            }
            int randomCity = rand.nextInt(TOTAL_NODE_SIZE);
            int randomCity2 = rand.nextInt(TOTAL_NODE_SIZE);
            double tmp = population[randomWay][randomCity];
            population[randomWay][randomCity] = population[randomWay][randomCity2];
            population[randomWay][randomCity2] = tmp;
        }
    }

    public static void replaceWorstWayWithChild(double[] child){
        int worstWayIndex = findWorstWayIndex();
        population[worstWayIndex] = child;
    }

    public static int findWorstWayIndex(){
        int maxAt = 0;
        for(int i = 0; i < population.length; i++){
            maxAt = population[i][population[i].length - 1] > population[maxAt][population[i].length - 1] ? i : maxAt;
        }
        return maxAt;
    }

    public static void printCurrentBestWay(int[] parentIndexes){
        for(int i = 0; i < parentIndexes.length;i++){
            System.out.println((i+1) + ".best way cost: " + population[parentIndexes[i]][population[i].length - 1]);
            for (int j = 0 ; j < population[parentIndexes[i]].length - 1 ; j++){
                System.out.print(population[parentIndexes[i]][j] + " ");
            }
            System.out.println();
        }
    }

    public static double[] createChild(int parentIndexes[]){
        double[] parent1 = population[parentIndexes[0]];
        double[] parent2 = population[parentIndexes[1]];
        double[] child = new double[TOTAL_NODE_SIZE + 1];
        int randomRange1 = rand.nextInt(TOTAL_NODE_SIZE) + 1;
        int randomRange2 = rand.nextInt(TOTAL_NODE_SIZE) + 1;
        if(randomRange1 > randomRange2){
            int tmp = randomRange1;
            randomRange1 = randomRange2;
            randomRange2 = tmp;
        }
        for(int i = 0;i < parent1.length - 1;i++){
            if(i > randomRange1 && i < randomRange2){
                child[i] = parent1[i];
            }else{
                child[i] = parent2[i];
            }
        }
        fixChild(child);
        return child;
    }

    public static void fixChild(double[] child){
        Set<Double> cities  = new HashSet();
        Set<Double> duplicateCities  = new HashSet();
        Set<Double> missingCities  = new HashSet();
        for (int i = 1;i<child.length;i++){
            missingCities.add((double)i);
            if(cities.contains(child[i-1])){
                duplicateCities.add((double)child[i-1]);
            }
            cities.add(child[i-1]);
        }
        for (int i = 1;i<child.length;i++){
            missingCities.remove(child[i - 1]);
        }
        for (int i = 1;i<child.length;i++){
            if(duplicateCities.contains(child[i-1])){
                duplicateCities.remove(child[i-1]);
                child[i-1] = missingCities.stream().findFirst().get();
                missingCities.remove(child[i-1]);
            }
            if(duplicateCities.isEmpty()){
                break;
            }
        }
    }

    public static void printMatrix(){
        for (int i = 0;i<population.length;i++){
            System.out.print(i+"-> ");
            for (int j = 0;j< population[i].length;j++){
                System.out.print(population[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static int[] findParentIndexes(){
        double firstMin = Double.MAX_VALUE, secondMin = Double.MAX_VALUE;
        int firstMinIndex = 0;
        int secondMinIndex = 0;
        int[] parents = {0,0};
        for (int i = 0; i < population.length ; i ++) {
            if (population[i][population[i].length - 1] < firstMin) {
                secondMin = firstMin;
                firstMin = population[i][population[i].length - 1];
                firstMinIndex = i;
            }
            else if (population[i][population[i].length - 1] < secondMin
                    && population[i][population[i].length - 1] != firstMin){
                secondMin = population[i][population[i].length - 1];
                secondMinIndex = i;
            }
        }
        parents[0] = firstMinIndex;
        parents[1] = secondMinIndex;
        return parents;
    }

    public static void calculatePath(double[][] population){
        for(int i = 0;i<population.length;i++){
            double totalDistance = 0;
            for(int j = 0;j < population[i].length - 2;j++){
                totalDistance += distanceBetweenTwoPoints(population[i][j],population[i][j + 1]);
            }
            population[i][population[i].length - 1] = totalDistance;
        }
    }

    public static double[][] initializePopulationRandom(){
        double[][] population = new double[POPULATION_SIZE][TOTAL_NODE_SIZE + 1];
        boolean firstTime = true;
        for(int i = 0; i < POPULATION_SIZE;i++){
            if(firstTime){
                for(int j = 0; j < population[i].length; j++) {
                    if(j == (population[i].length - 1)){
                        population[i][j] = 0;
                        continue;
                    }
                    population[i][j] = rand.nextInt(TOTAL_NODE_SIZE) + 1;
                    for (int k = 0; k < j; k++) {
                        if (population[i][j] == population[i][k]) {
                            j--;
                        }
                    }
                }   
                firstTime = false;
            }else{
                for(int j = 0; j < population[i].length; j++){
                    population[i][j] = population[0][j];
                }
            }
        }
        return population;
    }

    public static double distanceBetweenTwoPoints(double city1,double city2){
        Node node1 = allNodes.get(city1);
        Node node2 = allNodes.get(city2);
        return Math.sqrt((Math.pow(node2.x-node1.x, 2) + Math.pow(node2.y-node1.y, 2)));
    }

    public static Map<Double, Node> initializeData(String filePath){
        Map<Double, Node> allNodes  = new HashMap<>();
        try {
            File myObj = new File(filePath);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if(Character.isDigit(data.charAt(0))){
                    String[] splitedData = data.split(" ");
                    double city = Double.parseDouble(splitedData[0]);
                    double x = Integer.parseInt(splitedData[1]);
                    double y = Integer.parseInt(splitedData[2]);
                    allNodes.put(city,new Node(x,y));
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return allNodes;
    }
}

class Node{
    public double x;
    public double y;

    public Node(double x,double y){
        this.x = x;
        this.y = y;
    }
}