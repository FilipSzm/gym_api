package jwzp_ww_fs.app.models;

import java.time.DayOfWeek;
import java.util.Map;

public record Club(String name, String address, Map<DayOfWeek, OpeningHours> whenOpen) {
}
