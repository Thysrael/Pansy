package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MyCompare
{
    public static void compareContent(String src1, String src2)
    {
        ArrayList<String> lines1 = getLines(src1);
        ArrayList<String> lines2 = getLines(src2);
        int i;
        boolean ac = true;
        for (i = 0; i < lines1.size() && i < lines2.size(); i++)
        {
            String s1 = lines1.get(i);
            String s2 = lines2.get(i);
            if (!s1.equals(s2))
            {
                System.out.println("at line " + i + ": ");
                System.out.println(src1 + "\t:\t" + s1);
                System.out.println(src2 + "\t:\t" + s2);
                System.out.println("=====================================");
                ac = false;
            }
        }

        if (i != lines1.size())
        {
            System.out.println(src1 + " is longer.");
            ac = false;
        }
        if (i != lines2.size())
        {
            System.out.println(src2 + " is longer.");
            ac = false;
        }

        if (ac)
        {
            System.out.println("Everything is OK.");
        }
    }

    private static ArrayList<String> getLines(String src)
    {
        final InputStream stream;
        try
        {
            stream = new FileInputStream(src);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Source file is not founded.");
            throw new RuntimeException(e);
        }

        final Scanner scanner = new Scanner(stream);

        ArrayList<String> strings = new ArrayList<>();

        while (scanner.hasNextLine())
        {
            strings.add(scanner.nextLine());
        }

        scanner.close();
        try
        {
            stream.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return strings;
    }
}
