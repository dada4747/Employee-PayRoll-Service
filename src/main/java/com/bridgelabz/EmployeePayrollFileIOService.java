package com.bridgelabz;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class EmployeePayrollFileIOService {
    public static final String PAYROLL_FILE_NAME = "payroll-file.txt";

    public static void writeData(List<EmployeePayrollData> employeePayrollData) {
        StringBuilder stringBuilder = new StringBuilder();
        employeePayrollData.forEach(emp -> stringBuilder.append(emp.toString().concat("\n")));

        try {
            Files.write(Paths.get(PAYROLL_FILE_NAME), stringBuilder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long countEntries() {
        long entries = 0;
        try {
            entries = Files.lines(new File(PAYROLL_FILE_NAME).toPath()).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }
}
