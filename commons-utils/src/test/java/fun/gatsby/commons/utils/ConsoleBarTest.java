package fun.gatsby.commons.utils;

public class ConsoleBarTest {

    public static void main(String[] args) throws InterruptedException {
        ConsoleProgressBar bar = new ConsoleProgressBar();
        bar.showCurrent();
        for (int i = 0; i < 100; i++) {
            bar.iterate(51);
            bar.showCurrent();
            Thread.sleep(50);
        }
//        for(int i = 0; i < 5; i++) {
//            System.out.println("**********************************");
//        }
//        // ESC[5A - cursor up 5 times
//        // \r - cursor return to begin of line
//        // ESC[J - erase to end of screen
//        System.out.print("\033[5A\r\033[J");
//        for(int i = 0; i < 5; i++) {
//            System.out.println("##################################");
//        }
    }
}
