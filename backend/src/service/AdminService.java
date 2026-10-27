package service;

import model.Course;
import model.Enrollment;
import model.User;
import repository.DataStore;

import java.util.*;

/**
 * Service providing statistical computations and operational aggregations
 * for the Institution Administrator dashboard.
 */
public class AdminService {
    private final DataStore dataStore;

    public AdminService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Computes genuine, real-time institutional metrics based on stored records.
     */
    public Map<String, Object> getSystemStatistics() {
        int totalCourses = dataStore.getCourseMap().size();
        
        // Count active registered students
        int totalStudents = 0;
        for (User u : dataStore.getUserMap().values()) {
            if (u.isStudent()) {
                totalStudents++;
            }
        }

        int totalEnrollmentRecords = dataStore.getEnrollments().size();

        int totalCapacity = 0;
        int totalFilledSeats = 0;
        for (Course c : dataStore.getCourseMap().values()) {
            totalCapacity += c.getCapacity();
            totalFilledSeats += c.getEnrolledCount();
        }
        int totalAvailableSeats = Math.max(0, totalCapacity - totalFilledSeats);
        double utilizationRate = totalCapacity > 0
                ? Math.round(((double) totalFilledSeats / totalCapacity) * 1000.0) / 10.0
                : 0.0;

        // Department Breakdown
        Map<String, Map<String, Integer>> deptMap = new LinkedHashMap<>();
        for (Course c : dataStore.getCourseMap().values()) {
            String dept = c.getDepartment();
            Map<String, Integer> deptStats = deptMap.computeIfAbsent(dept, k -> {
                Map<String, Integer> map = new LinkedHashMap<>();
                map.put("courseCount", 0);
                map.put("totalCapacity", 0);
                map.put("enrolledCount", 0);
                return map;
            });
            deptStats.put("courseCount", deptStats.get("courseCount") + 1);
            deptStats.put("totalCapacity", deptStats.get("totalCapacity") + c.getCapacity());
            deptStats.put("enrolledCount", deptStats.get("enrolledCount") + c.getEnrolledCount());
        }

        List<Map<String, Object>> deptList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : deptMap.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("department", entry.getKey());
            row.put("courseCount", entry.getValue().get("courseCount"));
            row.put("totalCapacity", entry.getValue().get("totalCapacity"));
            row.put("enrolledCount", entry.getValue().get("enrolledCount"));
            deptList.add(row);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCourses", totalCourses);
        stats.put("totalStudents", totalStudents);
        stats.put("totalEnrollments", totalEnrollmentRecords);
        stats.put("totalCapacity", totalCapacity);
        stats.put("totalFilledSeats", totalFilledSeats);
        stats.put("totalAvailableSeats", totalAvailableSeats);
        stats.put("utilizationRate", utilizationRate);
        stats.put("departments", deptList);

        return stats;
    }

    /**
     * Lists registered students.
     */
    public List<Map<String, Object>> getRegisteredStudents() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : dataStore.getUserMap().values()) {
            if (u.isStudent()) {
                list.add(u.toMap(false));
            }
        }
        return list;
    }
}
