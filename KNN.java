
import java.awt.*;
import java.io.*;
import java.util.*;


public class KNN {
	
	// Hold all books from csv file
	public static ArrayList<String> books = new ArrayList<String>();
	// Existing user data
	public static HashMap<String, HashMap <String, Integer>> userRatings = new HashMap <String, HashMap<String, Integer>>();
	// New user data
	public static HashMap<String, HashMap <String, Integer>> newuserRatings = new HashMap <String, HashMap<String, Integer>>();
	public static ArrayList<String> users = new ArrayList();
	// Averages for the user being looked at
	public static HashMap<String, Double> averages = new HashMap<String, Double>();
	// Pearson hash stored for all users
	public static HashMap<String, Double> pearsonHash = new HashMap<String, Double> ();

	
	/**
	 * Reading in all of the data from the .csv file into a HashMap of Hashmaps for each user.
	 */
	public static void dataIn ()  throws IOException
	{
		BufferedReader csvReader = new BufferedReader(new FileReader("knn-csc480-a4.csv"));
		String row;
		int count = 0;
		
		while ((row = csvReader.readLine()) != null) {
		    String[] data = row.split(",", -1);
		    HashMap<String, Integer> temp = new HashMap <String, Integer>();
		    
		    for (int i = 0; i < data.length; i++)
		    {
		    	 
		    	 
		    	 if(count == 0 && i != 0)
				    {
				    	books.add(data[i]);
				    }
		    	 
		    	 // Adding all existing users to the HashMap
		    	 else if (count < 21 && i != 0)
		    	 {
		    		 String book = books.get(i-1);
		    		 Integer rating = Integer.parseInt(data[i]);
		    		 temp.put(book, rating);
		    		 userRatings.put(data[0], temp);
		    		 
		    	 }
		    	 
		    	 
		    	 else if (count > 21 && i != 0)
		    	 {
		    		 String book = books.get(i-1);
		    		 Integer rating = Integer.parseInt(data[i]);
		    		 temp.put(book, rating);
		    		 newuserRatings.put(data[0], temp);
		    		 
		    	 }
		    	 
		    }
		    
		    if (count != 0)
		    {
		    	users.add(data[0]);
		    }
		    
		    count++;
		    
		}
		csvReader.close();
		
		
	}
	
	/**
	 * Determines the average rating for each user and then calculates the Pearson coeffcient
	 * per the example in Slide 30. 
	 */
	public static void pearsonCo (int k, String testUser)
	{
	
				ArrayList<Integer> newRT = new ArrayList<Integer>();
				// Test user from Main Method
				HashMap <String, Integer>testU = newuserRatings.get(testUser);
				Double avg = 0.0;
				Double total = 0.0;
				Double testAvg = 0.0;
				
				// Determining average rating
				for (int z = 0; z < testU.size(); z++)
				{
					String book = books.get(z);
					avg += testU.get(book);
					newRT.add(testU.get(book));
					
					if (testU.get(book) != 0)
					{
						total++;
					}
				}
				
				avg = avg/total;
				testAvg = avg;
				
		// Existing user data		
		for (int i = 0; i < userRatings.size(); i++)
		{
			String user = users.get(i);
			HashMap<String, Integer> temp = userRatings.get(user);
			avg = 0.0;
			total = 0.0;
			ArrayList<Integer> userRT = new ArrayList<Integer>();
			
			for (int y = 0; y < temp.size(); y++)
			{
				String book = books.get(y);
				avg += temp.get(book);
				userRT.add(temp.get(book));
				
				if (temp.get(book) != 0)
				{
					total++;
				}
			}
			
			avg = avg/total;
			averages.put(user, avg);
			ArrayList<Integer> overlap = new ArrayList<Integer>();
			
			// Overlapping items
			for (int q = 0; q < userRT.size(); q++)
			{
				if (userRT.get(q)!= 0 && newRT.get(q) != 0)
				{
					overlap.add(q);
				}
			}
			
			//System.out.println(overlap);
			Double pearson = 0.0;
			
			// First half of equation     
			for (int q = 0; q < overlap.size(); q++)
			{
				Integer u = userRT.get(overlap.get(q));
				Integer n = newRT.get(overlap.get(q));
				
				pearson += (u - avg) * (n - testAvg);
			}
			
			double upearson_2 = 0.0;
			double npearson_2 = 0.0;
			
			// Second half of equation
			for (int q = 0; q < overlap.size(); q++)
			{
				Integer u = userRT.get(overlap.get(q));
				Integer n = newRT.get(overlap.get(q));
				
				upearson_2 += (u - avg) * (u - avg);
				npearson_2 += (n - avg) * (n - avg);
			}
			
			double p2 = Math.sqrt((upearson_2 * npearson_2));
			pearson = pearson/p2;
			
			pearsonHash.put(user, pearson);
			
		}
		
	}
	
	/**
	 * Determine the k neighbors for the test user based on inputs from the main method.
	 */
	public static ArrayList<String> kneighbors (int k, String u)
	{
		ArrayList<String> toreturn = new ArrayList<String>();
		ArrayList<String> blacklist = new ArrayList<String>();
		
		blacklist.add(u);
		int index = 0;
		Double tracker = -100000.0;
		
		for (int i = 0; i < pearsonHash.size(); i++)
		{
			String user = users.get(i);
			if (pearsonHash.get(user) > tracker && blacklist.contains(user) == false)
			{
				tracker = pearsonHash.get(user);
				index = i;
			}
			
			if (i == pearsonHash.size() -1)
			{
				user = users.get(index);
				toreturn.add(user);
				
				if (toreturn.size() < k)
				{
					i = 0;
					blacklist.add(user);
					tracker = -100000.0;
				}
			}
		}
		
		return toreturn;
	}
	
	/**
	 * Determining predictions for the new users on both those they have rated and those they have
	 * not. Mean absolute error also calculated here.
	 */
	public static void newuserPredictions(String nu, ArrayList<String> neighbors)
	{
		HashMap<String, Integer> nuRating = newuserRatings.get(nu);
		ArrayList<Integer> Rated = new ArrayList<Integer>();
		Double MAE = 0.0;
		int maeCount = 0;
		
			for (int i =0; i < books.size(); i++)
			{
				String book = books.get(i);
				Double prediction = 0.0;
				Double simAcc = 0.0;
				
				for (int z = 0; z < neighbors.size(); z++)
				{	
					String olduser = neighbors.get(z);
					Integer rating = userRatings.get(olduser).get(book);
					Double pearson = pearsonHash.get(olduser);
					
					if (rating != 0)
					{
						Double calc = rating * pearson;
						prediction += calc;
						simAcc += pearson;
					}
				}
				
				// Accounting for runs that have no predictions from any K neighbor
				if (simAcc == 0)
				{
					continue;
				}
				
				prediction = prediction/simAcc;
				
				// New user already had a rating
				if (newuserRatings.get(nu).get(book) != 0)
				{
					Integer nRating = newuserRatings.get(nu).get(book);
					Double compare = Math.abs(prediction - nRating);
					MAE += compare;
					maeCount++;
					System.out.println("Actual rating: " + nRating + " " + "Predicted Rating: " + prediction);
				}
				
				// New user had no rating
				else if (newuserRatings.get(nu).get(book) == 0)
				{
					System.out.println("Book: " + book + " " + "Predicted Rating: " + prediction);
				}
				
			}
			
			MAE = MAE/maeCount;
			System.out.println("The MAE for predictions on user " + nu + " is :" + MAE);
	
	}
	
	/**
	 * Determining the averages and pearson hash for the existing user
	 */
	public static void recommendations (String user)
	{
		ArrayList<Integer> rt = new ArrayList<Integer>();
		// User from Main Method
		HashMap <String, Integer>testU = userRatings.get(user);
		Double avg = 0.0;
		Double total = 0.0;
		Double testAvg = 0.0;
		
		// Determining average rating
		for (int z = 0; z < testU.size(); z++)
		{
			String book = books.get(z);
			avg += testU.get(book);
			rt.add(testU.get(book));
			
			if (testU.get(book) != 0)
			{
				total++;
			}
		}
		
		for (int i = 0; i < userRatings.size(); i++)
		{
			String user2 = users.get(i);
			HashMap<String, Integer> temp = userRatings.get(user2);
			avg = 0.0;
			total = 0.0;
			ArrayList<Integer> userRT = new ArrayList<Integer>();
			
			for (int y = 0; y < temp.size(); y++)
			{
				String book = books.get(y);
				avg += temp.get(book);
				userRT.add(temp.get(book));
				
				if (temp.get(book) != 0)
				{
					total++;
				}
			}
			
			avg = avg/total;
			averages.put(user2, avg);
			ArrayList<Integer> overlap = new ArrayList<Integer>();
			
			// Overlapping items
						for (int q = 0; q < userRT.size(); q++)
						{
							if (userRT.get(q)!= 0 && rt.get(q) != 0)
							{
								overlap.add(q);
							}
						}
						
						Double pearson = 0.0;
						
						// First half of equation     
						for (int q = 0; q < overlap.size(); q++)
						{
							Integer u = userRT.get(overlap.get(q));
							Integer n = rt.get(overlap.get(q));
							
							pearson += (u - avg) * (n - testAvg);
						}
						
						double upearson_2 = 0.0;
						double npearson_2 = 0.0;
						
						// Second half of equation
						for (int q = 0; q < overlap.size(); q++)
						{
							Integer u = userRT.get(overlap.get(q));
							Integer n = rt.get(overlap.get(q));
							
							upearson_2 += (u - avg) * (u - avg);
							npearson_2 += (n - avg) * (n - avg);
						}
						
						double p2 = Math.sqrt((upearson_2 * npearson_2));
						pearson = pearson/p2;
						
						pearsonHash.put(user, pearson);
						
					}
	}
	
	/**
	 * Determining recommendations for the existing user given from the main method. For
	 * every book they have not rated, they are provided recommendations for N items. Given in decreasing
	 * order.
	 */
	public static void userPredict (String user, ArrayList<String> ng, int n)
	{
		HashMap<String, Integer> nuRating = userRatings.get(user);
		HashMap<String, Double> nItems= new HashMap<String, Double>();
		ArrayList<String> booksRead = new ArrayList<String>();
		Double MAE = 0.0;
		int maeCount = 0;
		
			for (int i =0; i < books.size(); i++)
			{
				String book = books.get(i);
				Double prediction = 0.0;
				Double simAcc = 0.0;
				
				for (int z = 0; z < ng.size(); z++)
				{	
					String olduser = ng.get(z);
					Integer rating = userRatings.get(olduser).get(book);
					Double pearson = pearsonHash.get(olduser);
					
					if (rating != 0)
					{
						Double calc = rating * pearson;
						prediction += calc;
						simAcc += pearson;
					}
				}
				
				// Accounting for runs that have no predictions from any K neighbor
				if (simAcc == 0)
				{
					continue;
				}
				
				prediction = prediction/simAcc;
				
				if (userRatings.get(user).get(book) == 0)
				{
					nItems.put(book, prediction);
					booksRead.add(book);
				}
				
			}
			
			Double a = -20.0;
			String bked = "";
			
			// N recommendations
			for (int u = 0; u < n; u++)
			{
				
				for (int g = 0; g < nItems.size(); g++)
				{
					
					String bk = booksRead.get(g);
					
					if (nItems.get(bk) == null)
					{
						continue;
					}
					
					Double val = nItems.get(bk);
					if (val > a)
					{
						bked = bk;
						a = val;
					}
					
					if (g == nItems.size()-1)
					{
						System.out.println("Recommend book: " + bked + " for user: " + user + " with rating of: " + a);
						nItems.remove(bked);
						booksRead.remove(bked);
						a = -20.0;
						nItems.values().removeIf(Objects::isNull);
					}
				
				}
				
				
			}
			
			
	
	}
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub

		// Reading in the file and adding to the Hashmaps for existing users, and new users
		dataIn();
		
		Scanner input = new Scanner(System.in);
		
		while (true)
		{
			System.out.println("Enter the new user to analyze: ");
			String selection = input.nextLine();
			System.out.println("Enter the k neighbors to look at: ");
			
			int k = input.nextInt();
			input.nextLine();
			String user = selection;
			pearsonCo(k, user);
			
			// Identify K users
			ArrayList<String> knn_neighbors = kneighbors(k, user);
			System.out.println("User: " + user);
			System.out.println("K nearest neighbors: " + knn_neighbors);
			
			// Gathering predictions using weighted average
			newuserPredictions(user, knn_neighbors);
			
			System.out.println("Enter a user (U1-U20) to get recommendations for: ");
			String selection2 = input.nextLine();
			System.out.println("Enter the value for N: ");
			int n = input.nextInt();
			input.nextLine();
			
			// Gathering recommendations for the desired user
			recommendations(selection2);
			ArrayList<String> knn_neighbors_r = kneighbors(4, selection2);
			System.out.println("User: " + selection2);
			System.out.println("K nearest neighbors: " + knn_neighbors_r);
			
			// Getting the N recommendations for the user
			userPredict(selection2, knn_neighbors_r, n);
		}
	}

}
