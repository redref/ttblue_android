package ttblue_android.com.ttblue_android;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DeviceTypes {
    private static final Map<String, Integer> myMap;
    static {
        Map<String, Integer> aMap = new HashMap<>();
        aMap.put("SPORTS_WATCH_2_HW_REVISION_08", 2008);
        aMap.put("SPORTS_WATCH_2_HW_REVISION_09", 2009);
        myMap = Collections.unmodifiableMap(aMap);
    }

    public static Integer get(String devicetype) {
        return myMap.get(devicetype);
    }

    public static Boolean contains(Integer hardwarerevision) {
        return myMap.containsValue(hardwarerevision);
    }
}
