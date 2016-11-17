import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.file.*;


public class HangmanGame implements Callable <List<String>>{
	private File file;
	
	public static void main(String[] args){
		int answer = -1;	//-1 denotes no valid answer yet
		
		File[] files = new File[4];
		for (int i = 0; i<4; i++){	//Set the file objects for our text files up
			files[i] = new File("file" + (i+1) + ".txt");
		}
		
		Path savePath = Paths.get("saveFile.bin");	//prepare the save file for our random lists array
		
		List<String>[] lists = new List[4];
		
		Scanner scanner = new Scanner(System.in);	//prepare a reader for the non-gui version
		
		System.out.println("Would you like to extract the words for your Hangman\ngame with multithreading? y/n");
		while(answer == -1){
			char c = scanner.next().trim().charAt(0);
			switch(c){
				case 'y': case 'Y': answer = 1; break;
				case 'n': case 'N': answer = 0; break;
				default: break;
			}
		}
		long beforeTime = System.currentTimeMillis();
		if (answer == 0){
			for(int i = 0; i<=3; i++){
				lists[i] = getRandomFromList( getWords(files[i]), 50 );
				System.out.println(lists[i].size());
			}
		} else if (answer == 1){
			ExecutorService es = Executors.newFixedThreadPool(4);
			
			Future<List<String>>[] threads = new Future[4];
			
			for(int i = 0; i<4; i++){
				HangmanGame hangmanThread = new HangmanGame(files[i]);
				threads[i] = es.submit(hangmanThread);
			}
			
			for(int i = 0; i<4; i++){
				try {
					lists[i] = threads[i].get();
				}
				catch (ExecutionException e){
					System.out.println(e.getMessage());
				} 
				catch (InterruptedException e){
					System.out.println(e.getMessage());
				}
			}
			for(int i = 0; i<4; i++){
				System.out.println(lists[i].size());
			}
			es.shutdown();
		}
		
		System.out.println("Reading and writing the words to and\nfrom the files took " + (System.currentTimeMillis()-beforeTime) + " Milliseconds");
		System.out.println(Arrays.toString(lists)); 
			
		serialize(lists, savePath);
	}
	
	public HangmanGame(File fileTarget){
		file = fileTarget;
	}
	
	public static List<String> getWords(File file){
		List<String> words = new ArrayList<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file));){
			String line;
			while ( (line = reader.readLine()) != null ) {
			    words.add(line);
			}	
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}
		return words;
	}
	
	public static List<String> getRandomFromList(List<String> fullList, int sizeInt){
		List<String> listCopy = new ArrayList<String>(fullList);
		List<String> randList = new ArrayList<String>();
		Random randNo = new Random();
		while(listCopy.size() > 0 && randList.size() < sizeInt){
			randList.add(listCopy.remove(randNo.nextInt(listCopy.size())));
		}
		return randList;
	}
	
	public static void serialize(List<String>[] list, Path path){
		try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))) {
			out.writeObject(list);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public List<String> call(){
		return new ArrayList<String>(getRandomFromList( getWords(file), 50 ));
	}
}