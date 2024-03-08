package View;

import static View.ColorCode.red;
import static View.ColorCode.reset;

public class Animation {

    public void starting() {
        int totalBlocks = 30;
        for (int i = 0; i <= 100; i += 2) {
            int blocksToShow = (i * totalBlocks) / 100;
            displayProgressBar(i, blocksToShow, totalBlocks);
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(" " + reset);
    }

    private void displayProgressBar(int percentage, int blocksToShow, int totalBlocks) {
        StringBuilder progressBar = new StringBuilder();
        progressBar.append(" ".repeat(10))
                .append(" Starting [ ").append(percentage).append("% ]");
        progressBar.append(" ".repeat(10))
                .append(loadingBar(blocksToShow, totalBlocks))
                .append("\r");
        System.out.print(progressBar);
    }

    private String loadingBar(int blocksToShow, int totalBlocks) {
        return red + "█".repeat(Math.max(0, blocksToShow)) +
                " ".repeat(Math.max(0, totalBlocks - blocksToShow));
    }
}
