import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("input.txt")));
        Scanner scanner = new Scanner(input);

        Parser parser = new Parser(scanner);

//        Token a = scanner.nextToken();
//        while (a.getTag() != DomainTag.END) {
//            System.out.println(a.getValue() + " " + a.getTag());
//            a = scanner.nextToken();
//        }
    }
}