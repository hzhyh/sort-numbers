/*
題目
給定一檔案內有若干個(N個)數目字(1W~100W)，
請利用Multi-Process與Multi-Thread撰寫一程式，
可將這些數目字切成K份(K由使用者自訂)進行排序(BubbleSort與MergeSort)，
同時顯示CPU執行之時間。

程式須實現以下四種方法
1.將N個數目字直接進行BubbleSort，並顯示CPU執行之時間。
2.將N個數目字切成K份，並由K個threads分別進行BubbleSort之後，再用thread(s)作MergeSort，並顯示CPU執行之時間。
3.將N個數目字切成K份，並由K個processes分別進行BubbleSort之後，再用process(es)作MergeSort，並顯示CPU執行之時間。
4.將N個數目字切成K份，在一個process內對K份資料進行BubbleSort之後，再用同一個process作MergeSort，並顯示CPU執行之時間。
 */

import java.util.Scanner;
import java.util.ArrayList;
import java.util.Date;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

//單個share的index範圍
class Range {
    final int left;
    final int right;
    
    Range(int left, int right) {
        this.left = left;
        this.right = right;
    }
}

public class NumberSorter {
    
    private static Scanner scanner = new Scanner(System.in); // 用來接受使用者的輸入
    private String filename; // 資料檔名
    private int valueK; // K
    private int[] numArray; // 未排序的原始資料
    private int[] numArraySorted; // 欲排序與已排序資料
    private Range[] shares; // 各分割部分之index範圍
    private int cpuTime; // 存放任務的執行時間，輸出時會用到
    private boolean goThreading; // 用來讓MergeSort判斷要不要使用multi-threading
    
    public static void main(String[] args) {
        
        // 印出作者與題目資訊
        opening();
        
        while (true) {
            // 開始一個新的排序
            NumberSorter sorter = new NumberSorter();
            
            // 讀檔
            sorter.read();
            
            // 排序並輸出
            sorter.sort1();
            sorter.sort2();
//            sorter.sort3();
            sorter.sort4();
            
            // 決定是否要排序其它檔案
            if (ending()) {
                break;
            }
        }
        
        System.out.println("Bye!");
    } // void main()
    
    private static boolean ending() {
        System.out.print("\nDo you want to sort another file? (Y/N): ");
        char c = scanner.next().charAt(0);
        if (c != 'y' && c != 'Y') {
            return true;
        }
        return false;
    }
    
    private void read() {
        
        // 確認input資料夾存在
        File file = new File("input");
        while (!file.exists() || !file.isDirectory()) {
            System.out.println("Please put all input files in SortNumbers/input/");
            System.out.println("Done? Press ENTER to continue...");
            scanner.nextLine();
        }
        
        // 輸入檔名，取得檔案
        System.out.print("Input filename: ");
        filename = scanner.next();
        file = new File("input", filename);
        
        // 若檔案不存在，要求重新輸入直到取得存在的檔案
        while (!file.exists()) {
            System.out.println("File not found! Check up and input again.");
            System.out.print("Input filename: ");
            filename = scanner.next();
            file = new File("input", filename);
        }
        
        // 讀取檔案內容
        FileReader fileReader = null;
        BufferedReader reader = null;
        ArrayList<Integer> numList = new ArrayList<Integer>();
        try {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            // 一行行依序讀完
            String thisLine = null;
            while ((thisLine = reader.readLine()) != null) {
                // 轉為 Integer 存進 numArray
                numList.add(Integer.valueOf(thisLine));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 關閉 fileReader 和 reader
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // 將numList內容複製到numArray
        numArray = new int[numList.size()];
        for (int i = 0; i < numList.size(); i++) {
            numArray[i] = numList.get(i);
        }
            
        // 輸入K
        System.out.print("Input K: ");
        valueK = scanner.nextInt();
        while (valueK > numArray.length) {
            System.out.println("K shouldn't be greater than the amount of numbers.");
            System.out.print("Input K: ");
            valueK = scanner.nextInt();
        }
        
        // 將資料切成K份
        share();
        
    } // void read()
    
    private void share() { // 決定K份資料各自的index範圍並存入shares
        shares = new Range[valueK];
        int size = numArray.length;
        for (int i = 0; i < valueK; i++) {
            // 決定該份資料的index範圍
            int left = size * i / valueK;
            int right = (size * (i+1) / valueK) - 1;
            // 範圍存入shares
            shares[i] = new Range(left, right);
        }
        printShares();
    }
    
    private void printShares() {
        System.out.println("共 " + numArray.length + " 筆資料，切成 " + valueK + " 份");
        for (int i = 0; i < shares.length; i++) {
            System.out.println("第 " + (i+1) + " 份有 " 
                               + (shares[i].right-shares[i].left+1) + " 筆資料，範圍是: " 
                               + shares[i].left + " ~ " + shares[i].right);
        }
    }

    private static void opening() {
        System.out.println("1092 作業系統 作業一");
        System.out.println();
        System.out.println("題目簡介：");
        System.out.println("給定一檔案內有若干個(N個)數目字(1W~100W)，\n"
                + "利用Multi-Process與Multi-Thread撰寫一程式，\n"
                + "可將這些數目字切成K份(K由使用者自訂)進行排序(BubbleSort與MergeSort)，\n"
                + "同時顯示CPU執行之時間。");
        System.out.println();
        System.out.println("程式須實現以下四種方法：\n"
                + "1.將N個數目字直接進行BubbleSort，並顯示CPU執行之時間。\n"
                + "2.將N個數目字切成K份，並由K個threads分別進行BubbleSort之後，再用thread(s)作MergeSort，並顯示CPU執行之時間。\n"
                + "3.將N個數目字切成K份，並由K個processes分別進行BubbleSort之後，再用process(es)作MergeSort，並顯示CPU執行之時間。\n"
                + "4.將N個數目字切成K份，在一個process內對K份資料進行BubbleSort之後，再用同一個process作MergeSort，並顯示CPU執行之時間。");
        System.out.println();
        System.out.println("Let's get started!");
        System.out.println();
    }

    // BubbleSort
    private void sort1() {
        //直接進行BubbleSort
        
        numArraySorted = numArray.clone();
        
        long startTime = System.currentTimeMillis();
        new BubbleSort(0, numArray.length-1).run();
        long endTime = System.currentTimeMillis();
        cpuTime = (int)(endTime - startTime);

        output(1);
    } // void sort1()
    
    private void sort2() {
        numArraySorted = numArray.clone();
        
        long startTime = System.currentTimeMillis();
        
        // 由K個threads分別進行BubbleSort
        Thread[] threads = new Thread[valueK];
        for (int i = 0; i < valueK; i++) {
            threads[i] = new Thread(new BubbleSort(shares[i].left, shares[i].right));
            threads[i].start();
        }
        // 等threads執行完畢
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {}
        }
        
        // 再用thread(s)作MergeSort
        goThreading = true; // 告訴 MergeSort 可以開新的 thread
        Thread t = new Thread(new MergeSort(0, valueK-1));
        t.start();
        // 等threads執行完畢
        try {
            t.join();
        } catch (InterruptedException e) {}
        
        long endTime = System.currentTimeMillis();
        cpuTime = (int)(endTime - startTime);
        
        output(2);
    } // void sort2()
    
    private void sort3() { // *****沒做不用看*****
        // 切成K份
        // 由K個processes分別進行BubbleSort
        // 再用process(es)作MergeSort
        numArraySorted = numArray.clone();
        
        long startTime = System.currentTimeMillis();
        
        // code here
        
        long endTime = System.currentTimeMillis();
        cpuTime = (int)(endTime - startTime);
        
        output(3);
    } // void sort3()
    
    private void sort4() {
        numArraySorted = numArray.clone();
        
        long startTime = System.currentTimeMillis();
        
        // 在一個process內對K份資料進行BubbleSort
        for (int i = 0; i < valueK; i++) {
            new BubbleSort(shares[i].left, shares[i].right).run();
        }
        
        // 再用同一個process作MergeSort
        goThreading = false; // 告訴 MergeSort 不要開新的 thread
        new MergeSort(0, valueK-1).run();
        
        long endTime = System.currentTimeMillis();
        cpuTime = (int)(endTime - startTime);
        
        output(4);
    } // void sort4()
    
    private class BubbleSort implements Runnable {
        int left, right; // numArraySorted 的 index 範圍
        BubbleSort(int left, int right) {
            this.left = left;
            this.right = right;
        }
        @Override
        public void run() {
            for (int i = 0; i < (right - left); i++) {
                for (int j = left; j < right-i; j++) {
                    if (numArraySorted[j] > numArraySorted[j+1]) {
                        int temp = numArraySorted[j];
                        numArraySorted[j] = numArraySorted[j+1];
                        numArraySorted[j+1] = temp;
                    }
                }
            }
        } // run()
    } // class BubbleSort
    
    private class MergeSort implements Runnable {
        int left, right; // shares 的 index 範圍
        MergeSort(int left, int right) {
            this.left = left;
            this.right = right;
        }
        @Override
        public void run() {
            // 從 left 到 right 至少2組
            if (left < right) {
                int middle = (left + right) / 2;
                // 從 left 到 right 至少3組，需要進一步 divide
                if (left < middle) {
                    if (goThreading) { // 用threads
                        Thread t1 = new Thread(new MergeSort(left, middle));
                        Thread t2 = new Thread(new MergeSort(middle+1, right));
                        t1.start();
                        t2.start();
                        try {
                            t1.join();
                            t2.join();
                        } catch (InterruptedException e) {}
                    } else { // 在原本的process
                        new MergeSort(left, middle).run();
                        new MergeSort(middle+1, right).run();
                    }
                    
                }
                
                merge(left, middle, right);
            }
        } // run()
        
        private void merge(int leftShare, int middleShare, int rightShare) {
            int left = shares[leftShare].left;
            int middle = shares[middleShare].right;
            int right = shares[rightShare].right;
            
            // 把資料分成A和B兩個Array,從left到middle是A，middle+1到right是B
            int lenA = middle - left + 1;
            int lenB = right - middle;
            int[] a = new int[lenA];
            int[] b = new int[lenB];
            
            // Copy data to temp arrays a[] and b[]
            for (int i = 0; i < lenA; i++) {
                a[i] = numArraySorted[left+i];
            }
            for (int i = 0; i < lenB; i++) {
                b[i] = numArraySorted[middle+i+1];
            }
            
            // 開始merge
            // 從temp arrays將資料放回numArraySorted
            int i = 0;
            int j = 0;
            int k = left;
            // 當temp arrays a 和 b都還沒比完
            while (i < lenA && j < lenB) {
                if (a[i] < b[j]) {
                    numArraySorted[k] = a[i];
                    i++;
                } else {
                    numArraySorted[k] = b[j];
                    j++;
                }
                k++;
            }

            // 將剩下的資料依序放回原array
            while (i < lenA) {
                numArraySorted[k] = a[i];
                i++;
                k++;
            }
            while (j < lenB) {
                numArraySorted[k] = b[j];
                j++;
                k++;
            }
        } // merge()
    } // class MergeSort
    
    private void output(int n) {
        
        // 創建output資料夾如果它不存在
        File file = new File("output");
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
        
        // 弄出output檔名
        String outputFilename;
        
        int dotIndex = filename.indexOf('.');
        if (dotIndex == -1) {
            // 無副檔名
            outputFilename = filename + "_output" + n;
        } else {
            // 有副檔名
            outputFilename = filename.substring(0, dotIndex) 
                    + "_output" + n 
                    + filename.substring(dotIndex);
        }
        
        file = new File("output", outputFilename);
        FileWriter fileWriter = null;
        try {
            // 將排序後的數字寫入檔案
            fileWriter = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            System.out.println("Sort :");
            writer.write("Sort :\n");
            for (int i = 0; i < numArraySorted.length; i++) {
                System.out.println(numArraySorted[i]);
                writer.write(Integer.toString(numArraySorted[i]));
                writer.newLine();
            }
            
            // 寫入時間
            System.out.println("CPU Time : " + cpuTime);
            writer.write("CPU Time : " + cpuTime);
            writer.newLine();
            SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss z");
            System.out.println("Output Time : " + sdf.format(new Date()));
            writer.write("Output Time : " + sdf.format(new Date()));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
} // class NumberSorter
