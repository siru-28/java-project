
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class ConsoleColor {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";
    public static final String PURPLE = "\u001B[35m";
}

abstract class Person {
    protected String fullName;

    public Person(String name) {
        this.fullName = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String name) {
        this.fullName = name;
    }
}

abstract class Student extends Person {
    protected String id, semester, academicYear, bioType, currentCourse = "N/A";
    protected int attendedCount = 0, totalClasses = 0, lateCount = 0;

    public Student(String id, String name, String sem, String year, String type) {
        super(name);
        this.id = id;
        this.semester = sem;
        this.academicYear = year;
        this.bioType = type;
    }

    public abstract String getStudentType();

    public String getId() {
        return id;
    }

    public String getSemester() {
        return semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public String getCurrentCourse() {
        return currentCourse;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int total) {
        this.totalClasses = total;
    }

    public int getAttendedCount() {
        return attendedCount;
    }

    public void setCourse(String course) {
        this.currentCourse = course;
    }

    public String getCourse() {
        return currentCourse;
    }

    public void setSemester(String sem) {
        this.semester = sem;
    }

    public void setYear(String yr) {
        this.academicYear = yr;
    }

    public double getPercentage() {
        return (totalClasses == 0) ? 0 : (attendedCount * 100.0 / totalClasses);
    }

    public void markAttendance(boolean isLate) {
        attendedCount++;
        if (isLate)
            lateCount++;
    }

    public void setStudentType(String type) {
    }

    public void setBioType(String type) {
        this.bioType = type;
    }
}

class RegularStudent extends Student {
    public RegularStudent(String id, String name, String sem, String year, String type) {
        super(id, name, sem, year, type);
    }

    @Override
    public String getStudentType() {
        return "REGULAR";
    }
}

class AddInStudent extends Student {
    public AddInStudent(String id, String name, String sem, String year, String type) {
        super(id, name, sem, year, type);
    }

    @Override
    public String getStudentType() {
        return "ADD-IN";
    }
}

class StudentManager {

    private static final Object End = null;
    private List<Student> students = new ArrayList<>();
    private final String DATA_FILE = "database.txt";

    public void loadAll() {
        File file = new File(DATA_FILE);
        if (!file.exists())
            return;
        try (Scanner fs = new Scanner(file)) {
            while (fs.hasNextLine()) {
                String line = fs.nextLine();
                String[] p = line.split(",");
                if (p.length < 8)
                    continue;

                Student s;
                if (p[2].equals("REGULAR")) {
                    s = new RegularStudent(p[0], p[1], p[3], p[7], "Fingerprint");
                } else {
                    s = new AddInStudent(p[0], p[1], p[3], p[7], "Fingerprint");
                }
                int attended = Integer.parseInt(p[4]);
                int total = Integer.parseInt(p[5]);
                for (int i = 0; i < attended; i++) {
                    s.markAttendance(false);
                }
                s.setTotalClasses(total);
                s.setCourse(p[6]);
                students.add(s);
            }
        } catch (Exception e) {
            System.out.println("Error loading: " + e.getMessage());
        }
    }

    public List<Student> getStudents() {
        return students;
    }

    public void addStudent(Student s) {
        students.add(s);
        saveAll();
    }

    public Student findById(String id) {
        for (Student s : students)
            if (s.getId().equalsIgnoreCase(id))
                return s;
        return null;
    }

    public void deleteStudent(String id) {
        students.removeIf(s -> s.getId().equalsIgnoreCase(id));
        saveAll();
    }

    public void clearAll() {
        students.clear();
        saveAll();
    }

    public void saveAll() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("database.txt"))) {
            for (Student s : students) {
                pw.printf("%s,%s,%s,%s,%d,%d,%s,%s%n",
                        s.getId(), s.getFullName(), s.getStudentType(), s.getSemester(),
                        s.getAttendedCount(), s.getTotalClasses(), s.getCourse(), s.getAcademicYear());
            }
        } catch (Exception e) {
            System.out.println("Error saving database!");
        }
    }

    public void saveReport(String inst, LocalTime start) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("report.txt"))) {
            pw.println("=========================================================================================");
            pw.printf("| INSTRUCTOR: %-15s | CLASS START: %-15s |%n", inst, start);
            pw.println("=========================================================================================");
            pw.printf("| %-10s | %-18s | %-10s | %-8s | %-10s |%n", "ID", "NAME", "TYPE", "PRESENT", "COURSE");
            pw.println("-----------------------------------------------------------------------------------------");
            for (Student s : students) {
                pw.printf("| %-10s | %-18s | %-10s | %-8d | %-10s |%n",
                        s.getId(), s.getFullName(), s.getStudentType(), s.getAttendedCount(), s.getCourse());
            }
            pw.println("=========================================================================================");
            System.out.println("Report saved to report.txt!");
        } catch (Exception e) {
            System.out.println("Error saving report!");
        }
    }

    public void showDashboard(String inst, LocalTime start, LocalTime end) {
        int total = students.size(), reg = 0, addin = 0, low = 0, good = 0, sumClasses = 0;
        for (Student s : students) {
            if (s.getStudentType().equals("REGULAR"))
                reg++;
            else
                addin++;
            if (s.getPercentage() < 80)
                low++;
            else
                good++;
            sumClasses += s.getTotalClasses();
        }
        System.out.println(ConsoleColor.CYAN
                + "\n╔══════════════════════════════════════════════════════════════════════════════════");

        System.out.printf("║  INSTRUCTOR: %-15s | START: %-10s | END: %-10s ║%n", inst, start, end);

        System.out.printf("║  TOTAL REGISTERED: %-10d | REGULAR: %-10d | ADD-IN: %-15d ║%n", total, reg, addin);

        System.out.printf("║  LOW STATUS (<80): %-11d | GOOD STATUS (>=80): %-10d | TOTAL CLASS: %-4d ║%n", low, good,
                sumClasses);

        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════╝"
                + ConsoleColor.RESET);
    }

    public List<Student> getAll() {
        return students;
    }
}

public class SmartAttendanceFinal {
    private static Scanner sc = new Scanner(System.in);
    private static Set<String> dailyCourses = new HashSet<>();

    // Login Function
    private static boolean login() {
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;
        while (true) {
            System.out.println(ConsoleColor.PURPLE + "\n========== SYSTEM LOGIN ==========");
            System.out.print("Username: ");
            String user = sc.nextLine();
            System.out.print("Password (Numbers only): ");
            String passInput = sc.nextLine();
            boolean isUserCorrect = user.equals("@siru00");
            boolean isPassCorrect = false;
            int pass = -1;
            try {
                pass = Integer.parseInt(passInput);
                if (pass == 1234)
                    isPassCorrect = true;
            } catch (NumberFormatException e) {
                isPassCorrect = false;
            }
            if (isUserCorrect && isPassCorrect) {
                System.out.println(ConsoleColor.GREEN + "Access Granted! Welcome to the System." + ConsoleColor.RESET);
                return true;
            } else {
                attempts++;
                String errorMsg;

                if (!isUserCorrect && !isPassCorrect) {
                    errorMsg = "Incorrect Username and Password!";
                } else if (!isUserCorrect) {
                    errorMsg = "Incorrect Username!";
                } else {
                    errorMsg = "Incorrect Password!";
                }
                System.out.println(ConsoleColor.RED + errorMsg + " You have " + (MAX_ATTEMPTS - attempts)
                        + " attempts left." + ConsoleColor.RESET);
            }
            if (attempts >= MAX_ATTEMPTS) {
                System.out.println(ConsoleColor.RED + "\nToo many failed attempts!");
                for (int i = 10; i > 0; i--) {
                    System.out.print("\rPlease wait " + i + " seconds before trying again... ");
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }
                System.out.println("\n");
                attempts = 0;
            }
        }
    }

    public static void main(String[] args) {
        if (!login()) {
            return;
        }
        StudentManager m = new StudentManager();
        m.loadAll();
        System.out.println("\n--- Session Setup ---");
        System.out.print("Enter Instructor Name: ");
        String inst = sc.nextLine();
        LocalTime Start = null;
        LocalTime End = null;
        while (Start == null) {
            try {
                System.out.println("Class Start Time (HH:mm): ");
                Start = LocalTime.parse(sc.nextLine());
            } catch (Exception e) {
                System.out
                        .println(ConsoleColor.RED + "error the time should be in the form of HH:mm"
                                + ConsoleColor.RESET);
            }
        }
        while (End == null) {
            try {
                System.out.print("Class End Time (HH:mm): ");
                End = LocalTime.parse(sc.nextLine());
                if (End.isBefore(Start)) {
                    System.out.println(
                            ConsoleColor.RED + "Error: End time cannot be before start time!" + ConsoleColor.RESET);
                    End = null;
                }
            } catch (Exception e) {
                System.out
                        .println(ConsoleColor.RED + "Error: Time should be in HH:mm format!" + ConsoleColor.RESET);
            }
        }
        int grace = 0;
        boolean graceValid = false;
        while (!graceValid) {
            try {
                System.out.print("Grace (minutes): ");
                grace = Integer.parseInt(sc.nextLine());
                graceValid = true; // በትክክል ካስገባ loop ይቆማል
            } catch (NumberFormatException e) {
                System.out.println(
                        ConsoleColor.RED + "Error: Please enter numbers only for grace time!" + ConsoleColor.RESET);
            }
        }
        String globalYear = "2018";
        while (true) {
            m.showDashboard(inst, Start, End);
            System.out.println(ConsoleColor.YELLOW
                    + "1. Register New Student  2. ATTEND  3. SEARCH  4. PERFORMANCE  5. EDIT  6. DELETE  7. REPORT  8. CLEAR ALL  9. EXIT"
                    + ConsoleColor.RESET);
            System.out.print("Action(Enter Number) > ");
            int ch = 0;
            try {
                ch = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println(
                        ConsoleColor.RED + "Invalid input! Please Enter a Number." + ConsoleColor.RESET);
                continue;
            }
            if (ch == 9) {
                System.out.println(ConsoleColor.PURPLE + "Exiting System...Goodbye!" + ConsoleColor.RESET);
                break;
            }

            String id;
            switch (ch) {
                case 1:
                    while (true) {

                        System.out.print("Enter ID ( max 10): ");
                        id = sc.nextLine().trim();
                        if (m.findById(id) != null) {
                            System.out
                                    .println(ConsoleColor.RED + "This ID is already registered!" + ConsoleColor.RESET);
                            continue;
                        }

                        if (id.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{2,10}$")) {
                            break;
                        } else {
                            System.out.println(ConsoleColor.RED
                                    + "Invalid ID! mix letters and numbers and max 10 chars."
                                    + ConsoleColor.RESET);
                        }
                    }
                    String name = "";
                    while (true) {
                        System.out.print("Full Name: ");
                        name = sc.nextLine().trim();

                        if (!name.isEmpty() && name.matches("^[a-zA-Z\\s]+$"))
                            break;
                        System.out.println(ConsoleColor.RED + "Please enter your name correctly (Letters only)!"
                                + ConsoleColor.RESET);
                    }

                    System.out.print("Enter Semester: ");
                    String sem = sc.nextLine();
                    System.out.print("Enter Accadamic Year: ");
                    globalYear = sc.nextLine();
                    System.out.print("Scan Fingerprint (Press Enter): ");
                    sc.nextLine();
                    RegularStudent regS = new RegularStudent(id, name, sem, globalYear,
                            "Fingerprint");
                    regS.setTotalClasses(0);
                    m.addStudent(regS);
                    System.out.println(
                            ConsoleColor.GREEN + "\n[✔️] Student '" + name + "' Successfully Registered!"
                                    + ConsoleColor.RESET);
                    break;
                case 2:
                    String CourseName;
                    while (true) {
                        System.out.print("Course Name: ");
                        CourseName = sc.nextLine().trim();
                        if (CourseName.matches("^[a-zA-Z\\s]+$")) {
                            break;
                        }
                        System.out.println("Invalid Course Name! please Enter letter only");
                    }
                    System.out.print("Student ID: ");
                    String sid = sc.nextLine().trim();
                    Student s = m.findById(sid);
                    if (s != null) {
                        s.setCourse(CourseName);
                        s.setTotalClasses(s.getTotalClasses() + 1);
                        System.out.println("Scanning Fingerprint... Done!");
                        boolean late = LocalTime.now().isAfter(Start.plusMinutes(grace));
                        s.markAttendance(late);
                        m.saveAll();
                        System.out.println(ConsoleColor.GREEN + "Attendance Marked!" + ConsoleColor.RESET);

                    } else {
                        System.out.println(ConsoleColor.YELLOW +
                                "ID not found! Is this an Add-in student? (yes/no)"
                                + ConsoleColor.RESET);
                        if (sc.nextLine().equalsIgnoreCase("yes")) {
                            System.out.print("Full Name: ");
                            String n = sc.nextLine();
                            System.out.print("Semester: ");
                            String sm = sc.nextLine();
                            String y;
                            while (true) {
                                System.out.print("Academic Year (Batch): ");
                                y = sc.nextLine().trim();

                                try {
                                    if (y.isEmpty()) {
                                        System.out.println(
                                                ConsoleColor.RED + "Input cannot be Empty" + ConsoleColor.RESET);
                                        continue;
                                    }
                                    int regularYear = Integer.parseInt(globalYear.trim());
                                    int addInYear = Integer.parseInt(y);
                                    if (addInYear > regularYear) {
                                        break;
                                    } else {
                                        System.out.println(ConsoleColor.RED
                                                + "Error: Add-in students must be from a senior batch (Year > "
                                                + regularYear + ")!" + ConsoleColor.RESET);
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println(ConsoleColor.RED + "Please enter a valid year in numbers!"
                                            + ConsoleColor.RESET);
                                }
                            }
                            AddInStudent ads = new AddInStudent(sid, n, sm, y, "Fingerprint");
                            ads.setCourse(CourseName);
                            ads.setTotalClasses(1);
                            ads.markAttendance(false);
                            m.addStudent(ads);
                            System.out.println(
                                    ConsoleColor.GREEN + "Add-in Registered & Attendance Marked!" +
                                            ConsoleColor.RESET);
                        } else {
                            System.out.println(ConsoleColor.RED + "Attendance cancelled." +
                                    ConsoleColor.RESET);
                        }
                    }
                    break;
                case 3:
                    System.out.print("ID: ");
                    Student f = m.findById(sc.nextLine());
                    if (f != null)
                        System.out.println("Found: " + f.getFullName());
                    break;
                case 4:
                    System.out.print("ID: ");
                    Student ps = m.findById(sc.nextLine());
                    if (ps != null) {
                        System.out.println(
                                "\nName: " + ps.getFullName() + " | Percent: " + ps.getPercentage() + "%");
                    }
                    break;
                case 5:
                    System.out.print("Enter ID: ");
                    id = sc.nextLine().trim();
                    Student es = m.findById(id);
                    if (es != null) {
                        System.out.println("\n============================");
                        System.out.println("   CURRENT STUDENT INFO");
                        System.out.println("============================");
                        System.out.println("ID: " + es.getId());
                        System.out.println("Name: " + es.getFullName());
                        System.out.println("Type: " + es.getStudentType());
                        System.out.println("Course: " + es.getCourse());
                        System.out.println(
                                "Attendance: " + es.getPercentage() + "% (" + es.getTotalClasses() + " classes)");
                        System.out.println("============================\n");
                        System.out.println("What do you want to update?");
                        System.out.println("1. Name");
                        System.out.println("2. Student Type (REGULAR/ADD-IN)");
                        System.out.println("3. Reset Attendance (Total Classes)");
                        System.out.print("Choice: ");
                        String choice = sc.nextLine();
                        switch (choice) {
                            case "1":
                                System.out.print("Enter New Name: ");
                                String nN = sc.nextLine();
                                if (!nN.isEmpty())
                                    es.setFullName(nN);
                                break;
                            case "2":
                                System.out.print("Enter New Type (REGULAR/ADD-IN): ");
                                String nT = sc.nextLine().toUpperCase();
                                if (!nT.isEmpty())
                                    es.setStudentType(nT);
                                break;
                            case "3":
                                System.out.print("Are you sure you want to reset classes to 0? (y/n): ");
                                if (sc.nextLine().equalsIgnoreCase("y")) {
                                    es.setTotalClasses(0);
                                }
                                break;
                            default:
                                System.out.println("Invalid choice.");
                                break;
                        }
                        m.saveAll();
                        System.out.println("Update successful and saved to file!");
                    } else {
                        System.out.println("Student with ID " + id + " not found.");
                    }
                    break;
                case 6:
                    System.out.print("Enter ID to Delete: ");
                    String deleteId = sc.nextLine();
                    Student ds = m.findById(deleteId);

                    if (ds != null) {
                        System.out.print("Are you sure you want to delete " + ds.getFullName() + "? (y/n): ");
                        String confirm = sc.nextLine();
                        if (confirm.equalsIgnoreCase("y")) {
                            m.getStudents().remove(ds);
                            m.saveAll();
                            System.out.println("Student deleted successfully!");
                        } else {
                            System.out.println("Deletion cancelled.");
                        }
                    } else {
                        System.out.println("Student not found!");
                    }
                    break;

                case 7:
                    System.out.println("\n--- REPORT ---");
                    for (Student st : m.getAll())
                        System.out.println(
                                st.getId() + " | " + st.getFullName() + " | " + st.getPercentage() + "%");
                    break;

                case 8:
                    System.out.print("Clear all? ");
                    if (sc.nextLine().equalsIgnoreCase("yes"))
                        m.clearAll();
                    break;
            }
        }
    }
}
