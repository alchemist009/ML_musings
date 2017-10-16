import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Scanner;
import java.util.Set;
import java.util.HashMap;
import java.util.Map.Entry;


public class LogisticRegressionClassifier {

	public static Set<String> distinct_vocab = new HashSet<String>();
	public static HashMap<String, Integer> my_spam_wordmap = new HashMap<String, Integer>();
	public static HashMap<String, Integer> my_ham_wordmap = new HashMap<String, Integer>();

	public static HashMap<String,HashMap<String, Integer> >spam_filemap = new HashMap<String, HashMap<String,Integer>>();	
	public static HashMap<String,HashMap<String, Integer> >ham_filemap = new HashMap<String, HashMap<String,Integer>>();

	static Set<String> ham_file_set = new HashSet<String>();
	static Set<String> spam_file_set = new HashSet<String>();
	static Set<String> all_file_set = new HashSet<String>();
	public static Set<String> Stopword_list = new HashSet<String>();

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println(" Kindly provide input in the following format :");
			System.out.println(" <path-to-directory-containing-test-and-training-dirs> <yes/no : To remove stopword or not> <learning-rate> <lambda-value> <repetitions>");
			System.exit(1);
		} 

		String path = args[0];
		String to_filter = args[1];
		double learning_rate = Double.parseDouble(args[2]);
		double lambda = Double.parseDouble(args[3]);
		int num_iterations = Integer.parseInt(args[4]);

		File path_to_spam_train = new File(path+"/train/spam");
		File path_to_ham_train = new File(path+"/train/ham"); 

		File path_to_spam_test = new File(path+"/test/spam");
		File path_to_ham_test = new File(path+"/test/ham");  

		File stopwords = new File(path+"/stopwords.txt");

		addDistinct(path_to_spam_train);
		addDistinct(path_to_ham_train);

		Scanner s1=null;
		try {
			s1 = new Scanner(stopwords);
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		while(s1.hasNext()){
			String sw = s1.next();
			Stopword_list.add(sw);
		}
		s1.close();

		if(to_filter.equals("yes")){
			for(String str : Stopword_list){
				if(distinct_vocab.contains(str)){
					distinct_vocab.remove(str);
				}
			}

		}


		getHashmap_spam(path_to_spam_train);
		getHashmap_ham(path_to_ham_train);

		LogisticRegressionAlgorithm lr = new LogisticRegressionAlgorithm(path, distinct_vocab,ham_filemap,spam_filemap,my_ham_wordmap,my_spam_wordmap, learning_rate, lambda, num_iterations,all_file_set,spam_file_set,ham_file_set);


		lr.train();
		int s_count = 0 ;
		int num_spam_testfiles = 0;


		for(File testfile : path_to_spam_test.listFiles()){
			num_spam_testfiles = num_spam_testfiles+1;
			HashMap<String, Integer> my_testmap = new HashMap<String, Integer>();
			Scanner sc = new Scanner(testfile);
			while(sc.hasNext()){
				String line = sc.nextLine();
				for(String s: line.toLowerCase().trim().split(" ")){
					s = s.replaceAll("[^a-zA-Z]+", "");
					if(my_testmap.containsKey(s)){
						my_testmap.put(s, my_testmap.get(s)+1);
					}else{
						my_testmap.put(s, 1);
					}
				}	
			}
			sc.close();
			
			if(to_filter.equals("yes")){
				//System.out.println("Testing begins now");
				for(String stopword: Stopword_list){
					stopword = stopword.replaceAll("[^a-zA-Z]+", "");
					if(my_testmap.containsKey(stopword)){
						my_testmap.remove(stopword);
					}
				}
			}
			
			int spam = lr.test(my_testmap);
			if(spam == 1){
				s_count++;

			}

		}

		double spam_acc = ( (double)s_count / (double)num_spam_testfiles)*100;

		System.out.println("Spam % Accuracy "+ (spam_acc));

		System.out.println();


		int h_count = 0 ;
		int num_ham_testfiles = path_to_ham_test.listFiles().length;

		for(File testfile : path_to_ham_test.listFiles()){
			//num_ham_testfiles = num_ham_testfiles+1;
			HashMap<String, Integer> my_testmap = new HashMap<String, Integer>();
			Scanner sc = new Scanner(testfile);
			while(sc.hasNext()){
				String line = sc.nextLine();
				for(String s: line.toLowerCase().trim().split(" ")){
					s = s.replaceAll("[^a-zA-Z]+", "");

					if(my_testmap.containsKey(s)){
						my_testmap.put(s, my_testmap.get(s)+1);
					}else{
						my_testmap.put(s, 1);
					}
				}	
			}
			sc.close();
			int ham = lr.test(my_testmap);
			if(ham == 0){
				h_count++;

			}
		}


		double ham_acc = ( (double)h_count / (double)num_ham_testfiles)*100;
		System.out.println("Ham % Accuracy : "+ ham_acc);
	}

	private static void getHashmap_spam(File path_to_spam_train) throws Exception {
		for(File file: path_to_spam_train.listFiles()){
			HashMap<String, Integer> spam_filevocab = new HashMap<String, Integer>();

			spam_file_set.add(file.getName());
			all_file_set.add(file.getName());
			Scanner sc = new Scanner(file);
			while(sc.hasNext()){
				String line = sc.nextLine();

				for(String s: line.toLowerCase().trim().split(" ")){
					s = s.replaceAll("[^a-zA-Z]+", "");
					if(distinct_vocab.contains(s)){

						if(my_spam_wordmap.containsKey(s)){
							my_spam_wordmap.put(s, my_spam_wordmap.get(s)+1);
						}else{
							my_spam_wordmap.put(s, 1);
						}

						if(spam_filevocab.containsKey(s)){
							spam_filevocab.put(s, spam_filevocab.get(s)+1);
						}
						else{
							spam_filevocab.put(s, 1);
						}
					}

					spam_filemap.put(file.getName(), spam_filevocab);
				}
			}
			sc.close();
		}

	}

	private static void getHashmap_ham(File path_to_ham_train) throws Exception {


		for(File file: path_to_ham_train.listFiles()){
			HashMap<String, Integer> ham_filevocab = new HashMap<String, Integer>();
			ham_file_set.add(file.getName());
			all_file_set.add(file.getName());

			Scanner sc = new Scanner(file);
			while(sc.hasNext()){
				String line = sc.nextLine();
				for(String s: line.toLowerCase().trim().split(" ")){
					s = s.replaceAll("[^a-zA-Z]+", "");
					if(!s.isEmpty()){

						if(distinct_vocab.contains(s)){
							if(my_ham_wordmap.containsKey(s)){
								my_ham_wordmap.put(s, my_ham_wordmap.get(s)+1);
							}else{
								my_ham_wordmap.put(s, 1);
							}

						}	
					}	

					if(!s.isEmpty()){

						if(distinct_vocab.contains(s)){
							if(ham_filevocab.containsKey(s)){
								ham_filevocab.put(s, ham_filevocab.get(s)+1);
							}else{
								ham_filevocab.put(s, 1);
							}
						}
					}
					ham_filemap.put(file.getName(), ham_filevocab);
				}
			}
			sc.close();
		}
	}


	private static void addDistinct(File path_to_spam_train) throws Exception {

		for(File file: path_to_spam_train.listFiles()){

			Scanner scanner = new Scanner(file);
			while(scanner.hasNext()){
				String line = scanner.nextLine();
				for(String s : line.toLowerCase().trim().split(" ")){
					s = s.replaceAll("[^a-zA-Z]+", "");
					if(!s.isEmpty()){
						distinct_vocab.add(s);
					}
				}
			}
			scanner.close();

		}
	}
}
	class LogisticRegressionAlgorithm {
	static HashMap<String, HashMap<String, Integer>> ham_filemap_LR = new HashMap<String, HashMap<String,Integer>>();
	static HashMap<String,HashMap<String,Integer>> spam_filemap_LR = new HashMap<String, HashMap<String,Integer>>();
	static HashMap<String, Integer> my_ham_wordmap_LR = new HashMap<String, Integer>();
	static HashMap<String, Integer> my_spam_wordmap_LR = new HashMap<String, Integer>();
	HashMap<String, Double>file_expected_sum_weights = new HashMap<String, Double>();

	double learning_rate_LR = 0.0 ;
	double lambda_LR = 0; 
	int num_iterations_LR =0;
	String path_LR = new String();
	static Set<String> distinct_vovab_LR = new HashSet<String>();
	static double w0 = 0.1;
	static Set<String> ham_file_set = new HashSet<String>();
	static Set<String> spam_file_set = new HashSet<String>();
	static Set<String> all_file_set = new HashSet<String>();

	static HashMap<String, Double> weighted_wordmap = new HashMap<String, Double>();
	static HashMap<String, Double> new_weighted_wordmap = new HashMap<String, Double>();

	public LogisticRegressionAlgorithm(String path, Set<String> distinct_vocab,
			HashMap<String, HashMap<String, Integer>> ham_filemap,
			HashMap<String, HashMap<String, Integer>> spam_filemap,
			HashMap<String, Integer> my_ham_wordmap,
			HashMap<String, Integer> my_spam_wordmap, double learning_rate, double lambda, int num_iterations, Set<String> all_file_set2, Set<String> spam_file_set2, Set<String> ham_file_set2){
		
		path_LR = path;
		
		distinct_vovab_LR = distinct_vocab;
		ham_filemap_LR = ham_filemap;
		spam_filemap_LR = spam_filemap;
		
		my_ham_wordmap_LR = my_ham_wordmap;
		my_spam_wordmap_LR = my_spam_wordmap;
		
		learning_rate_LR = learning_rate;
		lambda_LR = lambda; 
		num_iterations_LR = num_iterations;
	
		all_file_set = all_file_set2;
		spam_file_set = spam_file_set2;
		ham_file_set = ham_file_set2;


	}

	public void train() {
		int counter = 0;
		for(String s:distinct_vovab_LR){
		
			double r =  (Math.random() * (1 -(-1))) + (-1);
			weighted_wordmap.put(s, r); // assign random weights
		}

		for(int i =0 ; i<1; i++){

			for(String currentword : distinct_vovab_LR){
				double delta_wterror = 0;

				for(String filename : all_file_set){
					double classofthisfile;
					int count_currentword = getCountcurrentword(filename, currentword);
					if(spam_file_set.contains(filename)){
						classofthisfile = 1; //spam
					}
					else{
						classofthisfile = 0;//ham
					}
					double Sigmoidofthisfile = calculateWeightofthisfile(filename);
					double error = (classofthisfile - Sigmoidofthisfile);
					delta_wterror = delta_wterror + count_currentword*error;
				}
				
				double new_weight_forWord = weighted_wordmap.get(currentword) + learning_rate_LR*delta_wterror -(learning_rate_LR*lambda_LR*weighted_wordmap.get(currentword));
				weighted_wordmap.put(currentword, new_weight_forWord);
			}
		}
	
	}

	
	
	private int getCountcurrentword(String filename, String currentword) {
		int count = 0;
		if(ham_file_set.contains(filename)){
			try {
				for(Entry<String, Integer> wordcount1: ham_filemap_LR.get(filename).entrySet()){
					if(wordcount1.getKey().equals(currentword)){
						count = wordcount1.getValue();
						return count;
					}
				}
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		else if(spam_file_set.contains(filename)){

			try {
				for(Entry<String, Integer> wordcount: spam_filemap_LR.get(filename).entrySet()){
					if(wordcount.getKey().equals(currentword)){
						count = wordcount.getValue();
						return count;
					}
				}
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		return 0;
	}

	
	
	private double calculateWeightofthisfile(String filename) {

		if(ham_file_set.contains(filename)){
			double weightedsum = w0;
			try{
				for(Entry<String, Integer> values_map: ham_filemap_LR.get(filename).entrySet()){
					weightedsum = weightedsum + weighted_wordmap.get( values_map.getKey() )  * values_map.getValue();
				}	
			}
			catch(Exception E){
				System.out.println( E);
				
			}
			return (Sigmod(weightedsum) );
		}

		else{
			double weightedsum1 = w0;
			try{
				for(Entry<String, Integer> values_map1: spam_filemap_LR.get(filename).entrySet()){
					weightedsum1 = weightedsum1 + weighted_wordmap.get( values_map1.getKey() )  * values_map1.getValue();
				}	
			}
			catch(Exception e){
				System.out.println("..");
			}

			return (Sigmod(weightedsum1) );
		}


	}

	private double Sigmod(double weightedsum) {
		if(weightedsum>100){
			return 1.0;
		}
		else if(weightedsum<-100){

			return 0.0;
		}
		else{
			return (1.0 /(1.0+ Math.exp(-weightedsum)));
		}
	}

	public HashMap<String, Integer> getmyWordsinthisfile(String filename) {

		if(ham_file_set.contains(filename)){
			return ham_filemap_LR.get(filename);
		}
		else{
			return spam_filemap_LR.get(filename);
		}

	}

	public int test(HashMap<String, Integer> my_testmap) {
		double test_sum = 0;
		for(Entry<String, Integer> ent :my_testmap.entrySet()){
			if(weighted_wordmap.containsKey(ent.getKey())){
				test_sum = test_sum + (weighted_wordmap.get(ent.getKey())* ent.getValue());
			}
		}
		test_sum = test_sum+w0;
		if(test_sum>=0){
			return 1;
		}
		else{
			return 0;
		}
	}

}

