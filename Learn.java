import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

@SuppressWarnings("unused")
public class Learn
{
	private static final int DEFAULT_EPOCH_SIZE = 1000000;// 11743281;
	private static final int SEQUENCE_SIZE = 10;
	private static final double THRESHOLD = 0.9;

	private static Database db = new Database();
	private static IndexMap indexMap;

	public static void main(String[] args)
	{
		String fileName = null;
		int epochSize = DEFAULT_EPOCH_SIZE;
		if (args.length < 1)
		{
			System.err.println("Error: Missing input file name");
			System.exit(-1);
		}
		else
		{
			fileName = args[0];
			if(args.length > 1)
			{
				epochSize = Integer.parseInt(args[1]);
			}
		}

//		 redirectOutput();

		indexMap = new IndexMap(fileName);

		System.out.println("Number of distinct system calls = " + indexMap.size());
		System.out.println("Epoch size: " + epochSize);
		try
		{
			final BufferedReader reader = new BufferedReader(new FileReader(new File(fileName + "_trace")));

			Database currentSnapshot = null, previousSnapshot = null, currentEpochChange = null, previousEpochChange = null;
			double oldSimilarity = 0, similarity = 0;
			Epoch epoch = null;

			boolean success = false;
			for (int i = 0; i == 0 || !epoch.isLastEpoch; i++)
			{
				previousSnapshot = currentSnapshot;
				previousEpochChange = currentEpochChange;
				oldSimilarity = similarity;

				System.out.println("\nStarting epoch " + i);
				epoch = new Epoch(reader, epochSize);
				currentSnapshot = epoch.run();
				System.out.println("Current database size: " + db.size());
//				System.out.println("Curr Snapshot: " + Arrays.toString(currentSnapshot.getValues()));
				// epoch.print();
				// db.print();

				if (i == 0)
				{
					// First epoch .. Nothing to compare to
					currentEpochChange = currentSnapshot;
					continue;
				}
				if(i < 2)
				{
					// No similarity calculated until after 2nd epoch
					oldSimilarity = currentSnapshot.size();
				}

				currentEpochChange = currentSnapshot.getChange(previousSnapshot);
//				similarity = currentEpochChange.calculateSimilarity(previousEpochChange);
				similarity = currentEpochChange.calculateDistance(previousEpochChange);

//				System.out.println("Prev Snapshot: " + Arrays.toString(previousSnapshot.getValues()));
//				System.out.println("Prev Change: " + Arrays.toString(previousEpochChange.getValues()));
//				System.out.println("Curr Change: " + Arrays.toString(currentEpochChange.getValues()));
				System.out.println("Similarity: " + similarity);

//				if(similarity > THRESHOLD)
				if(similarity < currentSnapshot.size() && oldSimilarity < previousSnapshot.size())
				{
					success = true;
					System.out.println("\nTraining Complete after " + i + " epochs");
					break;
				}
			}
			reader.close();
			if(!success)
			{
				System.out.println("\nTraining failed!");
			}
		}
		catch (IOException e)
		{
			System.err.println(e);
			System.exit(-2);
		}
	}

	private static void redirectOutput()
	{
		try
		{
			System.setOut(new PrintStream(new File("log")));
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e);
		}
	}

	private static class Database extends HashMap<ArrayList<Byte>, Integer>
	{
		private static final long serialVersionUID = 1L;

		private Database getChange(Database snapshot)
		{
			Database difference = new Database();
			for (ArrayList<Byte> entry : keySet())
			{
				if (snapshot.containsKey(entry))
				{
					// Entry already exists -> Calculate difference
					difference.put(entry, Math.abs(this.get(entry) - snapshot.get(entry)));
				}
				else
				{
					// New entry
					difference.put(entry, get(entry));
				}
			}
			return difference;
		}

		private double calculateSimilarity(Database before)
		{
			double dot = 0, norm1 = 0 , norm2 = 0;
			
			for(ArrayList<Byte> entry : keySet())
			{
				int val = get(entry);
				norm1 += val * val;
//				System.out.print(norm1 + ", ");
			}
			System.out.println(norm1);

			for(ArrayList<Byte> entry : keySet())
			{
				int newVal = get(entry);
				norm2 += newVal * newVal;
//				System.out.print(norm2 + ", ");
			}
			System.out.println(norm2);
			
			for (ArrayList<Byte> entry : keySet())
			{
				int newVal = get(entry);
				if (before.containsKey(entry))
				{
					int oldVal = before.get(entry);
					dot += oldVal * newVal;
//					norm1 += oldVal * oldVal;
				}
//				norm2 += newVal * newVal;
//				System.out.print(dot + ", ");
			}
			System.out.println(dot);
			double result = dot/norm1;
			result /= norm2;
			return result;// dot / (norm1 * norm2);
		}

		private int calculateDistance(Database before)
		{
			int distance = 0;
			for (ArrayList<Byte> entry : keySet())
			{
				if (before.containsKey(entry))
				{
					distance += Math.abs(get(entry) - before.get(entry));// * get(entry);
				}
				else
				{
					distance += get(entry);// * get(entry);
				}
			}
			return distance;
		}

		private void add(ArrayList<Byte> entry)
		{
			if (containsKey(entry))
			{
				replace(entry, get(entry) + 1);
			}
			else
			{
				put(entry, 1);
			}
		}

		private Integer[] getValues()
		{
			Integer[] frequencies = new Integer[values().size()];
			values().toArray(frequencies);
			return frequencies;
		}

		private void print()
		{
			for (ArrayList<Byte> entry : keySet())
			{
				System.out.println(entry + " => " + get(entry));
			}
		}
	}

	private static class Epoch extends ArrayList<String>
	{
		private static final long serialVersionUID = 1L;

		private boolean isLastEpoch = false;

		private Epoch(BufferedReader reader, int epochSize) throws IOException
		{
			String syscall;
			for (int i = 0; i < epochSize; i++)
			{
				if ((syscall = reader.readLine()) != null)
				{
					add(syscall);
				}
				else
				{
					isLastEpoch = true;
					return;
				}
			}
		}

		private Database run()
		{
			for (int i = 0; i < size(); i++)
			{
				db.add(getCounts(i));
			}
			return (Database) db.clone();
		}

		private ArrayList<Byte> getCounts(int start)
		{
			final int num_syscalls = indexMap.size() + 1;
			final ArrayList<Byte> window = new ArrayList<Byte>(Collections.nCopies(num_syscalls, (byte) 0));
			for (int i = start; i < start + SEQUENCE_SIZE && i < size(); i++)
			{
				String syscall = get(i);
				int index = indexMap.get(syscall);
				Byte count = window.get(index);
				window.set(index, ++count);
			}
			// System.out.println(window);
			return window;
		}

		private void print()
		{
			System.out.println(this);
		}
	}

	private static class IndexMap extends HashMap<String, Integer>
	{
		private static final long serialVersionUID = 1L;
//		private static final int IGNORE_THRESHOLD = 10;

		private IndexMap(String fileName)
		{
			// Build index map from count file
			try
			{
				final BufferedReader reader = new BufferedReader(new FileReader(new File(fileName + "_count")));
				String line;
				while ((line = reader.readLine()) != null)
				{
					String[] words = line.split("\t");
					if (Integer.parseInt(words[1]) < size())
					{
						break;
					}
					put(words[0], size());
				}
				reader.close();
			}
			catch (IOException e)
			{
				System.err.println(e);
				System.exit(-2);
			}
		}

		private int get(String syscall)
		{
			if (containsKey(syscall))
			{
				return super.get(syscall);
			}
			else
			{
				return size();
			}
		}

		private void print()
		{
			for (String entry : keySet())
			{
				System.out.println(entry + " => " + get(entry));
			}
		}
	}
}
