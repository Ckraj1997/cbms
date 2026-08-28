package mca.fincorebanking.service;

import java.util.List;
import java.util.Map;

public interface SuperAdminService {

    List<Map<String, Object>> executeSql(String query);

    Map<String, Object> getSystemStats();
}