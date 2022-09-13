package util;

import java.io.*;
import java.util.Scanner;
import java.util.StringJoiner;

public class MyIO
{
    /**
     * 根据 sourceFilePath 读取文件，最后返回一个字符串
     * @return 源码字符串
     */
    public static String readFile(String filePath)
    {
        final InputStream stream;
        try
        {
            stream = new FileInputStream(filePath);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Source file is not founded.");
            throw new RuntimeException(e);
        }

        final Scanner scanner = new Scanner(stream);
        final StringJoiner stringJoiner = new StringJoiner("\n");
        while (scanner.hasNextLine())
        {
            stringJoiner.add(scanner.nextLine());
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
        return stringJoiner.toString();
    }

    public static void output(String filePath, String content)
    {
        PrintWriter writer;
        try
        {
            writer = new PrintWriter(filePath);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Target file is not founded.");
            throw new RuntimeException(e);
        }
        writer.print(content);
        writer.close();
    }
}
