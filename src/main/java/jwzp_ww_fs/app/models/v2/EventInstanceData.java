package jwzp_ww_fs.app.models.v2;

import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(example = """
        \"date\": \"9999-12-31\",
        \"time\": \"00:00\",
        \"capacity\": 0,
        """)
public record EventInstanceData(int capacity, LocalDate date, LocalTime time)
{}
