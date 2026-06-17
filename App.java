public class App {

    // TODO: validér at args ikke er tom, før vi læser args[0]
    public static void main(String[] args) {
        String name = args[0];                  // FIXME: fejler hvis args er tom
        System.out.println(greet(name));
    }

    static String greet(String name) {
        // TODO: håndtér null eller tomt navn
        return "Hej, " + name + "!";
    }
}
