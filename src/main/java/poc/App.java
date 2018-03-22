package poc;

import org.apache.camel.main.Main;

public class App {

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.addMainListener(new Configuration());
        main.run(args);
    }

}

