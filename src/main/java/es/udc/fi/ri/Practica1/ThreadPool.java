package es.udc.fi.ri.Practica1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexWriter;

public class ThreadPool {

	public static void Pool(int numThreads, Path path, IndexWriter indexWriter) {
		// Creamos x Threads
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		if (Files.isDirectory(path)) {
			File[] files = new File(path.toString()).listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					System.out.println(file.toString());
					Runnable thread = new IndexerThread(file.toString(),indexWriter);
					executor.execute(thread);
				}
			}
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
	}
	
	public static ArrayList<IndexWriter> Pool(int numThreads, Path path, String indexPath,String mode) {
		// Creamos x Threads
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		ArrayList<IndexWriter> index = new ArrayList<>();
		if (Files.isDirectory(path)) {
			File[] files = new File(path.toString()).listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
	        		String[] parts = file.toString().split(Pattern.quote(File.separator));
	        		String folder= parts[parts.length-1]; 
	        		String newFolder = indexPath+"\\"+folder;
	        		new File(newFolder).mkdir();
        			IndexWriter indexWriter = IndexFiles.getIndexWriter(newFolder, mode);
        			System.out.println(file.toString());
					Runnable thread = new IndexerThread(file.toString(),indexWriter);
					executor.execute(thread);
        			index.add(indexWriter);
	        		
				}
			}
		}executor.shutdown();
		while (!executor.isTerminated()) {
		}
		return index;
	}
	
}
