import java.util.Scanner;

public class test implements Runnable{
    Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        System.out.println("please input something");
        String input = scanner.nextLine();
        System.out.println("you have inputted: " + input);
    }
}
