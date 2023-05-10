import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.Random;
import java.lang.System;

// багатопотокова програма для пошуку мінімального елемента в масиві
public class Main {

    private int threadIndexDone = 0; // к-сть завершених потоків
    private final int threadsCount = 8; // к-сть потоків для запуска
    private int minIndex = -1; // індекс мінімального значення
    private int minElement = Integer.MAX_VALUE; // ініціалізації для пошуку мін. елем. в масиві,
    // оскільки вона гарантує, що будь-який елемент у масиві буде меншим за початкове значення
    private ComputeThread[] threads; // змінна threads являє собою масив об'єктів типу ComputeThread,
    // який буде запущений у його власний потік і виконуватиме вирахування. Юзається шоб сворити і запустити кілька потоків одночасно
    public static void main(String[] args) {
        Main main = new Main();  // Створюється екземпляр класу Main і викликається метод EntryPoint().
        main.EntryPoint();
    }

    public void EntryPoint(){
        System.out.println("Threads count: " + threadsCount);
        int[] array = new int[100000000]; // створення масиву і заповнення рандом числами
        RandomizeArray(array);

        long startTime = System.nanoTime(); // використовується для вимірювання часу, що минув, із точністю до наносекунд,
        // зберігає значення, яке повертає на початку виконання програми а потім використовується для обчислення загального часу, що минув.

        threads = new ComputeThread[threadsCount]; // Створюється масив потоків і запускаються всі потоки в циклі за допомогою методу start()
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new ComputeThread(array, i, threadsCount, this);
            threads[i].start();
        }
        WaitForThreads(); //  очікує завершення всіх потоків

        long endTime = System.nanoTime();

        System.out.println("Min index: " + minIndex + ", min element: " + array[minIndex]);
        System.out.println("Time: " + (endTime - startTime) + " nanoseconds");

        //заміряється час виконання і виводиться індекс і значення мінімального елемента в масиві а також час виконання
    }

    synchronized private void WaitForThreads(){
        while (threadsCount > threadIndexDone){ // чи кількість завершених потоків threadIndexDone менше загальної кількості потоків
            try {
                wait(); // якщо потік ще не завершено, викликається метод wait(), який приостанавлює виконання поточного потоку до тех пор,
                // поки інший потік не викликає метод notify()або notifyAll().
            } catch (InterruptedException e) {
                e.printStackTrace(); // трассировку стека
            }
        }
    }

    synchronized public void incrementThreadIndexDone(int threadIndex){ //є синхронізованим і викликається кожним потоком ComputeThread для завершення своєї роботи
        // оновлює значення змінних minIndex і minElement, які містять мінімальне значення
        System.out.println("Thread " + threadIndex + " done with min index: " + threads[threadIndex].getMinIndex() + ", with value: " + threads[threadIndex].getMinElement());

//Якщо значення minIndex ще не було встановлено, то воно встановлюється першим прийшовшим потоком. Якщо значення вже встановлено,
// то метод перевіряє, менше чи мінімальне значення, знайдене в поточному потоці, ніж поточне значення minElement.

        if (minIndex == -1) {
            minIndex = threads[threadIndex].getMinIndex();
            minElement = threads[threadIndex].getMinElement();
        }
        else if (threads[threadIndex].getMinElement() < minElement) {
            minIndex = threads[threadIndex].getMinIndex();
            minElement = threads[threadIndex].getMinElement();
        }

        threadIndexDone++;
        notify(); // розбудити очікуваний потік, який чекає, поки всі потоки закінчать свою роботу
    }

    private void RandomizeArray(int[] array) { // генерує випадкове значення
        //в діапазоні від Integer.MIN_VALUE до 0, потім вибирає випадковий індекс randomIndex
        // в межах довжини масиву array і надає йому це випадкове значення.
        Random random = new Random();
        int randomIndex = random.nextInt(array.length);
        int randomValue = random.nextInt(Integer.MIN_VALUE, 0);
        array[randomIndex] = randomValue;
        System.out.println("Randomize min index in: " + randomIndex + ", with value: " + randomValue);
    }
}

class ComputeThread extends Thread{

    private final Main owner;
    private final int[] array;
    private final int threadIndex;
    private final int threadsCount;

    private int minIndex;
    private int minElement;


    public ComputeThread(int[] array, int threadIndex, int threadsCount, Main owner) {
        this.array = array;
        this.threadIndex = threadIndex;
        this.threadsCount = threadsCount;
        this.owner = owner;
    }

    @Override
    public void run() {
        int firstIndex = threadIndex * array.length / threadsCount;
        int lastIndex = (threadIndex + 1) * array.length / threadsCount;
        minIndex = firstIndex;
        minElement = array[firstIndex];
        for (int i = firstIndex; i < lastIndex; i++) {
            if (array[i] < minElement) {
                minElement = array[i];
                minIndex = i;
            }
        }
        owner.incrementThreadIndexDone(threadIndex);
    }

    public int getMinIndex() {
        return minIndex;
    }
    public int getMinElement() {
        return minElement;
    }
}