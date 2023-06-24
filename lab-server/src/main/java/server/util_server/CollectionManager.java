package server.util_server;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import common.data.Discipline;
import common.comparator.DisciplineComparator;
import common.data.Difficulty;
import common.data.LabWork;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import common.exceptions.*;
/**
 * Class which keeps your collection of LabWorks and has methods for work with it
 * @author Arsenev Denis
 */
@XStreamAlias("ArrayDeque")
public class CollectionManager {

    private final Lock readLock;
    private final Lock writeLock;

    private static long idCounter = 1;
    /**
     *Field, which keeps your collection
     */
    @XStreamImplicit
    private ArrayDeque<LabWork> collection;
    /**
     * Field, which keeps date of initialization of the element in collection
     */
    private LocalDateTime initializationDate;

    /**
     * Field, which keeps name of loaded file with collection
     */
    private String fileName;

    /**
     * Basic constructor, which sets date of initialization and filename
     */
    public CollectionManager() {
        this.initializationDate = LocalDateTime.now();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    public void setLabWorks(ArrayDeque<LabWork> labWorks) {
        try {
            writeLock.lock();
            this.collection = labWorks;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Setter of initialization date
     * @param initializationDate Date of initialization
     */
    public void setInitializationDate(LocalDateTime initializationDate) {
        this.initializationDate = initializationDate;
    }
    /**
     * Getter of collection
     * @return Collection
     */
    public ArrayDeque<LabWork> getCollection() {
        try {
            readLock.lock();
            return collection;
        } finally {
            readLock.unlock();
        }
    }

    public void reassignIds() {
        try {
            writeLock.lock();
            for (LabWork lab : collection) {
                lab.setId(idCounter++);
            }
        } finally {
            writeLock.unlock();
        }
    }
    /**
     * Method, which adds new element in collection
     * @param lab New element to add
     */
    public void addLabWork(LabWork lab) {
        try {
            writeLock.lock();
            collection.add(lab);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean checkMin(LabWork labWork) {
        try {
            readLock.lock();
            boolean check = true;
            for (LabWork lab : collection) {
                if (lab.compareLab(labWork) <= 0) {
                    check = false;
                    break;
                }
            }
            return check;
        } finally {
            readLock.unlock();
        }
    }

    public List<Long> returnIDsOfLower(LabWork labWork) {
        try {
            readLock.lock();
            List<Long> ids = new ArrayList<>();
            for (LabWork lab : collection) {
                if (lab.compareLab(labWork) <= 0) {
                    ids.add(lab.getId());
                }
            }
            return ids;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Getter of filename
     * @return String name of file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter of filename
     * @param fileName String name of file
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Method, which returns type of the collection
     * @return Type of the collection
     */
    public String collectionType() {
        try {
            readLock.lock();
            return collection.getClass().getName();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Method, which returns number of elements in the collection
     * @return Int number of elements
     */
    public int collectionSize() {
        try {
            readLock.lock();
            return collection.size();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Method, which prints main information about collection
     */
    public String info() {
        try {
            readLock.lock();
            return "Type of collection: " + collectionType() + ",\n" + "Initialization date: " + initializationDate
                    + ",\n" + "Amount of elements: " + collectionSize();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Method, which sorts collection by ID of LabWorks
     */
    public void sortByID() {
        List<LabWork> sorted = new ArrayList<>(collection);
        sorted.sort(Comparator.comparingLong(LabWork::getId));
        collection.clear();
        collection.addAll(sorted);
    }
    /**
     * Method, which clears your collection by deleting all the elements
     */
    public void clear() {
        collection.clear();
    }

    /**
     * Method, which prints every element of the collection
     */
    public String show() {
        if (collection.isEmpty()) {
            return "Collection is empty";
        } else {
            StringBuilder sb = new StringBuilder();
            for (LabWork lab: collection) {
                sb.append(lab).append("\n");
            }
            sb = new StringBuilder(sb.substring(0, sb.length() - 2));
            return sb.toString();
            }
        }

    /**
     * Method, which prints first element of the collection
     * @throws CollectionIsEmptyException Exception for the case of empty collection
     */
    public LabWork head() throws CollectionIsEmptyException{
        try {
            readLock.lock();
            if (!collection.isEmpty()) {
                return collection.getFirst();
            } else {
                throw new CollectionIsEmptyException("Collection is empty!");
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Method, which deletes element from the collection by ID
     * @param id ID of the element
     * @throws NoSuchIDException Exception for the case when there is no element with such ID
     */
    public void removeByID(Long id) {
        try {
            writeLock.lock();
            collection.removeIf(mb -> Objects.equals(mb.getId(), id));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Method, which changes element by chosen ID
     * @param id ID of the element
     * @param lab New element of the collection
     * @throws NoSuchIDException Exception for the case when there is no element with such ID
     */
    public void updateByID(Long id, LabWork lab) {
        try {
            writeLock.lock();
            if (collection.removeIf(mb -> Objects.equals(mb.getId(), id)))
                lab.setId(id);
            collection.add(lab);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Method, which adds new element in the collection if it's lower than others
     * @param lab New element of the collection
     * @throws ElementIsNotMinException Exception for the case if such element is not minimal
     */
    public void addIfMin(LabWork lab) throws ElementIsNotMinException {
        for (LabWork lr: collection) {
            if (lr.compareLab(lab) <= 0) {
                throw new ElementIsNotMinException("This element is not minimal, try another one!");
            }
        }
        addLabWork(lab);
    }

    /**
     * Method, which removes lower element than given
     * @param lab Element for comparison
     * @throws CollectionIsEmptyException Exception for the case of empty collection
     */
    public ArrayDeque<LabWork> removeLower(LabWork lab) throws CollectionIsEmptyException {
        ArrayDeque<LabWork> copy = new ArrayDeque<>(collection);
        if (!collection.isEmpty()) {
            collection.removeIf(mb -> mb.compareLab(lab) <= 0);
            copy.removeAll(collection);
        } else {
            throw new CollectionIsEmptyException("Collection is empty");
        }
        return copy;
    }

    /**
     * Method, which prints elements with less maximum points than given
     * @param maximumPoints Integer number of maximum points
     * @throws CollectionIsEmptyException Exception for the case of empty collection
     * @throws NoMaxPointsException Exception for the case of absence of the element
     */
    public ArrayDeque<LabWork> filterLessThanMaximumPoint(Integer maximumPoints) throws CollectionIsEmptyException, NoMaxPointsException{
        try {
            readLock.lock();
            boolean flag = true;
            ArrayDeque<LabWork> copy = new ArrayDeque<>();
            if (!collection.isEmpty()) {
                for (LabWork lab : collection) {
                    if (lab.getMaximumPoint() != null) {
                        if (lab.getMaximumPoint() < maximumPoints) {
                            flag = false;
                            copy.add(lab);
                        }
                    }
                }
                if (flag) {
                    throw new NoMaxPointsException("There are no elements with appropriate maximum points!");
                }
            } else {
                throw new CollectionIsEmptyException("Collection is empty!");
            }
            return copy;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Method, which prints elements with the difficulty greater than given
     * @param difficulty Difficulty of the element for comparison
     * @throws CollectionIsEmptyException Exception for the case of empty collection
     * @throws NoDifficultyException Exception for the case of absence of the difficulty in each element of the collection
     */
    public ArrayDeque<LabWork> filterGreaterThanDifficulty(Difficulty difficulty) throws CollectionIsEmptyException, NoDifficultyException {
        try {
            readLock.lock();
            boolean flag1 = true;
            boolean flag2 = true;
            ArrayDeque<LabWork> copy = new ArrayDeque<>();
            if (!collection.isEmpty()) {
                for (LabWork lab : collection) {
                    if (lab.getDifficulty() != null) {
                        flag1 = false;
                        if (lab.compareDifficulty(difficulty) > 0) {
                            flag2 = false;
                            copy.add(lab);
                        }
                    }
                }
                if (flag1) {
                    throw new NoDifficultyException("There are no elements with difficulty!");
                }
                if (flag2) {
                    return null;
                }
                return copy;
            } else {
                throw new CollectionIsEmptyException("Collection is empty!");
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Method, which prints elements in descending order comparing them by their disciplines
     * @throws CollectionIsEmptyException Exception for the case of empty collection
     */
    public ArrayDeque<Discipline> printFieldDescendingDiscipline() throws CollectionIsEmptyException{
        try {
            readLock.lock();
            if (!collection.isEmpty()) {
                List<LabWork> sortedLabs = new ArrayList<>(collection);
                ArrayDeque<Discipline> copy = new ArrayDeque<>();
                sortedLabs = sortedLabs.stream().sorted((new DisciplineComparator()).reversed()).collect(Collectors.toList());
                //sortedLabs.sort((new DisciplineComparator()).reversed());
                for (LabWork lr : sortedLabs) {
                    copy.add(lr.getDiscipline());
                }
                return copy;
            } else {
                throw new CollectionIsEmptyException("Collection is empty!");
            }
        } finally {
            readLock.unlock();
        }
    }
}
