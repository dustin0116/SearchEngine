import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Data structure for the Inverted Index.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @author Yen Dah Hsiang
 * @version Fall 2019
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	/** Custom lock. */
	private final SimpleReadWriteLock lock;

	/** Constructor for thread safe inverted index. */
	public ThreadSafeInvertedIndex() {

		super();
		lock = new SimpleReadWriteLock();
	}

	@Override
	public void add(Collection<String> words, String location, int start) {

		try {

			lock.writeLock().lock();
			super.add(words, location, start);

		} finally {

			lock.writeLock().unlock();
		}
	}

	@Override
	public void add(String string, String location, int position) {

		try {

			lock.writeLock().lock();
			super.add(string, location, position);

		} finally {

			lock.writeLock().unlock();
		}
	}

	@Override
	public Map<String, Integer> getAllWordCount() {

		try {

			lock.readLock().lock();
			return super.getAllWordCount();

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getLocations() {

		try {

			lock.readLock().lock();
			return super.getLocations();

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getLocations(String word) {

		try {

			lock.readLock().lock();
			return super.getLocations(word);

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public Set<Integer> getPositions(String word, String location) {

		try {

			lock.readLock().lock();
			return super.getPositions(word, location);

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public int getTotalWords() {

		try {

			lock.writeLock().lock();
			return super.getTotalWords();

		} finally {

			lock.writeLock().unlock();
		}
	}

	@Override
	public int getWordCount(String location) {

		try {

			lock.readLock().lock();
			return super.getWordCount(location);

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getWords() {

		try {

			lock.readLock().lock();
			return super.getWords();

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public boolean hasLocation(String location) {

		try {

			lock.readLock().lock();
			return super.hasLocation(location);

		} finally {

			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean hasLocation(String word, String location) {

		try {

			lock.readLock().lock();
			return super.hasLocation(word, location);

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public boolean hasPosition(String word, String location, int position) {

		try {

			lock.readLock().lock();
			return super.hasPosition(word, location, position);

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public boolean hasWord(String word) {

		try {

			lock.readLock().lock();
			return super.hasWord(word);

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<Result> exactSearch(Collection<String> queries) {

		try {

			lock.readLock().lock();
			return super.exactSearch(queries);

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<Result> partialSearch(Collection<String> queries) {

		try {

			lock.readLock().lock();
			return super.partialSearch(queries);

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex local) {

		try {

			lock.writeLock().lock();
			super.addAll(local);

		} finally {

			lock.writeLock().unlock();
		}
	}

	@Override
	public void wordCountsToJson(Path path) throws IOException {

		try {

			lock.readLock().lock();
			super.wordCountsToJson(path);

		} finally {

			lock.readLock().unlock();
		}
	}

	@Override
	public void indexToJson(Path path) throws IOException {

		try {

			lock.readLock().lock();
			super.indexToJson(path);

		} finally {

			lock.readLock().unlock();
		}
	}

}
