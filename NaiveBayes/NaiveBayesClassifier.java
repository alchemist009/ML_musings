import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Scanner;
import java.util.Set;
import java.util.HashMap;
import java.util.Map.Entry;


public class NaiveBayesClassifier {

	public static Set<String> distinct_vocab = new HashSet<String>();
	public static TreeMap <String, Integer> spam_wordmap = new TreeMap<String, Integer>();
	public static TreeMap<String, Integer> ham_wordmap = new TreeMap<String, Integer>();
	public static Set<String> Stopword_list = new HashSet<String>();

	public static void main(String[] args) throws Exception{
		if (args.length < 2) {
			System.out.println(" Kindly provide input in the following format :");
			System.out.println(" <path-to-directory-containing-test-and-training-dirs> <yes/no : To remove stopword or not> ");
			System.exit(1);
		} 
		String path = args[0];
		String to_filter = args[1];
		File path_to_spam_train = new File(path+"/train/spam");
		File path_to_ham_train = new File(path+"/train/ham");
		
		File path_to_spam_test = new File(path+"/test/spam");
		File path_to_ham_test = new File(path+"/test/ham");
		File path_to_stopwords = new File(path+"/stopwords.txt");
		String[] splsym = {"!","#","%","^","&","*","(",")","!", ":",".","{","}", "[","]",">","<","?","/", "*","~", "@"};
	
		addDistinct(path_to_spam_train);
		addDistinct(path_to_ham_train);
	
		for(String s1: splsym){
			distinct_vocab.remove(s1);
		}
		
		Scanner s=null;
		try {
			s = new Scanner(path_to_stopwords);
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		while(s.hasNext()){
			String sw = s.next();
			Stopword_list.add(sw);
		}
		s.close();

		getHashmap_spam(path_to_spam_train);
		getHashmap_ham(path_to_ham_train);

		for(String s1: splsym){
			if(spam_wordmap.containsKey(s1) ){
				spam_wordmap.remove(s1);
				
			}
			if(ham_wordmap.containsKey(s1) ){
				ham_wordmap.remove(s1);
				
			}
		}
		if(to_filter.equals("yes")){
			for(String str : Stopword_list){
				if(distinct_vocab.contains(str)){
					distinct_vocab.remove(str);
				}
			}
			for(String stopword : Stopword_list){
				if(spam_wordmap.containsKey(stopword) ){
					spam_wordmap.remove(stopword);
				}
				if(ham_wordmap.containsKey(stopword) ){
					ham_wordmap.remove(stopword);
				}
			}
		}
		
		NaiveBayesAlgorithm nb = new NaiveBayesAlgorithm(ham_wordmap,spam_wordmap, distinct_vocab);
		nb.train(1);
		// Priors//
		double priorSpam_probability = 
				1.0*(path_to_spam_train.listFiles().length)/(  path_to_spam_train.listFiles().length + path_to_ham_train.listFiles().length ) ;

		double priorHam_probability = 1.0 - priorSpam_probability;

		double l_priorSpam_probability = Math.log(priorSpam_probability);
		double l_priorHam_probability = Math.log(priorHam_probability);
		//-----
		
		if(to_filter.equals("yes")){
			System.out.println("Accuracy of Naive Bayes after removal of Stop Words:");
		}
		else{
			System.out.println("Accuracy of Naive Bayes with out removing Stop Words: ");
		}
		System.out.println();
		
	
		
		double num_correct_spam =0;
		int ns = 0;
		for(File file: path_to_spam_test.listFiles()){
			ns = ns +1;
			if(nb.test_doc(file, l_priorHam_probability, l_priorSpam_probability, Stopword_list,to_filter) == 1){
				num_correct_spam = num_correct_spam + 1.0;
			}
		}
		double spam_accuracy = num_correct_spam/ns; 
		System.out.println("Spam % Accuracy "+ spam_accuracy*100);
		
		
		double num_correct_ham =0;
		int nh=0;
		for(File file: path_to_ham_test.listFiles()){
			nh=nh+1;
			if(nb.test_doc(file, priorHam_probability, priorSpam_probability,Stopword_list,to_filter) == 0){
				num_correct_ham = num_correct_ham + 1.0;
			}
		}
		System.out.println();
		double ham_accuracy = num_correct_ham/nh; 
		System.out.println("Ham % Accuracy : "+ ham_accuracy*100);
		
		
		System.out.println();
		System.out.println("Overall Accuracy  : "+( (num_correct_ham+num_correct_spam)/(ns+nh))*100);
		 

	}


	private static void getHashmap_spam(File path_to_spam_train) throws Exception {
		for(File file: path_to_spam_train.listFiles()){
			Scanner sc = new Scanner(file);
			while(sc.hasNext()){
				String line = sc.nextLine();
				for(String s: line.toLowerCase().trim().split(" ")){
					if(!s.isEmpty()){
						
						if(spam_wordmap.containsKey(s)){
							spam_wordmap.put(s, spam_wordmap.get(s)+1);
						}else{
							spam_wordmap.put(s, 1);
						}
					}
				}
			}
			sc.close();
		}

	}


	private static void getHashmap_ham(File path_to_ham_train) throws Exception {

		for(File file: path_to_ham_train.listFiles()){
			Scanner sc = new Scanner(file);
			while(sc.hasNext()){
				String line = sc.nextLine();
				for(String s: line.toLowerCase().trim().split(" ")){
					if(!s.isEmpty()){
						
						if(ham_wordmap.containsKey(s)){
							ham_wordmap.put(s, ham_wordmap.get(s)+1);
						}else{
							ham_wordmap.put(s, 1);
						}
					}	
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
					if(!s.isEmpty()){
						distinct_vocab.add(s);
					}
				}
			}
			scanner.close();

		}
	}
}
	class NaiveBayesAlgorithm {
	static HashMap<String, Double> spam_map_likelihood = new HashMap<String, Double>();
	static HashMap<String, Double> ham_map_likelihood = new HashMap<String, Double>();

	static TreeMap<String, Integer> spam_map = new TreeMap<String, Integer>();
	static TreeMap<String, Integer> ham_map = new TreeMap<String, Integer>();
	static Set<String> Vocabset = new HashSet<String>();
	static int stotal=0;
	static int htotal=0;

	public NaiveBayesAlgorithm(TreeMap<String,Integer> ham_wordmap,
			TreeMap<String, Integer> spam_wordmap, Set<String> distinct_vocab) {

		spam_map = spam_wordmap;
		ham_map = ham_wordmap;
		Vocabset = distinct_vocab;

	}
	public int train(int i){

		int spam_totalterms =0;
		for(Entry<String, Integer> entry: spam_map.entrySet()){
			spam_totalterms = spam_totalterms + entry.getValue();
		}
		

		int ham_totalterms =0;
		for(Entry<String, Integer> entry: ham_map.entrySet()){
			ham_totalterms = ham_totalterms + entry.getValue();
		}
		
		for(String s : Vocabset){

			if(spam_map.containsKey(s)){

				double spam_likely = (spam_map.get(s)+1.0)/(spam_totalterms+Vocabset.size()+1.0);
				double spam_loglikely = Math.log(spam_likely);
				spam_map_likelihood.put(s, spam_loglikely);
			}			
		}
		for(String s : Vocabset){

			if(ham_map.containsKey(s)){

				double ham_likely = (ham_map.get(s)+1.0)/(ham_totalterms+Vocabset.size()+1.0);
				double ham_loglikely = Math.log(ham_likely);
				ham_map_likelihood.put(s, ham_loglikely);
			}
		
		}
		stotal = spam_totalterms;
		htotal = ham_totalterms;
		
		return 1;
	}

	public int test_doc(File file, double priorHam_probability, double priorSpam_probability) throws Exception {

		double current_spamprobability= 0.0;
		double current_hamprobability= 0.0;
		Scanner scanner = new Scanner(file);
		while(scanner.hasNext()){
			String line = scanner.nextLine();
		

			for(String s : line.toLowerCase().split(" ")){
					
					if(spam_map_likelihood.containsKey(s)){
						current_spamprobability= current_spamprobability+ spam_map_likelihood.get(s);
					}else{

						current_spamprobability= current_spamprobability+ Math.log(1.0 / (stotal + Vocabset.size()+1.0)) ;

					}
					if(ham_map_likelihood.containsKey(s)){
						current_hamprobability= current_hamprobability+ ham_map_likelihood.get(s);
					}else{
						current_hamprobability= current_hamprobability+  Math.log( 1.0 / (htotal + Vocabset.size()+1.0));
					}

				
			}
		}
		scanner.close();
		current_spamprobability= current_spamprobability+ priorSpam_probability;
		current_hamprobability= current_hamprobability+ priorSpam_probability;

		if(current_spamprobability> current_hamprobability){
			return 1; // spam
		}

		else{
			return 0;
		}
	
	}

	public int test_doc(File file, double priorHam_probability, double priorSpam_probability, Set<String> stopword_list, String tofilter) throws Exception {

		double current_spamprobability= 0.0;
		double current_hamprobability= 0.0;
		Scanner scanner = new Scanner(file);
		while(scanner.hasNext()){
			String line = scanner.nextLine();
            if(tofilter.equals("yes") ){
            	for(String s : line.toLowerCase().split(" ")){
        
            		if(!stopword_list.contains(s)){
    					if(spam_map_likelihood.containsKey(s)){
    						current_spamprobability= current_spamprobability+ spam_map_likelihood.get(s);
    					}else{

    						current_spamprobability= current_spamprobability+ Math.log(1.0 / (stotal + Vocabset.size()+1.0)) ;

    					}
    					if(ham_map_likelihood.containsKey(s)){
    						current_hamprobability= current_hamprobability+ ham_map_likelihood.get(s);
    					}else{
    						current_hamprobability= current_hamprobability+  Math.log( 1.0 / (htotal + Vocabset.size()+1.0));
    					}
            		}
    			}
            }
            else{
            	for(String s : line.toLowerCase().split(" ")){
            			
    					if(spam_map_likelihood.containsKey(s)){
    						current_spamprobability= current_spamprobability+ spam_map_likelihood.get(s);
    					}else{

    						current_spamprobability= current_spamprobability+ Math.log(1.0 / (stotal + Vocabset.size()+1.0)) ;

    					}
    					if(ham_map_likelihood.containsKey(s)){
    						current_hamprobability= current_hamprobability+ ham_map_likelihood.get(s);
    					}else{
    						current_hamprobability= current_hamprobability+  Math.log( 1.0 / (htotal + Vocabset.size()+1.0));
    					}

    				}	
    			}
            
		}
		scanner.close();
		current_spamprobability= current_spamprobability+ priorSpam_probability;
		current_hamprobability= current_hamprobability+ priorSpam_probability;

		if(current_spamprobability> current_hamprobability){
			return 1; // spam
		}

		else{
			return 0;
		}

	}


}

