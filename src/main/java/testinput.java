import java.util.Scanner;

public class testinput implements Runnable{

    @Override
    public void run() {
            while(true) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("HI TYPE");
                String input = scanner.nextLine();
                System.out.println("I AM PRINTING INPUT I AM PRINTING INPUT I AM PRITING INPUT" + input);

            }
    }
}
